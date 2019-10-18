package com.openrsc.server.plugins.quests.members.grandtree;


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
import com.openrsc.server.util.rsc.DataConversions;
import java.util.concurrent.Callable;


public class GnomeGlider implements TalkToNpcListener , TalkToNpcExecutiveListener {
    @Override
    public boolean blockTalkToNpc(Player p, Npc n) {
        return DataConversions.inArray(new int[]{ NpcId.GNOME_PILOT_GRANDTREE.id(), NpcId.GNOME_PILOT_KARAMJA_BROKEN.id(), NpcId.GNOME_PILOT_KARAMJA.id(), NpcId.GNOME_PILOT_VARROCK.id(), NpcId.GNOME_PILOT_ALKHARID.id(), NpcId.GNOME_PILOT_WHITEMOUNTAIN.id() }, n.getID());
    }

    @Override
    public GameStateEvent onTalkToNpc(Player p, Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    if (n.getID() == NpcId.GNOME_PILOT_VARROCK.id()) {
                        Functions.___playerTalk(p, n, "hello again");
                        Functions.___npcTalk(p, n, "well hello adventurer", "as you can see we crashed on impact", "i don't think it'll fly again", "sorry but you'll have to walk");
                    } else
                        if (((n.getID() == NpcId.GNOME_PILOT_KARAMJA.id()) || (n.getID() == NpcId.GNOME_PILOT_ALKHARID.id())) || (n.getID() == NpcId.GNOME_PILOT_WHITEMOUNTAIN.id())) {
                            if (p.getQuestStage(Quests.GRAND_TREE) == (-1)) {
                                Functions.___playerTalk(p, n, "hello again");
                                Functions.___npcTalk(p, n, "well hello adventurer");
                                Functions.___npcTalk(p, n, "would you like to go to the tree gnome stronghold?");
                                int travelBackMenu = Functions.___showMenu(p, n, "ok then", "no thanks");
                                if (travelBackMenu == 0) {
                                    Functions.___npcTalk(p, n, "ok, hold on tight");
                                    Functions.___message(p, "you both hold onto the wooden beam", "you take a few steps backand rush forwards", "the glider just lifts of the ground");
                                    p.teleport(221, 3567);
                                    Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                    p.teleport(414, 2995);
                                }
                                return null;
                            }
                            Functions.___playerTalk(p, n, "hello");
                            Functions.___npcTalk(p, n, "hello traveller");
                        } else
                            if (n.getID() == NpcId.GNOME_PILOT_GRANDTREE.id()) {
                                Functions.___playerTalk(p, n, "hello");
                                if (p.getQuestStage(Quests.GRAND_TREE) == (-1)) {
                                    Functions.___npcTalk(p, n, "well hello again traveller");
                                    Functions.___npcTalk(p, n, "can i take you somewhere?");
                                    Functions.___npcTalk(p, n, "i can fly like the birds");
                                    int menu = // do not send over
                                    Functions.___showMenu(p, n, false, "karamja", "varrock", "Al kharid", "white wolf mountain", "I'll stay here thanks");
                                    if (menu == 0) {
                                        Functions.___playerTalk(p, n, "take me to karamja");
                                        Functions.___npcTalk(p, n, "ok, your the boss, jump on", "hold on tight, it'll be a rough ride");
                                        Functions.___message(p, "you hold on tight to the glider's wooden beam", "the pilot leans back and then pushes the glider forward", "you float softly off the grand tree");
                                        p.teleport(221, 3567);
                                        Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                        p.teleport(389, 753);
                                        Functions.___playerTalk(p, n, "ouch");
                                    } else
                                        if (menu == 1) {
                                            Functions.___playerTalk(p, n, "take me to Varrock");
                                            Functions.___npcTalk(p, n, "ok, your the boss, jump on", "hold on tight, it'll be a rough ride");
                                            Functions.___message(p, "you hold on tight to the glider's wooden beam", "the pilot leans back and then pushes the glider forward", "you float softly off the grand tree");
                                            p.teleport(221, 3567);
                                            Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                            p.teleport(58, 504);
                                            Functions.___playerTalk(p, n, "ouch");
                                        } else
                                            if (menu == 2) {
                                                Functions.___playerTalk(p, n, "take me to Al kharid");
                                                Functions.___npcTalk(p, n, "ok, your the boss, jump on", "hold on tight, it'll be a rough ride");
                                                Functions.___message(p, "you hold on tight to the glider's wooden beam", "the pilot leans back and then pushes the glider forward", "you float softly off the grand tree");
                                                p.teleport(221, 3567);
                                                Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                                p.teleport(88, 664);
                                                Functions.___playerTalk(p, n, "ouch");
                                            } else
                                                if (menu == 3) {
                                                    Functions.___playerTalk(p, n, "take me to White wolf mountain");
                                                    Functions.___npcTalk(p, n, "ok, your the boss, jump on", "hold on tight, it'll be a rough ride");
                                                    Functions.___message(p, "you hold on tight to the glider's wooden beam", "the pilot leans back and then pushes the glider forward", "you float softly off the grand tree");
                                                    p.teleport(221, 3567);
                                                    Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                                    p.teleport(400, 461);
                                                    Functions.___playerTalk(p, n, "ouch");
                                                } else
                                                    if (menu == 4) {
                                                        Functions.___playerTalk(p, n, "i'll stay here thanks");
                                                        Functions.___npcTalk(p, n, "no worries, let me know if you change your mind");
                                                    }




                                    return null;
                                } else
                                    if ((p.getQuestStage(Quests.GRAND_TREE) >= 8) && (p.getQuestStage(Quests.GRAND_TREE) <= 9)) {
                                        Functions.___npcTalk(p, n, "hi, the king said that you need to leave");
                                        Functions.___playerTalk(p, n, "yes, apparently humans are invading");
                                        Functions.___npcTalk(p, n, "i find that hard to believe", "i have lots of human friends");
                                        Functions.___playerTalk(p, n, "it seems a bit strange to me");
                                        Functions.___npcTalk(p, n, "well, would you like me to take you somewhere?");
                                        int menu = Functions.___showMenu(p, n, "actually yes, take me to karamja", "no thanks i'm going to hang around");
                                        if (menu == 0) {
                                            Functions.___npcTalk(p, n, "ok, your the boss, jump on", "hold on tight, it'll be a rough ride");
                                            Functions.___message(p, "you hold on tight to the glider's wooden beam", "the pilot leans back and then pushes the glider forward", "you float softly off the grand tree");
                                            p.teleport(221, 3567);
                                            Functions.___playerTalk(p, n, "whhaaaaaaaaaagghhh");
                                            p.teleport(425, 764);
                                            Functions.___playerTalk(p, n, "ouch");
                                            Npc GNOME_PILOT = Functions.getNearestNpc(p, NpcId.GNOME_PILOT_KARAMJA_BROKEN.id(), 5);
                                            Functions.___npcTalk(p, GNOME_PILOT, "ouch");
                                            p.message("you crash in south karamja");
                                            Functions.___npcTalk(p, GNOME_PILOT, "sorry about that, are you ok");
                                            Functions.___playerTalk(p, GNOME_PILOT, "i seem to be fine, can't say the same for your glider");
                                            Functions.___npcTalk(p, GNOME_PILOT, "i don't think i can fix this", "looks like we'll be heading back by foot", "i hope you find what you came for adventurer");
                                            Functions.___playerTalk(p, GNOME_PILOT, "me too, take care little man");
                                            Functions.___npcTalk(p, GNOME_PILOT, "traveller watch out");
                                            Npc JOGRE = Functions.getNearestNpc(p, NpcId.JOGRE.id(), 15);
                                            if (JOGRE != null) {
                                                Functions.___npcTalk(p, JOGRE, "grrrrr");
                                                JOGRE.setChasing(p);
                                            }
                                        } else
                                            if (menu == 1) {
                                                Functions.___npcTalk(p, n, "ok, i'll be here if you need me");
                                            }

                                        return null;
                                    }

                                Functions.___npcTalk(p, n, "hello traveller");
                            } else
                                if (n.getID() == NpcId.GNOME_PILOT_KARAMJA_BROKEN.id()) {
                                    p.message("The Gnome pilot does not appear interested in talking");
                                }



                    return null;
                });
            }
        };
    }
}

