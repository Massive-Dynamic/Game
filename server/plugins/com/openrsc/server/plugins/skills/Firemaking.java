package com.openrsc.server.plugins.skills;


import com.openrsc.server.ServerConfiguration;
import com.openrsc.server.constants.ItemId;
import com.openrsc.server.constants.Skills;
import com.openrsc.server.event.SingleEvent;
import com.openrsc.server.event.custom.BatchEvent;
import com.openrsc.server.event.rsc.GameStateEvent;
import com.openrsc.server.external.FiremakingDef;
import com.openrsc.server.model.Point;
import com.openrsc.server.model.container.Item;
import com.openrsc.server.model.entity.GameObject;
import com.openrsc.server.model.entity.GroundItem;
import com.openrsc.server.model.entity.Mob;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.model.entity.update.Bubble;
import com.openrsc.server.model.world.World;
import com.openrsc.server.model.world.region.TileValue;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.listeners.action.InvUseOnGroundItemListener;
import com.openrsc.server.plugins.listeners.action.InvUseOnItemListener;
import com.openrsc.server.plugins.listeners.executive.InvUseOnGroundItemExecutiveListener;
import com.openrsc.server.plugins.listeners.executive.InvUseOnItemExecutiveListener;
import com.openrsc.server.util.rsc.CollisionFlag;
import com.openrsc.server.util.rsc.Formulae;
import com.openrsc.server.util.rsc.MessageType;
import java.util.concurrent.Callable;


public class Firemaking implements InvUseOnGroundItemListener , InvUseOnItemListener , InvUseOnGroundItemExecutiveListener , InvUseOnItemExecutiveListener {
    private static final int TINDERBOX = ItemId.TINDERBOX.id();

    /**
     * LOG IDs
     */
    private static int[] LOGS = new int[]{ ItemId.LOGS.id(), ItemId.OAK_LOGS.id(), ItemId.WILLOW_LOGS.id(), ItemId.MAPLE_LOGS.id(), ItemId.YEW_LOGS.id(), ItemId.MAGIC_LOGS.id() };

    @Override
    public boolean blockInvUseOnGroundItem(Item myItem, GroundItem item, Player player) {
        return (myItem.getID() == Firemaking.TINDERBOX) && Functions.inArray(item.getID(), Firemaking.LOGS);
    }

