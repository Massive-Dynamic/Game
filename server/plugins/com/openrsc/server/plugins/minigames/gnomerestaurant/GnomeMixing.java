package com.openrsc.server.plugins.minigames.gnomerestaurant;

import com.openrsc.server.constants.ItemId;
import com.openrsc.server.model.container.Item;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.plugins.listeners.action.InvUseOnItemListener;
import com.openrsc.server.plugins.listeners.executive.InvUseOnItemExecutiveListener;
import org.apache.commons.collections4.IterableMap;

import static com.openrsc.server.plugins.Functions.*;

public class GnomeMixing implements InvUseOnItemListener, InvUseOnItemExecutiveListener {

	private GnomeCooking gc = new GnomeCooking();

	private boolean canMix(Item itemOne, Item itemTwo) {
		for (GnomeMix gm : GnomeMix.values()) {
			if (gm.isValid(itemOne.getID(), itemTwo.getID())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean blockInvUseOnItem(Player p, Item item1, Item item2) {
		return canMix(item1, item2);
	}

	@Override
	public void onInvUseOnItem(Player p, Item item1, Item item2) {
		GnomeMix gm = null;
		for (GnomeMix mix : GnomeMix.values()) {
			if (mix.isValid(item1.getID(), item2.getID())) {
				gm = mix;
			}
		}

		if (hasItem(p, gm.itemIDOther)) {
			p.setBusy(true);

			// Remove secondary ingredient
			if (gm.itemIDOther != ItemId.GNOME_SPICE.id())
				removeItem(p, gm.itemIDOther, 1);

			gc.addGnomeRecipeCache(p, gm.itemID, gm.itemIDOther);

			String recipe = "";
			if (p.getCache().hasKey("gnome_recipe")) {
				recipe = p.getCache().getString("gnome_recipe");
			}

			// If complete (denoted with a trailing ! on the recipe)
			if (recipe.endsWith("!")) {
				removeItem(p, gm.itemID, 1); // Remove base item

				if (gc.recipeStrings[gc.CHOC_CRUNCHIE].equals(recipe)) {
					addItem(p, ItemId.CHOC_CRUNCHIES.id(), 1);
				}
				else if (gc.recipeStrings[gc.WORM_CRUNCHIE].equals(recipe)) {
					addItem(p, ItemId.WORM_CRUNCHIES.id(), 1);
				}
				else if (gc.recipeStrings[gc.TOAD_CRUNCHIE].equals(recipe)) {
					addItem(p, ItemId.TOAD_CRUNCHIES.id(), 1);
				}
				else if (gc.recipeStrings[gc.SPICY_CRUNCHIE].equals(recipe)) {
					addItem(p, ItemId.SPICE_CRUNCHIES.id(), 1);
				}
				else if (gc.recipeStrings[gc.CHEESE_AND_TOMATO_BATTA].equals(recipe)) {
					addItem(p, ItemId.CHEESE_AND_TOMATO_BATTA.id(), 1);
				}
				else if (gc.recipeStrings[gc.WORM_BATTA].equals(recipe)) {
					addItem(p, ItemId.WORM_BATTA.id(), 1);
				}
				else if (gc.recipeStrings[gc.FRUIT_BATTA].equals(recipe)) {
					addItem(p, ItemId.FRUIT_BATTA.id(), 1);
				}
				else if (gc.recipeStrings[gc.VEG_BATTA].equals(recipe)) {
					addItem(p, ItemId.VEG_BATTA.id(), 1);
				}
				else if (gc.recipeStrings[gc.CHOC_BOMB].equals(recipe)) {
					addItem(p, ItemId.CHOCOLATE_BOMB.id(), 1);
				}
				else if (gc.recipeStrings[gc.VEGBALL].equals(recipe)) {
					addItem(p, ItemId.VEGBALL.id(), 1);
				}
				else if (gc.recipeStrings[gc.WORM_HOLE].equals(recipe)) {
					addItem(p, ItemId.WORM_HOLE.id(), 1);
				}
				resetGnomeCooking(p);
			}

			p.message(gm.messages[0]);
			p.setBusy(false);
		}
	}

	enum GnomeMix {
		CHEESE_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.CHEESE.id(),
				"you crumble the cheese over the gnome batta"),
		TOMATO_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.TOMATO.id(),
				"you add the tomato to the gnome batta"),
		SPRINKLE_LEAVE_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.EQUA_LEAVES.id(),
				"you sprinkle the equa leaves over the gnome batta"),
		CHOCOLATE_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.CHOCOLATE_BAR.id(),
				"you add the chocolate to the dough bowl"),
		LEAVES_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.EQUA_LEAVES.id(),
				"you add the equa leaves to the dough bowl"),
		CREAM_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.CREAM.id(),
				"you pour thick cream over the gnome bowl"),
		SPRINKLE_CHOCO_DUST_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.CHOCOLATE_DUST.id(),
				"you sprinkle the chocolate dust over the gnome bowl"),
		AQUA_LEAVES_ON_TOADLEGS(ItemId.TOAD_LEGS.id(), ItemId.EQUA_LEAVES.id(),
				"you mix the equa leaves with your toads legs"),
		SPRINKLE_SPICE_ON_TOADLEGS(ItemId.TOAD_LEGS.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle the spice over the toads legs"),
		SEASONED_TOADLEGS_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.TOAD_LEGS.id(),
				"you add the toads legs to the gnome batta"),
		KINGWORMS_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.KING_WORM.id(),
				"you add the worm to the dough bowl"),
		ONIONS_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.ONION.id(),
				"you add the onion to the dough bowl"),
		GNOMESPICE_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle some gnome spice over the dough bowl"),
		SPRINKLE_SPICE_ON_DOUGH(ItemId.GIANNE_DOUGH.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle the spice into the dough"),
		TOADLEGS_ON_DOUGH(ItemId.GIANNE_DOUGH.id(), ItemId.TOAD_LEGS.id(),
				"you mix the toad's legs into the dough"),
		SPRINKLE_LEAVE_ON_CRUNCHIES(ItemId.GNOMECRUNCHIE.id(), ItemId.EQUA_LEAVES.id(),
				"you sprinkle some leaves over the crunchies"),
		GNOMESPICE_ON_WORM(ItemId.KING_WORM.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle some gnome spice over your worm"),
		KINGWORM_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.KING_WORM.id(),
				"you add the king worms to the gnome batta"),
		ONION_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.ONION.id(),
				"you add the onion to the gnome batta"),
		CABBAGE_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.CABBAGE.id(),
				"you add the Cabbage to the gnome batta"),
		DWELLBERRIES_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.DWELLBERRIES.id(),
				"you add the dwell berries to the gnome batta"),
		COCOLATE_ON_DOUGH(ItemId.GIANNE_DOUGH.id(), ItemId.CHOCOLATE_BAR.id(),
				"you crumble the chocolate into the dough"),
		CHOCO_DUST_ON_CRUNCHIES(ItemId.GNOMECRUNCHIE.id(), ItemId.CHOCOLATE_DUST.id(),
				"you sprinkle the chocolate dust over the crunchie"),
		POTATOES_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.POTATO.id(),
				"you add the potato to the dough bowl"),
		TOADLEGS_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.TOAD_LEGS.id(),
				"you add the taods legs to the gnome bowl"),
		ADD_CHEESE_TO_BOWL(ItemId.GNOMEBOWL.id(), ItemId.CHEESE.id(),
				"you add the cheese to the dough bowl"),
		DWELLBERRIES_ON_BOWL(ItemId.GNOMEBOWL.id(), ItemId.DWELLBERRIES.id(),
				"you add the dwell berries to the dough bowl"),
		LEAVES_ON_DOUGH(ItemId.GIANNE_DOUGH.id(), ItemId.EQUA_LEAVES.id(),
				"you mix the equaleaves into the dough"),
		KINGWORMS_ON_DOUGH(ItemId.GIANNE_DOUGH.id(), ItemId.KING_WORM.id(),
				"you mix the worm into the dough"),
		SPRINKLE_SPICE_OVER_CRUNCHIES(ItemId.GNOMECRUNCHIE.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle some spice over the crunchies"),
		DICED_ORANGE_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.DICED_ORANGE.id(),
				"you sprinkle the orange chunks over the gnome batta"),
		LIME_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.LIME_CHUNKS.id(),
				"you sprinkle the lime chunks over the gnome batta"),
		PINE_APPLE_ON_BATTA(ItemId.GNOMEBATTA.id(), ItemId.PINEAPPLE_CHUNKS.id(),
				"you sprinkle the pineapple chunks over the gnome batta"),
		SPRINKLE_SPICE_OVER_BATTA(ItemId.GNOMEBATTA.id(), ItemId.GNOME_SPICE.id(),
				"you sprinkle the gnome spice over the gnome batta");

		private int itemID;
		private int itemIDOther;
		private String[] messages;

		GnomeMix(int itemOne, int itemTwo, String... messages) {
			this.itemID = itemOne;
			this.itemIDOther = itemTwo;
			this.messages = messages;
		}

		public boolean isValid(int i, int is) {
			return compareItemsIds(new Item(itemID), new Item(itemIDOther), i, is);
		}
	}
}
