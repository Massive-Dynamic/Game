package com.openrsc.server.plugins.npcs.varrock;


import com.openrsc.server.constants.ItemId;
import com.openrsc.server.constants.NpcId;
import com.openrsc.server.constants.Quests;
import com.openrsc.server.event.rsc.GameStateEvent;
import com.openrsc.server.model.entity.Mob;
import com.openrsc.server.model.entity.npc.Npc;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.model.world.World;
import com.openrsc.server.plugins.Functions;
import com.openrsc.server.plugins.listeners.action.TalkToNpcListener;
import com.openrsc.server.plugins.listeners.executive.TalkToNpcExecutiveListener;
import com.openrsc.server.plugins.quests.free.ShieldOfArrav;
import java.util.concurrent.Callable;


public class King implements TalkToNpcListener , TalkToNpcExecutiveListener {
    @Override
    public boolean blockTalkToNpc(Player p, Npc n) {
        return n.getID() == NpcId.KING.id();
    }

    @Override
    public GameStateEvent onTalkToNpc(Player p, Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    if (Functions.hasItem(p, ItemId.CERTIFICATE.id())) {
                        Functions.___playerTalk(p, n, "Your majesty", "I have come to claim the reward", "For the return of the shield of Arrav");
                        if (p.getQuestStage(Quests.SHIELD_OF_ARRAV) == 5) {
                            Functions.___message(p, "You show the certificate to the king");
                            Functions.___npcTalk(p, n, "My goodness", "This is the claim for a reward put out by my father", "I never thought I'd see anyone claim this reward", "I see you are claiming half the reward", "So that would come to 600 gold coins");
                            Functions.___message(p, "You hand over a certificate", "The king gives you 600 coins");
                            Functions.removeItem(p, ItemId.CERTIFICATE.id(), 1);
                            p.sendQuestComplete(Quests.SHIELD_OF_ARRAV);
                            if (ShieldOfArrav.isBlackArmGang(p))
                                p.updateQuestStage(Quests.SHIELD_OF_ARRAV, -2);

                            return null;
                        } else
                            if (p.getQuestStage(Quests.SHIELD_OF_ARRAV) >= 0) {
                                Functions.___npcTalk(p, n, "The name on this certificate isn't yours!", "I can't give you the reward", "Unless you do the quest yourself");
                            } else {
                                Functions.___npcTalk(p, n, "You have already claimed the reward", "You can't claim it twice");
                                Functions.___message(p, "Why don't you give this certificate", "To whoever helped you get the shield");
                            }

                        return null;
                    } else
                        if (Functions.hasItem(p, ItemId.BROKEN_SHIELD_ARRAV_1.id()) && Functions.hasItem(p, ItemId.BROKEN_SHIELD_ARRAV_2.id())) {
                            Functions.___playerTalk(p, n, "Your majesty", "I have recovered the shield of Arrav", "I would like to claim the reward");
                            Functions.___npcTalk(p, n, "The shield of Arrav, eh?", "Yes, I do recall my father putting a reward out for that", "Very well", "Go get the authenticity of the shield verified", "By the curator at the museum", "And I will grant you your reward");
                            return null;
                        }

                    Functions.___playerTalk(p, n, "Greetings, your majesty");
                    Functions.___npcTalk(p, n, "Do you have anything of import to say?");
                    Functions.___playerTalk(p, n, "Not really");
                    Functions.___npcTalk(p, n, "You will have to excuse me then", "I am very busy", "I have a kingdom to run");
                    return null;
                });
            }
        };
    }
}

