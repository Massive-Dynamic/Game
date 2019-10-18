package com.openrsc.server.plugins.npcs.tutorial;


import com.openrsc.server.constants.NpcId;
import com.openrsc.server.event.rsc.GameStateEvent;
import com.openrsc.server.model.entity.Mob;
import com.openrsc.server.model.entity.npc.Npc;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.model.world.World;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.listeners.action.TalkToNpcListener;
import com.openrsc.server.plugins.listeners.executive.TalkToNpcExecutiveListener;
import java.util.concurrent.Callable;


public class CommunityInstructor implements TalkToNpcListener , TalkToNpcExecutiveListener {
    /**
     *
     *
     * @author Davve
    Tutorial island community instructor
     */
    @Override
    public GameStateEvent onTalkToNpc(Player p, Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    Functions.___npcTalk(p, n, "You're almost ready to go out into the main game area", "When you get out there", "You will be able to interact with thousands of other players");
                    int menu = Functions.___showMenu(p, n, "How can I communicate with other players?", "Are there rules on ingame behaviour?");
                    if (menu == 0) {
                        communicateDialogue(p, n);
                    } else
                        if (menu == 1) {
                            behaviourDialogue(p, n);
                        }

                    return null;
                });
            }
        };
    }

    private void communicateDialogue(Player p, Npc n) {
        Functions.___npcTalk(p, n, "typing in the game window will bring up chat", "Which players in the nearby area will be able to see", "If you want to speak to a particular friend anywhere in the game", "You will be able to select the smiley face icon", "then click to add a friend, and type in your friend's name", "If that player is logged in on the same world as you", "their name will go green", "If they are logged in on a different world their name will go yellow", "clicking on their name will allow you to send a message");
        int menu2 = Functions.___showMenu(p, n, "Are there rules on ingame behaviour?", "goodbye then");
        if (menu2 == 0) {
            behaviourDialogue(p, n);
        } else
            if (menu2 == 1) {
                Functions.___npcTalk(p, n, "Good luck");
                if (p.getCache().hasKey("tutorial") && (p.getCache().getInt("tutorial") < 100)) {
                    p.getCache().set("tutorial", 100);
                }
            }

    }

    private void behaviourDialogue(Player p, Npc n) {
        Functions.___npcTalk(p, n, "Yes you should read the rules of conduct on our front page", "To make sure you do nothing to get yourself banned", "but as general guide always try to be courteous to people in game", "Remember the people in the game are real people somewhere", "With real feelings", "If you go round being abusive or causing trouble", "your character could quickly be the one in trouble");
        int menu3 = Functions.___showMenu(p, n, "How can I communicate with other players?", "goodbye then");
        if (menu3 == 0) {
            communicateDialogue(p, n);
        } else
            if (menu3 == 1) {
                Functions.___npcTalk(p, n, "Good luck");
                if (p.getCache().hasKey("tutorial") && (p.getCache().getInt("tutorial") < 100)) {
                    p.getCache().set("tutorial", 100);
                }
            }

    }

    @Override
    public boolean blockTalkToNpc(Player p, Npc n) {
        return n.getID() == NpcId.COMMUNITY_INSTRUCTOR.id();
    }
}