    @Override
    public GameStateEvent onInvUseOnGroundItem(Item myItem, GroundItem item, Player player) {
        return new GameStateEvent(player.getWorld(), player, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    if (player.getWorld().getServer().getConfig().CUSTOM_FIREMAKING) {
                        switch (ItemId.getById(item.getID())) {
                            case LOGS :
                            case OAK_LOGS :
                            case WILLOW_LOGS :
                            case MAPLE_LOGS :
                            case YEW_LOGS :
                            case MAGIC_LOGS :
                                handleCustomFiremaking(item, player);
                                break;
                            default :
                                player.message("Nothing interesting happens");
                        }
                    } else {
                        if (item.getID() == ItemId.LOGS.id()) {
                            handleFiremaking(item, player);
                        } else
                            player.message("Nothing interesting happens");

                    }
                    return null;
                });
            }
        };
    }

    private void handleFiremaking(final GroundItem gItem, Player player) {
        final FiremakingDef def = player.getWorld().getServer().getEntityHandler().getFiremakingDef(gItem.getID());
        if (def == null) {
            player.message("Nothing interesting happens.");
            return;
        }
        if (player.getViewArea().getGameObject(gItem.getLocation()) != null) {
            player.playerServerMessage(MessageType.QUEST, "You can't light a fire here");
            return;
        }
        player.getUpdateFlags().setActionBubble(new Bubble(player, Firemaking.TINDERBOX));
        player.playerServerMessage(MessageType.QUEST, "You attempt to light the logs");
        player.setBatchEvent(new BatchEvent(player.getWorld(), player, 1200, "Normal Firemaking Logs Lit", Formulae.getRepeatTimes(player, Skills.FIREMAKING), false) {
            @Override
            public void action() {
                if (Formulae.lightLogs(getOwner().getSkills().getLevel(Skills.FIREMAKING))) {
                    getOwner().getWorld().getServer().getGameEventHandler().add(new SingleEvent(getOwner().getWorld(), getOwner(), 1200, "Light Logs") {
                        @Override
                        public void action() {
                            getOwner().playerServerMessage(MessageType.QUEST, "The fire catches and the logs begin to burn");
                            getWorld().unregisterItem(gItem);
                            final GameObject fire = new GameObject(getWorld(), gItem.getLocation(), 97, 0, 0);
                            getWorld().registerGameObject(fire);
                            getWorld().getServer().getGameEventHandler().add(new SingleEvent(getWorld(), null, def.getLength(), "Light Logs Fire Removal") {
                                @Override
                                public void action() {
                                    if (fire != null) {
                                        getWorld().registerItem(new GroundItem(getWorld(), ItemId.ASHES.id(), fire.getX(), fire.getY(), 1, ((Player) (null))));
                                        getWorld().unregisterGameObject(fire);
                                    }
                                }
                            });
                            getOwner().incExp(Skills.FIREMAKING, getExp(getOwner().getSkills().getMaxStat(Skills.FIREMAKING), 25), true);
                        }
                    });
                } else {
                    getOwner().playerServerMessage(MessageType.QUEST, "You fail to light a fire");
                    getOwner().getUpdateFlags().setActionBubble(new Bubble(getOwner(), Firemaking.TINDERBOX));
                }
            }
        });
    }

    private void handleCustomFiremaking(final GroundItem gItem, Player player) {
        final FiremakingDef def = player.getWorld().getServer().getEntityHandler().getFiremakingDef(gItem.getID());
        if (def == null) {
            player.message("Nothing interesting happens");
            return;
        }
        if (player.getSkills().getLevel(Skills.FIREMAKING) < def.getRequiredLevel()) {
            player.message(("You need at least " + def.getRequiredLevel()) + " firemaking to light these logs");
            return;
        }
        if (player.getViewArea().getGameObject(gItem.getLocation()) != null) {
            player.playerServerMessage(MessageType.QUEST, "You can't light a fire here");
            return;
        }
        player.getUpdateFlags().setActionBubble(new Bubble(player, Firemaking.TINDERBOX));
        player.playerServerMessage(MessageType.QUEST, "You attempt to light the logs");
        player.setBatchEvent(new BatchEvent(player.getWorld(), player, 1200, "Firemaking Logs Lit", Formulae.getRepeatTimes(player, Skills.FIREMAKING), false) {
            @Override
            public void action() {
                if (Formulae.lightCustomLogs(def, getOwner().getSkills().getLevel(Skills.FIREMAKING))) {
                    getOwner().message("The fire catches and the logs begin to burn");
                    getWorld().unregisterItem(gItem);
                    final GameObject fire = new GameObject(getWorld(), gItem.getLocation(), 97, 0, 0);
                    getWorld().registerGameObject(fire);
                    getWorld().getServer().getGameEventHandler().add(new SingleEvent(getWorld(), null, def.getLength(), "Firemaking Logs Lit") {
                        @Override
                        public void action() {
                            if (fire != null) {
                                getWorld().registerItem(new GroundItem(player.getWorld(), ItemId.ASHES.id(), fire.getX(), fire.getY(), 1, ((Player) (null))));
                                getWorld().unregisterGameObject(fire);
                            }
                        }
                    });
                    getOwner().incExp(Skills.FIREMAKING, def.getExp(), true);
                    interrupt();
                    // Determine which direction to move
                    int xPos = getOwner().getX();
                    int yPos = getOwner().getY();
                    TileValue tile = getOwner().getWorld().getTile(xPos, yPos);
                    TileValue tileNear;
                    if ((tile.traversalMask & CollisionFlag.WEST_BLOCKED) == 0) {
                        tileNear = getOwner().getWorld().getTile(xPos + 1, yPos);
                        if (((tileNear != null) && ((tileNear.traversalMask & CollisionFlag.FULL_BLOCK) == 0)) && (getOwner().getViewArea().getGameObject(new Point(xPos + 1, yPos)) == null)) {
                            getOwner().walk(getOwner().getX() + 1, getOwner().getY());
                            return;
                        }
                    }
                    if ((tile.traversalMask & CollisionFlag.EAST_BLOCKED) == 0) {
                        tileNear = getOwner().getWorld().getTile(xPos - 1, yPos);
                        if (((tileNear != null) && ((tileNear.traversalMask & CollisionFlag.FULL_BLOCK) == 0)) && (getOwner().getViewArea().getGameObject(new Point(xPos - 1, yPos)) == null)) {
                            getOwner().walk(getOwner().getX() - 1, getOwner().getY());
                            return;
                        }
                    }
                    if ((tile.traversalMask & CollisionFlag.NORTH_BLOCKED) == 0) {
                        tileNear = getOwner().getWorld().getTile(xPos, yPos - 1);
                        if (((tileNear != null) && ((tileNear.traversalMask & CollisionFlag.FULL_BLOCK) == 0)) && (getOwner().getViewArea().getGameObject(new Point(xPos, yPos - 1)) == null)) {
                            getOwner().walk(getOwner().getX(), getOwner().getY() - 1);
                            return;
                        }
                    }
                    if ((tile.traversalMask & CollisionFlag.SOUTH_BLOCKED) == 0) {
                        tileNear = getOwner().getWorld().getTile(xPos, yPos + 1);
                        if (((tileNear != null) && ((tileNear.traversalMask & CollisionFlag.FULL_BLOCK) == 0)) && (getOwner().getViewArea().getGameObject(new Point(xPos, yPos + 1)) == null)) {
                            getOwner().walk(getOwner().getX(), getOwner().getY() + 1);
                            return;
                        }
                    }
                } else {
                    getOwner().playerServerMessage(MessageType.QUEST, "You fail to light a fire");
                    getOwner().getUpdateFlags().setActionBubble(new Bubble(getOwner(), Firemaking.TINDERBOX));
                }
            }
        });
    }

    @Override
    public boolean blockInvUseOnItem(Player player, Item item1, Item item2) {
        return Functions.compareItemsIds(item1, item2, Firemaking.TINDERBOX, ItemId.LOGS.id()) || (player.getWorld().getServer().getConfig().CUSTOM_FIREMAKING && (((item1.getID() == Firemaking.TINDERBOX) && Functions.inArray(item2.getID(), Firemaking.LOGS)) || ((item2.getID() == Firemaking.TINDERBOX) && Functions.inArray(item1.getID(), Firemaking.LOGS))));
    }

    @Override
    public GameStateEvent onInvUseOnItem(Player player, Item item1, Item item2) {
        return new GameStateEvent(player.getWorld(), player, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    if (((item1.getID() == Firemaking.TINDERBOX) && Functions.inArray(item2.getID(), Firemaking.LOGS)) || ((item2.getID() == Firemaking.TINDERBOX) && Functions.inArray(item1.getID(), Firemaking.LOGS))) {
                        player.playerServerMessage(MessageType.QUEST, "I think you should put the logs down before you light them!");
                    }
                    return null;
                });
            }
        };
    }

    public int getExp(int level, int baseExp) {
        return ((int) ((baseExp + (level * 1.75)) * 4));
    }
}

