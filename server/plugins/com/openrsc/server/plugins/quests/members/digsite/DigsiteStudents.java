package com.openrsc.server.plugins.quests.members.digsite;


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
import java.util.concurrent.Callable;


public class DigsiteStudents implements TalkToNpcListener , TalkToNpcExecutiveListener {
    @Override
    public boolean blockTalkToNpc(Player p, Npc n) {
        return ((n.getID() == NpcId.STUDENT_ORANGE.id()) || (n.getID() == NpcId.STUDENT_PURPLE.id())) || (n.getID() == NpcId.STUDENT_GREEN.id());
    }

    /**
     * Rock sample: 1149 = PURPLE STUDENT
     */
    @Override
    public GameStateEvent onTalkToNpc(Player p, Npc n) {
        return new GameStateEvent(p.getWorld(), p, 0, (getClass().getSimpleName() + " ") + Thread.currentThread().getStackTrace()[1].getMethodName()) {
            public void init() {
                addState(0, () -> {
                    if (n.getID() == NpcId.STUDENT_ORANGE.id()) {
                        switch (p.getQuestStage(Quests.DIGSITE)) {
                            case 0 :
                            case 1 :
                                Functions.___playerTalk(p, n, "Hello there");
                                Functions.___npcTalk(p, n, "Hello there, as you can see I am a student");
                                Functions.___playerTalk(p, n, "What are you doing here ?");
                                Functions.___npcTalk(p, n, "Oh I'm studying for the earth sciences exam");
                                Functions.___playerTalk(p, n, "Interesting....perhaps I should study it as well...");
                                break;
                            case 2 :
                                Functions.___playerTalk(p, n, "Hello there");
                                if (p.getCache().hasKey("student_orange_c")) {
                                    // completed orange student help
                                    Functions.___npcTalk(p, n, "How's it going ?");
                                    Functions.___playerTalk(p, n, "There are more exam questions I'm stuck on");
                                    Functions.___npcTalk(p, n, "Hey, i'll tell you what I've learned, that may help", "The elligible people to use the digsite are:", "All that have passed the appropriate earth sciences exams");
                                    Functions.___playerTalk(p, n, "Thanks for the information");
                                } else
                                    if (p.getCache().hasKey("student_orange_s")) {
                                        // started orange student help
                                        if (Functions.hasItem(p, ItemId.ROCK_SAMPLE_ORANGE.id())) {
                                            Functions.___playerTalk(p, n, "Look what I found");
                                            Functions.removeItem(p, ItemId.ROCK_SAMPLE_ORANGE.id(), 1);
                                            p.getCache().store("student_orange_c", true);// store completed orange student help

                                            p.getCache().remove("student_orange_s");// remove started orange student help

                                            Functions.___npcTalk(p, n, "Excellent!", "I'm so happy", "Let me now help you with your exams...", "The elligible people to use the digsite are:", "All that have passed the appropriate earth sciences exams");
                                            Functions.___playerTalk(p, n, "Thanks for the information");
                                        } else {
                                            Functions.___playerTalk(p, n, "How's the study going ?");
                                            Functions.___npcTalk(p, n, "I'm getting there", "Have you found my rock sample yet ?");
                                            Functions.___playerTalk(p, n, "No sorry, not yet");
                                            Functions.___npcTalk(p, n, "Oh dear, I hope it didn't fall into the stream", "I might never find it again...");
                                        }
                                    } else {
                                        Functions.___playerTalk(p, n, "Can you help me with the earth sciences exams at all?");
                                        Functions.___npcTalk(p, n, "I can't do anything unless I find my rock sample");
                                        Functions.___playerTalk(p, n, "Hey this rings a bell");
                                        Functions.___npcTalk(p, n, "?");
                                        Functions.___playerTalk(p, n, "So if I find it you'll help me ?");
                                        Functions.___npcTalk(p, n, "I sure will");
                                        Functions.___playerTalk(p, n, "Any ideas where it may be ?");
                                        Functions.___npcTalk(p, n, "All I remember is that I was working near the tents when I lost it...");
                                        Functions.___playerTalk(p, n, "Okay I'll see what I can do ");
                                        p.getCache().store("student_orange_s", true);// started orange student help

                                    }

                                break;
                            case 3 :
                                Functions.___playerTalk(p, n, "Hello there");
                                Functions.___npcTalk(p, n, "How's it going ?");
                                Functions.___playerTalk(p, n, "There are more exam questions I'm stuck on");
                                Functions.___npcTalk(p, n, "Hey, i'll tell you what I've learned, that may help", "Correct sample transportation:", "Samples taken in rough form, kept only in sealed containers");
                                Functions.___playerTalk(p, n, "Thanks for the information");
                                if (!p.getCache().hasKey("student_orange_exam2")) {
                                    p.getCache().store("student_orange_exam2", true);
                                }
                                break;
                            case 4 :
                                Functions.___playerTalk(p, n, "Hello there");
                                Functions.___npcTalk(p, n, "How's it going ?");
                                Functions.___playerTalk(p, n, "There are more exam questions I'm stuck on");
                                Functions.___npcTalk(p, n, "Hey, i'll tell you what I've learned, that may help", "The proper technique for handling bones is:", "Handle bones very carefully, and keep away from other samples");
                                Functions.___playerTalk(p, n, "Thanks for the information");
                                if (!p.getCache().hasKey("student_orange_exam3")) {
                                    p.getCache().store("student_orange_exam3", true);
                                }
                                break;
                            case 5 :
                                Functions.___playerTalk(p, n, "Hello there");
                                Functions.___npcTalk(p, n, "Thanks a lot for finding my rock sample", "See you again");
                                break;
                            case 6 :
                            case -1 :
                                Functions.___npcTalk(p, n, "Hey it's the great explorer!", "Well done for finding the altar");
                                break;
                        }
                    } else
                        if (n.getID() == NpcId.STUDENT_GREEN.id()) {
                            switch (p.getQuestStage(Quests.DIGSITE)) {
                                case 0 :
                                case 1 :
                                    Functions.___playerTalk(p, n, "Hello there");
                                    Functions.___npcTalk(p, n, "Oh hi, i'm studying hard for an exam");
                                    Functions.___playerTalk(p, n, "What exam is that ?");
                                    Functions.___npcTalk(p, n, "It's the earth sciences exam");
                                    Functions.___playerTalk(p, n, "Interesting....");
                                    break;
                                case 2 :
                                    Functions.___playerTalk(p, n, "Hello there");
                                    if (p.getCache().hasKey("student_green_c")) {
                                        Functions.___npcTalk(p, n, "How's it going ?");
                                        Functions.___playerTalk(p, n, "I need more help with the exam");
                                        Functions.___npcTalk(p, n, "Well okay, this is what I have learned since I last spoke to you...", "The study of earthsciences is:", "The study of the earth, It's contents and It's history");
                                        Functions.___playerTalk(p, n, "Okay I'll remember that");
                                    } else
                                        if (p.getCache().hasKey("student_green_s")) {
                                            // started green student help
                                            if (Functions.hasItem(p, ItemId.ROCK_SAMPLE_GREEN.id())) {
                                                Functions.___playerTalk(p, n, "Hi, is this your rock sample ?");
                                                Functions.removeItem(p, ItemId.ROCK_SAMPLE_GREEN.id(), 1);
                                                p.getCache().store("student_green_c", true);// completed green student help

                                                p.getCache().remove("student_green_s");// remove started green student help

                                                Functions.___npcTalk(p, n, "Oh wow! you've found it!", "Thank you so much", "I'll be glad to tell you what I know about the exam", "The study of earthsciences is:", "The study of the earth, It's contents and It's history");
                                                Functions.___playerTalk(p, n, "Okay I'll remember that");
                                            } else {
                                                Functions.___playerTalk(p, n, "How's the study going ?");
                                                Functions.___npcTalk(p, n, "Very well thanks", "Have you found my rock sample yet ?");
                                                Functions.___playerTalk(p, n, "No sorry, not yet");
                                                Functions.___npcTalk(p, n, "Oh well...", "I am sure it's been picked up", "Couldn't you try looking through some pockets ?");
                                            }
                                        } else {
                                            Functions.___playerTalk(p, n, "Can you help me with the earth sciences exams at all?");
                                            Functions.___npcTalk(p, n, "Well...maybe I will if you help me with something");
                                            Functions.___playerTalk(p, n, "What's that ?");
                                            Functions.___npcTalk(p, n, "I have lost my rock sample");
                                            Functions.___playerTalk(p, n, "What does it look like ?");
                                            Functions.___npcTalk(p, n, "Err....like a rock!");
                                            Functions.___playerTalk(p, n, "Well that's not too helpful", "Can you remember where you last had it ?");
                                            Functions.___npcTalk(p, n, "It was around here for sure", "Maybe someone picked it up ?");
                                            Functions.___playerTalk(p, n, "Okay I'll have a look for you");
                                            p.getCache().store("student_green_s", true);// started green student help

                                        }

                                    break;
                                case 3 :
                                    Functions.___playerTalk(p, n, "Hello there");
                                    Functions.___npcTalk(p, n, "How's it going ?");
                                    Functions.___playerTalk(p, n, "I need more help with the exam");
                                    Functions.___npcTalk(p, n, "Well okay, this is what I have learned since I last spoke to you...", "Correct rockpick usage:", "Always handle with care, strike the rock cleanly on it's cleaving point");
                                    Functions.___playerTalk(p, n, "Okay I'll remember that");
                                    if (!p.getCache().hasKey("student_green_exam2")) {
                                        p.getCache().store("student_green_exam2", true);
                                    }
                                    break;
                                case 4 :
                                    Functions.___playerTalk(p, n, "Hello there");
                                    Functions.___npcTalk(p, n, "How's it going ?");
                                    Functions.___playerTalk(p, n, "I need more help with the exam");
                                    Functions.___npcTalk(p, n, "Well okay, this is what I have learned since I last spoke to you...", "Specimen brush use:", "Brush carefully and slowly, using short strokes");
                                    Functions.___playerTalk(p, n, "Okay I'll remember that");
                                    if (!p.getCache().hasKey("student_green_exam3")) {
                                        p.getCache().store("student_green_exam3", true);
                                    }
                                    break;
                                case 5 :
                                    Functions.___playerTalk(p, n, "Hello there");
                                    Functions.___npcTalk(p, n, "Thanks for your help, I'll pass these exams yet!", "See you later");
                                    break;
                                case 6 :
                                case -1 :
                                    Functions.___npcTalk(p, n, "Oh hi again", "News of your find has spread fast", "You are quite famous around here now");
                                    break;
                            }
                        } else
                            if (n.getID() == NpcId.STUDENT_PURPLE.id()) {
                                switch (p.getQuestStage(Quests.DIGSITE)) {
                                    case 0 :
                                    case 1 :
                                        Functions.___playerTalk(p, n, "Hello there");
                                        Functions.___npcTalk(p, n, "Hi there, I'm studying for the earth sciences exam");
                                        Functions.___playerTalk(p, n, "Interesting....This exam seems to be a popular one!");
                                        break;
                                    case 2 :
                                        Functions.___playerTalk(p, n, "Hello there");
                                        if (p.getCache().hasKey("student_purple_c")) {
                                            // completed purple student help
                                            Functions.___npcTalk(p, n, "How's it going ?");
                                            Functions.___playerTalk(p, n, "I am stuck on some more exam questions");
                                            Functions.___npcTalk(p, n, "Okay, I'll tell you my latest notes...", "The proper health and safety points are:", "Gloves and boots to be worn at all times, proper tools must be used");
                                            Functions.___playerTalk(p, n, "Great, thanks for your advice");
                                        } else
                                            if (p.getCache().hasKey("student_purple_s")) {
                                                // started purple student help
                                                if (Functions.hasItem(p, ItemId.ROCK_SAMPLE_PURPLE.id())) {
                                                    Functions.___playerTalk(p, n, "Guess what I found ?");
                                                    Functions.removeItem(p, ItemId.ROCK_SAMPLE_PURPLE.id(), 1);
                                                    p.getCache().store("student_purple_c", true);// completed purple student help

                                                    p.getCache().remove("student_purple_s");// remove started purple student help

                                                    Functions.___npcTalk(p, n, "Hey! my sample!", "Thanks ever so much", "Let me help you with those questions now", "The proper health and safety points are:", "Gloves and boots to be worn at all times, proper tools must be used");
                                                    Functions.___playerTalk(p, n, "Great, thanks for your advice");
                                                } else {
                                                    Functions.___playerTalk(p, n, "How's the study going ?");
                                                    Functions.___npcTalk(p, n, "Very well thanks", "Have you found my rock sample yet ?");
                                                    Functions.___playerTalk(p, n, "No sorry, not yet");
                                                    Functions.___npcTalk(p, n, "I'm sure it's just outside the digsite somewhere...");
                                                }
                                            } else {
                                                Functions.___playerTalk(p, n, "Can you help me with the exams at all?");
                                                Functions.___npcTalk(p, n, "I can if you help me...");
                                                Functions.___playerTalk(p, n, "How can I do that");
                                                Functions.___npcTalk(p, n, "I have lost my rock sample");
                                                Functions.___playerTalk(p, n, "What you as well ?");
                                                Functions.___npcTalk(p, n, "Err, yes it's gone somewhere");
                                                Functions.___playerTalk(p, n, "Do you know where you dropped it ?");
                                                Functions.___npcTalk(p, n, "Well, I was doing a lot of walking that day...", "Oh yes, that's right...", "We were studying ceramics in fact", "I found some pottery...", "And it seemed to match the design that is on those large urns...", "...I was in the process of checking this out", "And when we got back to the centre...", "My rock sample had gone");
                                                Functions.___playerTalk(p, n, "Leave it to me, I'll find it");
                                                Functions.___npcTalk(p, n, "Oh great!");
                                                p.getCache().store("student_purple_s", true);// started purple student help

                                            }

                                        break;
                                    case 3 :
                                        Functions.___playerTalk(p, n, "Hello there");
                                        Functions.___npcTalk(p, n, "How's it going ?");
                                        Functions.___playerTalk(p, n, "I am stuck on some more exam questions");
                                        Functions.___npcTalk(p, n, "Okay, I'll tell you my latest notes...", "Finds handling:", "Finds must be carefully handled, and gloves worn");
                                        Functions.___playerTalk(p, n, "Great, thanks for your advice");
                                        if (!p.getCache().hasKey("student_purple_exam2")) {
                                            p.getCache().store("student_purple_exam2", true);
                                        }
                                        break;
                                    case 4 :
                                        if (p.getCache().hasKey("student_purple_exam3")) {
                                            Functions.___playerTalk(p, n, "Hello there");
                                            Functions.___npcTalk(p, n, "Hi, the opal looks magnificent", "Thanks for everything you've done for me");
                                        } else
                                            if (p.getCache().hasKey("student_purple_opal")) {
                                                Functions.___playerTalk(p, n, "Hello there");
                                                Functions.___npcTalk(p, n, "Oh hi again", "Did you bring me the opal ?");
                                                if (Functions.hasItem(p, ItemId.UNCUT_OPAL.id())) {
                                                    // OPAL
                                                    Functions.___playerTalk(p, n, "Would that opal look like this by any chance ?");
                                                    Functions.removeItem(p, ItemId.UNCUT_OPAL.id(), 1);
                                                    p.getCache().store("student_purple_exam3", true);// completed purple student help

                                                    p.getCache().remove("student_purple_opal");// remove started purple student help

                                                    Functions.___npcTalk(p, n, "Wow, great you've found one", "This will look beautiful set in my necklace", "Thanks for that, now I'll tell you what I know...", "Sample preparation:", "Samples cleaned and carried only in specimen jars");
                                                    Functions.___playerTalk(p, n, "Great, thanks for your advice");
                                                } else {
                                                    Functions.___playerTalk(p, n, "I haven't found one yet");
                                                    Functions.___npcTalk(p, n, "Oh well, tell me when you do");
                                                }
                                            } else {
                                                Functions.___playerTalk(p, n, "Hello there");
                                                Functions.___npcTalk(p, n, "What, you want more help ?");
                                                Functions.___playerTalk(p, n, "Err... yes please!");
                                                Functions.___npcTalk(p, n, "Well.. it's going to cost you...");
                                                Functions.___playerTalk(p, n, "Oh, well how much ?");
                                                Functions.___npcTalk(p, n, "I'll tell you what I would like...", "A precious stone, I don't find many of these", "My favourite is an opal, they are beautiful", "...Just like me", "Tee hee hee !");
                                                Functions.___playerTalk(p, n, "Err... okay I'll see what I can do");
                                                if (!p.getCache().hasKey("student_purple_opal")) {
                                                    p.getCache().store("student_purple_opal", true);
                                                }
                                            }

                                        break;
                                    case 5 :
                                        Functions.___playerTalk(p, n, "Hello there");
                                        Functions.___npcTalk(p, n, "Thanks for your help, I'll pass these exams yet!", "See you later");
                                        break;
                                    case 6 :
                                    case -1 :
                                        Functions.___npcTalk(p, n, "Hi there", "Thanks again, hey maybe I'll be asking you", "For help next time...", "It seems you are something of an expert now !");
                                        break;
                                }
                            }


                    return null;
                });
            }
        };
    }
}

