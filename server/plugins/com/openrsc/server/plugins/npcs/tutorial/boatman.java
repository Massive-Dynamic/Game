package com.openrsc.server.plugins.npcs.tutorial;


import com.openrsc.server.constants.NpcId;
import com.openrsc.server.event.rsc.GameStateEvent;
import com.openrsc.server.model.entity.Mob;
import com.openrsc.server.model.entity.npc.Npc;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.model.world.World;
import com.openrsc.server.net.rsc.ActionSender;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.listeners.action.TalkToNpcListener;
import com.openrsc.server.plugins.listeners.executive.TalkToNpcExecutiveListener;
import java.util.concurrent.Callable;


public class boatman implements TalkToNpcListener , TalkToNpcExecutiveListener {
    /**
     *
     *
     * @author Davve
    Tutorial island boat man - last npc before main land (Lumbridge)
     */
    @Override
    public GameStateEvent onTalkToNpc(Player p, Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    Functions.___npcTalk(p, n, "Hello my job is to take you to the main game area", "It's only a short row", "I shall take you to the small town of Lumbridge", "In the kingdom of Misthalin");
                    int menu = Functions.___showMenu(p, n, "Ok I'm ready to go", "I'm not done here yet");
                    if (menu == 0) {
                        Functions.___npcTalk(p, n, "Lets go then");
                        p.message("You have completed the tutorial");
                        p.teleport(120, 648, false);
                        if (p.getCache().hasKey("tutorial")) {
                            p.getCache().remove("tutorial");
                        }
                        Functions.sleep(2000);
                        p.message("The boat arrives in Lumbridge");
                        p.getWorld().sendWorldAnnouncement(("New adventurer @gre@" + p.getUsername()) + "@whi@ has arrived in lumbridge!");
                        ActionSender.sendPlayerOnTutorial(p);
                    } else
                        if (menu == 1) {
                            Functions.___npcTalk(p, n, "Ok come back when you are ready");
                        }

                    return null;
                });
            }
        };
    }

    @Override
    public boolean blockTalkToNpc(Player p, Npc n) {
        return n.getID() == NpcId.BOATMAN.id();
    }
}

