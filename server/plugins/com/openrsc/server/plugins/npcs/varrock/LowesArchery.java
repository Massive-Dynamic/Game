package com.openrsc.server.plugins.npcs.varrock;


import com.openrsc.server.constants.ItemId;
import com.openrsc.server.constants.NpcId;
import com.openrsc.server.event.rsc.GameStateEvent;
import com.openrsc.server.model.Shop;
import com.openrsc.server.model.container.Item;
import com.openrsc.server.model.entity.Mob;
import com.openrsc.server.model.entity.npc.Npc;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.model.world.World;
import com.openrsc.server.net.rsc.ActionSender;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.ShopInterface;
import com.openrsc.server.plugins.listeners.action.TalkToNpcListener;
import com.openrsc.server.plugins.listeners.executive.TalkToNpcExecutiveListener;
import java.util.concurrent.Callable;


public final class LowesArchery implements ShopInterface , TalkToNpcListener , TalkToNpcExecutiveListener {
    private final Shop shop = new Shop(false, 3000, 100, 55, 1, new Item(ItemId.BRONZE_ARROWS.id(), 200), new Item(ItemId.CROSSBOW_BOLTS.id(), 150), new Item(ItemId.SHORTBOW.id(), 4), new Item(ItemId.LONGBOW.id(), 2), new Item(ItemId.CROSSBOW.id(), 2));

    @Override
    public boolean blockTalkToNpc(final Player p, final Npc n) {
        return n.getID() == NpcId.LOWE.id();
    }

    @Override
    public Shop[] getShops(World world) {
        return new Shop[]{ shop };
    }

    @Override
    public boolean isMembers() {
        return false;
    }

    @Override
    public GameStateEvent onTalkToNpc(final Player p, final Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    Functions.___npcTalk(p, n, "Welcome to Lowe's Archery Store", "Do you want to see my wares?");
                    int option = // do not send over
                    Functions.___showMenu(p, n, false, "Yes please", "No, I prefer to bash things close up");
                    if (option == 0) {
                        Functions.___playerTalk(p, n, "Yes Please");
                        p.setAccessingShop(shop);
                        ActionSender.showShop(p, shop);
                    } else
                        if (option == 1) {
                            Functions.___playerTalk(p, n, "No, I prefer to bash things close up");
                        }

                    return null;
                });
            }
        };
    }
}

