package com.openrsc.server.plugins.skills;

import com.openrsc.server.constants.ItemId;
import com.openrsc.server.constants.Skills;
import com.openrsc.server.event.custom.BatchEvent;
import com.openrsc.server.external.ObjectHarvestingDef;
import com.openrsc.server.model.TimePoint;
import com.openrsc.server.model.container.Item;
import com.openrsc.server.model.entity.GameObject;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.plugins.listeners.action.ObjectActionListener;
import com.openrsc.server.plugins.listeners.executive.ObjectActionExecutiveListener;
import com.openrsc.server.util.rsc.DataConversions;
import com.openrsc.server.util.rsc.Formulae;
import com.openrsc.server.util.rsc.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.openrsc.server.plugins.Functions.*;

public final class Harvesting implements ObjectActionListener,
	ObjectActionExecutiveListener {

	enum HarvestingEvents {
		NEGLECTED(-1),
		NONE(0),
		WATER(1),
		SOIL(2);

		private final int id;

		HarvestingEvents(int id) {
			this.id = id;
		}

		public int getID() {
			return id;
		}
	}

	private static class ItemLevelXPTrio {
		private int itemId;
		private int level;
		private int xp;
		ItemLevelXPTrio(int itemId, int level, int xp) {
			this.itemId = itemId;
			this.level = level;
			this.xp = xp;
		}

		int getItemId() { return itemId; }

		int getLevel() {
			return level;
		}

		int getXp() {
			return xp;
		}
	}

	enum HerbsProduce {
		HERB(1274, new ItemLevelXPTrio(ItemId.UNIDENTIFIED_GUAM_LEAF.id(), 9, 50),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_MARRENTILL.id(),14, 60),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_TARROMIN.id(), 19, 72),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_HARRALANDER.id(), 26, 96),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_RANARR_WEED.id(), 32, 122),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_IRIT_LEAF.id(), 44, 194),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_AVANTOE.id(), 50, 246),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_KWUARM.id(), 56, 312),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_CADANTINE.id(), 67, 480),
			new ItemLevelXPTrio(ItemId.UNIDENTIFIED_DWARF_WEED.id(), 79, 768)),
		SEAWEED(1280, new ItemLevelXPTrio(ItemId.SEAWEED.id(), 23, 84),
			new ItemLevelXPTrio(ItemId.EDIBLE_SEAWEED.id(), 23, 84)),
		LIMPWURTROOT(1281, new ItemLevelXPTrio(ItemId.LIMPWURT_ROOT.id(), 42, 144)),
		SNAPEGRASS(1273, new ItemLevelXPTrio(ItemId.SNAPE_GRASS.id(), 61, 328));

		private int objId;
		private ArrayList<ItemLevelXPTrio> produceTable;
		HerbsProduce(int objId, ItemLevelXPTrio... produce) {
			this.objId = objId;
			produceTable = new ArrayList<>();
			produceTable.addAll(Arrays.asList(produce));
		}

		public static HerbsProduce find(int objId) {
			for (HerbsProduce h : HerbsProduce.values()) {
				if (h.objId == objId) {
					return h;
				}
			}
			return null;
		}

		public ItemLevelXPTrio get(int itemId) {
			for (ItemLevelXPTrio i : produceTable) {
				if (i.itemId == itemId) {
					return i;
				}
			}
			return null;
		}

	}

	private final int[] itemsFruitTree = new int[]{
		ItemId.LEMON.id(), ItemId.LIME.id(), ItemId.RED_APPLE.id(),
		ItemId.ORANGE.id(), ItemId.GRAPEFRUIT.id(),
	};

	private final int[] itemsRegPalm = new int[]{
		ItemId.BANANA.id(), ItemId.COCONUT.id(),
	};

	private final int[] itemsOtherPalm = new int[]{
		ItemId.PAPAYA.id(),
	};

	private final int[] itemsBush = new int[]{
		ItemId.REDBERRIES.id(), ItemId.CADAVABERRIES.id(), ItemId.DWELLBERRIES.id(),
		ItemId.JANGERBERRIES.id(), ItemId.WHITE_BERRIES.id(),
	};

	private final int[] itemsAllotments = new int[]{
		ItemId.CABBAGE.id(), ItemId.RED_CABBAGE.id(), ItemId.WHITE_PUMPKIN.id(),
		ItemId.POTATO.id(), ItemId.ONION.id(), ItemId.GARLIC.id()
	};

	private int chanceAskSoil = 5;
	private int chanceAskWatering = 7;

	public static int getTool(Player p, GameObject obj) {
		String objName = obj.getGameObjectDef().getName();
		int expectedTool;
		if (objName.toLowerCase().contains("tree") || objName.toLowerCase().contains("palm") || objName.toLowerCase().contains("pineapple")) {
			expectedTool = ItemId.FRUIT_PICKER.id();
		} else {
			expectedTool = ItemId.HAND_SHOVEL.id();
		}
		return hasItem(p, expectedTool) ? expectedTool : ItemId.NOTHING.id();
	}

	@Override
	public void onObjectAction(final GameObject object, String command,
							   Player player) {
		int retrytimes;
		// Harvest of Xmas Tree
		if (object.getID() == 1238) {
			player.playerServerMessage(MessageType.QUEST, "You attempt to grab a present...");
			retrytimes = 10;
			player.setBatchEvent(new BatchEvent(player.getWorld(), player, 1800, "Harvesting Xmas", retrytimes, true) {
				@Override
				public void action() {
					final Item present = new Item(ItemId.PRESENT.id());
					if (getProduce(1, 1)) {
						//check if the tree still has gifts
						GameObject obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
						if (obj == null) {
							getOwner().playerServerMessage(MessageType.QUEST, "You fail to take from the tree");
							interrupt();
						} else {
							getOwner().getInventory().add(present);
							getOwner().playerServerMessage(MessageType.QUEST, "You get a nice looking present");
						}
						if (DataConversions.random(1, 1000) <= 100) {
							obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
							int depletedId = 1239;
							interrupt();
							if (obj != null && obj.getID() == object.getID()) {
								GameObject newObject = new GameObject(getWorld(), object.getLocation(), depletedId, object.getDirection(), object.getType());
								getWorld().replaceGameObject(object, newObject);
								getWorld().delayedSpawnObject(obj.getLoc(), 300 * 1000);
							}
						}
					} else {
						getOwner().playerServerMessage(MessageType.QUEST, "You fail to take from the tree");
						if (getRepeatFor() > 1) {
							GameObject checkObj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
							if (checkObj == null) {
								interrupt();
							}
						}
					}
					if (!isCompleted()) {
						getOwner().playerServerMessage(MessageType.QUEST, "You attempt to grab a present...");
					}

				}
			});
		} else if (command.equalsIgnoreCase("clip")) {
			handleClipHarvesting(object, player, player.click);
		} else {
			handleHarvesting(object, player, player.click);
		}
	}

	private void handleClipHarvesting(final GameObject object, final Player player,
								  final int click) {
		if (!harvestingChecks(object, player)) return;

		GameObject obj = player.getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
		final String objName = obj.getGameObjectDef().getName().toLowerCase();
		final HerbsProduce prodEnum = HerbsProduce.find(object.getID());
		int reqLevel = prodEnum != null ? prodEnum.produceTable.get(0).getLevel() : 1;

		if (!objName.contains("herb") && player.getSkills().getLevel(Skills.HARVESTING) < reqLevel) {
			player.playerServerMessage(MessageType.QUEST, "You need at least level " + reqLevel
				+ " harvesting to clip from the " + objName);
			return;
		}

		if (player.getInventory().countId(ItemId.HERB_CLIPPERS.id()) <= 0) {
			player.playerServerMessage(MessageType.QUEST,
				"You need some "
					+ player.getWorld().getServer().getEntityHandler()
					.getItemDef(ItemId.HERB_CLIPPERS.id())
					.getName().toLowerCase()
					+ " to clip from this havesting spot");
			return;
		}

		showBubble(player, new Item(ItemId.HERB_CLIPPERS.id()));
		player.playerServerMessage(MessageType.QUEST, "You attempt to clip from the spot...");
		player.setBatchEvent(new BatchEvent(player.getWorld(), player, 1800, "Harvesting", Formulae.getRepeatTimes(player, Skills.HARVESTING), true) {
			@Override
			public void action() {
				// herb uses herb drop table
				// seaweed 1/4 chance to be edible
				int prodId = !objName.contains("herb")
					? (objName.contains("sea weed") && DataConversions.random(1, 4) == 1 ? prodEnum.produceTable.get(1).getItemId()
					: prodEnum.produceTable.get(0).getItemId() ) : Formulae.calculateHerbDrop();
				int reqLevel = prodEnum.produceTable.get(0).getLevel();
				final Item produce = new Item(prodId);
				if (getWorld().getServer().getConfig().WANT_FATIGUE) {
					if (getWorld().getServer().getConfig().STOP_SKILLING_FATIGUED >= 1
						&& getOwner().getFatigue() >= getOwner().MAX_FATIGUE) {
						getOwner().playerServerMessage(MessageType.QUEST, "You are too tired to get produce");
						interrupt();
						return;
					}
				}
				if (!objName.contains("herb") && getOwner().getSkills().getLevel(Skills.HARVESTING) < reqLevel) {
					getOwner().playerServerMessage(MessageType.QUEST, "You need at least level " + reqLevel
						+ " harvesting to clip from the " + objName);
					interrupt();
					return;
				}

				if (getProduce(prodEnum.get(prodId).getLevel(), getOwner().getSkills().getLevel(Skills.HARVESTING))) {
					//check if the object is still up
					GameObject obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
					if (obj == null) {
						getOwner().playerServerMessage(MessageType.QUEST, "You fail to clip the plant");
						interrupt();
					} else {
						getOwner().getInventory().add(produce);
						getOwner().playerServerMessage(MessageType.QUEST, "You get " + (objName.contains("herb") ? "a herb"
							: "some " + (objName.contains(" ") ? objName.substring(objName.lastIndexOf(" ") + 1) : "produce")));
						getOwner().incExp(Skills.HARVESTING, prodEnum.get(prodId).getXp(), true);
					}
					if (DataConversions.random(1, 100) <= (!objName.contains("herb") ? 20 : 10)) {
						obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
						int depId = 1270;
						interrupt();
						if (obj != null && obj.getID() == object.getID()) {
							GameObject newObject = new GameObject(getWorld(), object.getLocation(), depId, object.getDirection(), object.getType());
							getWorld().replaceGameObject(object, newObject);
							getWorld().delayedSpawnObject(obj.getLoc(), DataConversions.random(60, 240) * 1000);
						}
					}
				} else {
					getOwner().playerServerMessage(MessageType.QUEST, "You fail to clip the plant");
					if (getRepeatFor() > 1) {
						GameObject checkObj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
						if (checkObj == null) {
							interrupt();
						}
					}
				}
				if (!isCompleted()) {
					getOwner().playerServerMessage(MessageType.QUEST, "You attempt to clip from the spot...");
					showBubble(getOwner(), new Item(ItemId.HERB_CLIPPERS.id()));
				}
			}
		});
	}

	private void handleHarvesting(final GameObject object, final Player player,
								   final int click) {
		if (!harvestingChecks(object, player)) return;

		final ObjectHarvestingDef def = player.getWorld().getServer().getEntityHandler().getObjectHarvestingDef(object.getID());

		final AtomicInteger evt = new AtomicInteger(checkCare(object, player));

		final int toolId = getTool(player, object);

		if (toolId != ItemId.NOTHING.id()) showBubble(player, new Item(toolId));
		player.playerServerMessage(MessageType.QUEST, "You attempt to get some produce...");
		player.setBatchEvent(new BatchEvent(player.getWorld(), player, 1800, "Harvesting", Formulae.getRepeatTimes(player, Skills.HARVESTING), true) {
			@Override
			public void action() {
				final Item produce = new Item(def.getProdId());
				if (getWorld().getServer().getConfig().WANT_FATIGUE) {
					if (getWorld().getServer().getConfig().STOP_SKILLING_FATIGUED >= 1
						&& getOwner().getFatigue() >= getOwner().MAX_FATIGUE) {
						getOwner().playerServerMessage(MessageType.QUEST, "You are too tired to get produce");
						interrupt();
						return;
					}
				}
				if (getOwner().getSkills().getLevel(Skills.HARVESTING) < def.getReqLevel()) {
					getOwner().playerServerMessage(MessageType.QUEST,"You need a harvesting level of " + def.getReqLevel() + " to get produce from here");
					interrupt();
					return;
				}

				if (toolId == ItemId.NOTHING.id() && DataConversions.random(0, 1) == 1) {
					getOwner().playerServerMessage(MessageType.QUEST, "You accidentally damage the produce and throw it away");
				} else if (evt.get() == HarvestingEvents.NEGLECTED.getID()) {
					getOwner().playerServerMessage(MessageType.QUEST, "But the spot seems weak, you decide to wait");
				} else if (getProduce(def.getReqLevel(), getOwner().getSkills().getLevel(Skills.HARVESTING))) {
					//check if the object is still up
					GameObject obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
					if (obj == null) {
						getOwner().playerServerMessage(MessageType.QUEST, "You fail to obtain some usable produce");
						interrupt();
					} else {
						String itemName = produce.getDef(player.getWorld()).getName().toLowerCase();
						getOwner().getInventory().add(produce);
						// if player did soil (or have an active one) they get small chance for another produce
						if (DataConversions.random(1, chanceAskSoil * 3) == 1
							&& evt.get() == HarvestingEvents.SOIL.getID()) {
							getOwner().getInventory().add(produce);
						}
						getOwner().playerServerMessage(MessageType.QUEST, "You get " +
							(itemName.endsWith("s") ? "some " : (startsWithVowel(itemName) ? "an " : "a ")) + itemName);
						getOwner().incExp(Skills.HARVESTING, def.getExp(), true);
					}
					if (DataConversions.random(1, 100) <= def.getExhaust()) {
						obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
						int depId = 1270;
						int prodId = def.getProdId();
						if (DataConversions.inArray(itemsFruitTree, prodId)) {
							depId = 1252; //exhausted tree
						} else if (DataConversions.inArray(itemsRegPalm, prodId)) {
							depId = 1253; //exhausted palm
						} else if (DataConversions.inArray(itemsOtherPalm, prodId)) {
							depId = 1254; //exhausted palm2
						} else if (prodId == ItemId.FRESH_PINEAPPLE.id()) {
							depId = 1255; //exhausted pineapple
						} else if (DataConversions.inArray(itemsBush, prodId)) {
							depId = 1261; //depleted bush
						} else if (prodId == ItemId.TOMATO.id()) {
							depId = 1271; //depleted tomato
						} else if (prodId == ItemId.CORN.id()) {
							depId = 1272; //depleted corn
						}
						interrupt();
						if (obj != null && obj.getID() == object.getID()) {
							// if player did water (or have an active one) they get small chance not to deplete node
							if (DataConversions.random(1, chanceAskWatering * 3) == 1
								&& evt.get() == HarvestingEvents.WATER.getID()) {
							}
							else if (def.getRespawnTime() > 0) {
								GameObject newObject = new GameObject(getWorld(), object.getLocation(), depId, object.getDirection(), object.getType());
								getWorld().replaceGameObject(object, newObject);
								getWorld().delayedSpawnObject(obj.getLoc(), def.getRespawnTime() * 1000);
							}
						}
					}
				} else {
					getOwner().playerServerMessage(MessageType.QUEST, "You fail to obtain some usable produce");
					if (getRepeatFor() > 1) {
						GameObject checkObj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
						if (checkObj == null) {
							interrupt();
						}
					}
				}
				if (!isCompleted()) {
					GameObject obj = getOwner().getViewArea().getGameObject(object.getID(), object.getX(), object.getY());
					evt.set(checkCare(obj, getOwner()));
					getOwner().playerServerMessage(MessageType.QUEST, "You attempt to get some produce...");
					if (toolId != ItemId.NOTHING.id()) showBubble(getOwner(), new Item(toolId));
				}
			}
		});
	}

	private boolean harvestingChecks(final GameObject obj, final Player player) {
		return !player.isBusy() && player.withinRange(obj, 1);
	}

	@Override
	public boolean blockObjectAction(GameObject obj, String command, Player player) {
		return command.equalsIgnoreCase("harvest") ||
			command.equalsIgnoreCase("clip") || (command.equals("collect") && obj.getID() == 1238);
	}

	private int checkCare(GameObject obj, Player p) {
		long timestamp = System.currentTimeMillis() + 3 * 60000;
		if (DataConversions.random(1, chanceAskWatering) == 1) {
			if (p.getAttribute("watered", null) == null
				|| expiredAction(obj, p, "watered")) {
				if (!hasItem(p, ItemId.WATERING_CAN.id())) {
					return HarvestingEvents.NEGLECTED.getID();
				}
				p.playerServerMessage(MessageType.QUEST, "You water the harvesting spot");
				p.setAttribute("watered", new TimePoint(obj.getX(), obj.getY(), timestamp));
				updateUsesWateringCan(p);
			}
			return HarvestingEvents.WATER.getID();
		} else if (DataConversions.random(1, chanceAskSoil) == 1) {
			if (p.getAttribute("soiled", null) == null
				|| expiredAction(obj, p, "soiled")) {
				if (!hasItem(p, ItemId.SOIL.id())) {
					return HarvestingEvents.NEGLECTED.getID();
				}
				p.playerServerMessage(MessageType.QUEST, "You add soil to the spot");
				p.setAttribute("soiled", new TimePoint(obj.getX(), obj.getY(), timestamp));
				p.getInventory().replace( ItemId.SOIL.id(), ItemId.BUCKET.id());
			}
			return HarvestingEvents.SOIL.getID();
		}
		return HarvestingEvents.NONE.getID();
	}

	private void updateUsesWateringCan(Player p) {
		if (!p.getCache().hasKey("uses_wcan")) {
			p.getCache().set("uses_wcan", 1);
		} else {
			int uses = p.getCache().getInt("uses_wcan");
			if (uses >= 4) {
				p.getInventory().remove(ItemId.WATERING_CAN.id(), 1);
				p.getCache().remove("uses_wcan");
			} else {
				p.getCache().put("uses_wcan", uses + 1);
			}
		}
	}

	private boolean expiredAction(GameObject obj, Player p, String key) {
		Object testObj = p.getAttribute(key);
		if (!(testObj instanceof TimePoint)) {
			return true;
		} else {
			TimePoint tp = (TimePoint) testObj;
			//expired or from distinct place
			return System.currentTimeMillis() - tp.getTimestamp() > 0 || !obj.getLocation().equals(tp.getLocation());
		}
	}

	private boolean getProduce(int reqLevel, int harvestingLevel) {
		return Formulae.calcGatheringSuccessful(reqLevel, harvestingLevel, 0);
	}
}
