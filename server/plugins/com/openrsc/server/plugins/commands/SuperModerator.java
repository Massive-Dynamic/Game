package com.openrsc.server.plugins.commands;

import com.openrsc.server.database.GameDatabaseException;
import com.openrsc.server.database.impl.mysql.queries.logging.StaffLog;
import com.openrsc.server.database.struct.NpcDrop;
import com.openrsc.server.external.ItemDropDef;
import com.openrsc.server.external.NPCDef;
import com.openrsc.server.model.Point;
import com.openrsc.server.model.entity.player.Player;
import com.openrsc.server.net.rsc.ActionSender;
import com.openrsc.server.plugins.listeners.action.CommandListener;
import com.openrsc.server.plugins.listeners.executive.CommandExecutiveListener;
import com.openrsc.server.util.rsc.DataConversions;
import com.openrsc.server.util.rsc.StringUtil;

import java.text.DateFormat;
import java.util.*;

import static com.openrsc.server.plugins.commands.Event.LOGGER;

public final class SuperModerator implements CommandListener, CommandExecutiveListener {

	public static String messagePrefix = null;
	public static String badSyntaxPrefix = null;

	public boolean blockCommand(String cmd, String[] args, Player player) {
		return player.isSuperMod();
	}

	@Override
	public void onCommand(String cmd, String[] args, Player player) {
		if(messagePrefix == null) {
			messagePrefix = player.getWorld().getServer().getConfig().MESSAGE_PREFIX;
		}
		if(badSyntaxPrefix == null) {
			badSyntaxPrefix = player.getWorld().getServer().getConfig().BAD_SYNTAX_PREFIX;
		}

		if (cmd.equalsIgnoreCase("setcache") || cmd.equalsIgnoreCase("scache") || cmd.equalsIgnoreCase("storecache")) {
			if (args.length < 2) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " (name) [cache_key] [cache_value]");
				return;
			}

			int keyArg = args.length >= 3 ? 1 : 0;
			int valArg = args.length >= 3 ? 2 : 1;

			Player p = args.length >= 3 ?
				player.getWorld().getPlayer(DataConversions.usernameToHash(args[0])) :
				player;

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not modify cache of a staff member of equal or greater rank.");
				return;
			}

			if (args[keyArg].equals("invisible")) {
				player.message(messagePrefix + "Can not change that cache value. Use ::invisible instead.");
				return;
			}

			if (args[keyArg].equals("invulnerable")) {
				player.message(messagePrefix + "Can not change that cache value. Use ::invulnerable instead.");
				return;
			}

			if (p.getCache().hasKey(args[keyArg])) {
				player.message(messagePrefix + p.getUsername() + " already has that setting set.");
				return;
			}

			try {
				boolean value = DataConversions.parseBoolean(args[valArg]);
				args[valArg] = value ? "1" : "0";
			} catch (NumberFormatException ex) {
			}

			p.getCache().store(args[keyArg], args[valArg]);
			player.message(messagePrefix + "Added " + args[keyArg] + " with value " + args[valArg] + " to " + p.getUsername() + "'s cache");
		} else if (cmd.equalsIgnoreCase("getcache") || cmd.equalsIgnoreCase("gcache") || cmd.equalsIgnoreCase("checkcache")) {
			if (args.length < 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " (name) [cache_key]");
				return;
			}

			int keyArg = args.length >= 2 ? 1 : 0;

			Player p = args.length >= 2 ?
				player.getWorld().getPlayer(DataConversions.usernameToHash(args[0])) :
				player;

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.getCache().hasKey(args[keyArg])) {
				player.message(messagePrefix + p.getUsername() + " does not have the cache key " + args[keyArg] + " set");
				return;
			}

			player.message(messagePrefix + p.getUsername() + " has value " + p.getCache().getCacheMap().get(args[keyArg]).toString() + " for cache key " + args[keyArg]);
		} else if (cmd.equalsIgnoreCase("deletecache") || cmd.equalsIgnoreCase("dcache") || cmd.equalsIgnoreCase("removecache") || cmd.equalsIgnoreCase("rcache")) {
			if (args.length < 2) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " (name) [cache_key]");
				return;
			}

			int keyArg = args.length >= 2 ? 1 : 0;

			Player p = args.length >= 2 ?
				player.getWorld().getPlayer(DataConversions.usernameToHash(args[0])) :
				player;

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not modify cache of a staff member of equal or greater rank.");
				return;
			}

			if (!p.getCache().hasKey(args[keyArg])) {
				player.message(messagePrefix + p.getUsername() + " does not have the cache key " + args[keyArg] + " set");
				return;
			}

			p.getCache().remove(args[keyArg]);
			player.message(messagePrefix + "Removed " + p.getUsername() + "'s cache key " + args[keyArg]);
		} else if (cmd.equalsIgnoreCase("setquest") || cmd.equalsIgnoreCase("queststage") || cmd.equalsIgnoreCase("setqueststage") || cmd.equalsIgnoreCase("resetquest") || cmd.equalsIgnoreCase("resetq")) {
			if (args.length < 3) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId] (stage)");
				return;
			}

			Player p = player.getWorld().getPlayer(DataConversions.usernameToHash(args[0]));

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not modify quests of a staff member of equal or greater rank.");
				return;
			}

			int quest;
			try {
				quest = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId] (stage)");
				return;
			}

			int stage;
			if (args.length >= 3) {
				try {
					stage = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex) {
					player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId] (stage)");
					return;
				}
			} else {
				stage = 0;
			}

			p.updateQuestStage(quest, stage);
			if (p.getUsernameHash() != player.getUsernameHash()) {
				p.message(messagePrefix + "A staff member has changed your quest stage for QuestID " + quest + " to stage " + stage);
			}
			player.message(messagePrefix + "You have changed " + p.getUsername() + "'s QuestID: " + quest + " to Stage: " + stage + ".");
		} else if (cmd.equalsIgnoreCase("questcomplete") || cmd.equalsIgnoreCase("questcom")) {
			if (args.length < 2) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId]");
				return;
			}

			Player p = player.getWorld().getPlayer(DataConversions.usernameToHash(args[0]));

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not modify quests of a staff member of equal or greater rank.");
				return;
			}

			int quest;
			try {
				quest = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId]");
				return;
			}

			p.sendQuestComplete(quest);
			if (p.getUsernameHash() != player.getUsernameHash()) {
				p.message(messagePrefix + "A staff member has changed your quest to completed for QuestID " + quest);
			}
			player.message(messagePrefix + "You have completed Quest ID " + quest + " for " + p.getUsername());
		} else if (cmd.equalsIgnoreCase("quest") || cmd.equalsIgnoreCase("getquest") || cmd.equalsIgnoreCase("checkquest")) {
			if (args.length < 2) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId]");
				return;
			}

			Player p = player.getWorld().getPlayer(DataConversions.usernameToHash(args[0]));

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			int quest;
			try {
				quest = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [questId]");
				return;
			}

			player.message(messagePrefix + p.getUsername() + " has stage " + p.getQuestStage(quest) + " for quest " + quest);
		} else if (cmd.equalsIgnoreCase("reloaddrops")) {
			try {
				final NpcDrop drops[] = player.getWorld().getServer().getDatabase().getNpcDrops();
				final HashMap<Integer, ArrayList<ItemDropDef>> list = new HashMap<>();

				for(final NpcDrop drop : drops) {
					if(!list.containsKey(drop.npcId)) {
						list.put(drop.npcId, new ArrayList<>());
					}

					final ItemDropDef dropDef = new ItemDropDef(drop.itemId, drop.amount, drop.weight);
					list.get(drop.npcId).add(dropDef);
				}

				final Set<Map.Entry<Integer, ArrayList<ItemDropDef>>> entrySet = list.entrySet();
				for (Map.Entry<Integer, ArrayList<ItemDropDef>> entry : entrySet) {
					final int npcId = entry.getKey();
					final ArrayList<ItemDropDef> arrayList = entry.getValue();
					final NPCDef def = player.getWorld().getServer().getEntityHandler().getNpcDef(npcId);
					def.drops = null;
					def.drops = arrayList.toArray(new ItemDropDef[]{});
				}
			} catch (final GameDatabaseException ex) {
				LOGGER.catching(ex);
			}
			player.message(messagePrefix + "Drop tables reloaded");
		} else if (cmd.equalsIgnoreCase("reloadworld") || cmd.equalsIgnoreCase("reloadland")) {
			player.getWorld().getWorldLoader().loadWorld();
			player.message(messagePrefix + "World Reloaded");
		} else if (cmd.equalsIgnoreCase("summonall")) {
			if (args.length == 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " (width) (height)");
				return;
			}

			if (args.length == 0) {
				for (Player p : player.getWorld().getPlayers()) {
					if (p == null)
						continue;

					if (!p.isDefaultUser() && !p.isPlayerMod())
						continue;

					p.summon(player);
					p.message(messagePrefix + "You have been summoned by " + player.getStaffName());
				}
			} else if (args.length >= 2) {
				int width;
				int height;
				try {
					width = Integer.parseInt(args[0]);
					height = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					player.message(badSyntaxPrefix + cmd.toUpperCase() + " (width) (height)");
					return;
				}
				Random rand = DataConversions.getRandom();
				for (Player p : player.getWorld().getPlayers()) {
					if (p != player) {
						int x = rand.nextInt(width);
						int y = rand.nextInt(height);
						boolean XModifier = rand.nextInt(2) == 0;
						boolean YModifier = rand.nextInt(2) == 0;
						if (XModifier)
							x = -x;
						if (YModifier)
							y = -y;

						Point summonLocation = new Point(x, y);

						p.summon(summonLocation);
						p.message(messagePrefix + "You have been summoned by " + player.getStaffName());
					}
				}
			}

			player.message(messagePrefix + "You have summoned all players to " + player.getLocation());
			player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 15, player.getUsername() + " has summoned all players to " + player.getLocation()));
		} else if (cmd.equalsIgnoreCase("returnall")) {
			for (Player p : player.getWorld().getPlayers()) {
				if (p == null)
					continue;

				if (!p.isDefaultUser() && !p.isPlayerMod())
					continue;

				p.returnFromSummon();
				p.message(messagePrefix + "You have been returned by " + player.getStaffName());
			}
			player.message(messagePrefix + "All players who have been summoned were returned");
		} else if (cmd.equalsIgnoreCase("fatigue")) {
			if (args.length < 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] (percentage)");
				return;
			}

			Player p = player.getWorld().getPlayer(DataConversions.usernameToHash(args[0]));

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not fatigue a staff member of equal or greater rank.");
				return;
			}

			int fatigue;
			try {
				fatigue = args.length > 1 ? Integer.parseInt(args[1]) : 100;
			} catch (NumberFormatException e) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [player] [amount]");
				return;
			}

			if (fatigue < 0)
				fatigue = 0;
			if (fatigue > 100)
				fatigue = 100;
			p.setFatigue(fatigue * 1500);

			if (p.getUsernameHash() != player.getUsernameHash()) {
				p.message(messagePrefix + "Your fatigue has been set to " + ((p.getFatigue() / 25) * 100 / 1500) + "% by a staff member");
			}
			player.message(messagePrefix + p.getUsername() + "'s fatigue has been set to " + ((p.getFatigue() / 25) * 100 / 1500 / 4) + "%");
			player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 12, p, p.getUsername() + "'s fatigue percentage was set to " + fatigue + "% by " + player.getUsername()));
		} else if (cmd.equalsIgnoreCase("jail")) {
			if (args.length != 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [name]");
				return;
			}

			Player p = player.getWorld().getPlayer(DataConversions.usernameToHash(args[0]));

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (p.isJailed()) {
				player.message(messagePrefix + "You can not jail a player who has already been jailed.");
				return;
			}

			if (p.hasElevatedPriveledges()) {
				player.message(messagePrefix + "You can not jail a staff member.");
				return;
			}

			Point originalLocation = p.jail();
			player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 5, player.getUsername() + " has summoned " + p.getUsername() + " to " + p.getLocation() + " from " + originalLocation));
			player.message(messagePrefix + "You have jailed " + p.getUsername() + " to " + p.getLocation() + " from " + originalLocation);
			if (p.getUsernameHash() != player.getUsernameHash()) {
				p.message(messagePrefix + "You have been jailed to " + p.getLocation() + " from " + originalLocation + " by " + player.getStaffName());
			}
		} else if (cmd.equalsIgnoreCase("release")) {
			Player p = args.length > 0 ?
				player.getWorld().getPlayer(DataConversions.usernameToHash(args[0])) :
				player;

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (p.hasElevatedPriveledges()) {
				player.message(messagePrefix + "You can not release a staff member.");
				return;
			}

			if (!p.isJailed()) {
				player.message(messagePrefix + p.getUsername() + " has not been jailed.");
				return;
			}

			Point originalLocation = p.releaseFromJail();
			player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 5, player.getUsername() + " has returned " + p.getUsername() + " to " + p.getLocation() + " from " + originalLocation));
			player.message(messagePrefix + "You have released " + p.getUsername() + " from jail to " + p.getLocation() + " from " + originalLocation);
			if (p.getUsernameHash() != player.getUsernameHash()) {
				p.message(messagePrefix + "You have been released from jail to " + p.getLocation() + " from " + originalLocation + " by " + player.getStaffName());
			}
		} else if (cmd.equalsIgnoreCase("ban")) {
			if (args.length < 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [name] [time in minutes, -1 for permanent, 0 to unban]");
				return;
			}

			final long userToBan = DataConversions.usernameToHash(args[0]);
			final String usernameToBan = DataConversions.hashToUsername(userToBan);
			final Player p = player.getWorld().getPlayer(userToBan);

			int time;
			if (args.length >= 2) {
				try {
					time = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					player.message(badSyntaxPrefix + cmd.toUpperCase() + " [name] (time in minutes, -1 for permanent, 0 to unban)");
					return;
				}
			} else {
				time = player.isAdmin() ? -1 : 60;
			}

			if (time == 0 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to unban users.");
				return;
			}

			if (time == -1 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to permanently ban users.");
				return;
			}

			if (time > 1440 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to ban for more than a day.");
				return;
			}

			if(p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			if (!p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not ban a staff member of equal or greater rank.");
				return;
			}

			player.message(messagePrefix + player.getWorld().getServer().getDatabase().banPlayer(usernameToBan, player, time));
		} else if (cmd.equalsIgnoreCase("viewipbans")) {
			StringBuilder bans = new StringBuilder("Banned IPs % %");
			for (Map.Entry<String, Long> entry : player.getWorld().getServer().getPacketFilter().getIpBans().entrySet()) {
				bans.append("IP: ").append(entry.getKey()).append(" - Unban Date: ").append((entry.getValue() == -1) ? "Never" : DateFormat.getInstance().format(entry.getValue())).append("%");
			}
			ActionSender.sendBox(player, bans.toString(), true);

		} else if (cmd.equalsIgnoreCase("ipban")) {
			if (args.length < 1) {
				player.message(badSyntaxPrefix + cmd.toUpperCase() + " [name] [time in minutes, -1 for permanent, 0 to unban]");
				return;
			}

			long userToBan = DataConversions.usernameToHash(args[0]);
			Player p = player.getWorld().getPlayer(userToBan);
			String ipToBan = (p != null) ? p.getCurrentIP() : "";
			int time;
			if (StringUtil.isIPv4Address(args[0]) || StringUtil.isIPv6Address(args[0])) {
				ipToBan = args[0];
			}
			if (ipToBan.equals("")) {
				player.message(messagePrefix + "You must enter an IP address to ban.");
				return;
			}
			if (args.length >= 2) {
				try {
					time = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					player.message(badSyntaxPrefix + cmd.toUpperCase() + " [name] (time in minutes, -1 for permanent, 0 to unban)");
					return;
				}
			} else {
				time = player.isAdmin() ? -1 : 60;
			}

			if (time == 0 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to unban users.");
				return;
			}

			if (time == -1 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to permanently ban users.");
				return;
			}

			if (time > 1440 && !player.isAdmin()) {
				player.message(messagePrefix + "You are not allowed to ban for more than a day.");
				return;
			}

			if (p != null && !p.isDefaultUser() && p.getUsernameHash() != player.getUsernameHash() && player.getGroupID() >= p.getGroupID()) {
				player.message(messagePrefix + "You can not ban a staff member of equal or greater rank.");
				return;
			}

			if (p != null) {
				p.unregister(true, "You have been banned by " + player.getUsername() + " " + (time == -1 ? "permanently" : " for " + time + " minutes"));
			}

			if (time == 0) {
				player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 11, p, player.getUsername() + " was unbanned by " + player.getUsername()));
			} else {
				player.getWorld().getServer().getGameLogger().addQuery(new StaffLog(player, 11, p, player.getUsername() + " was banned by " + player.getUsername() + " " + (time == -1 ? "permanently" : " for " + time + " minutes")));
			}


			//player.message(messagePrefix + player.getWorld().getServer().getLoginExecutor().getPlayerDatabase().banPlayer(usernameToBan, time));

			player.getWorld().getServer().getPacketFilter().ipBanHost(ipToBan, (time == -1 || time == 0) ? time : (System.currentTimeMillis() + (time * 60 * 1000)), "by ipban command");
		} else if (cmd.equalsIgnoreCase("ipcount")) {
			Player p = args.length > 0 ?
				player.getWorld().getPlayer(DataConversions.usernameToHash(args[0])) :
				player;

			if (p == null) {
				player.message(messagePrefix + "Invalid name or player is not online");
				return;
			}

			int count = 0;
			for (Player worldPlayer : player.getWorld().getPlayers()) {
				if (worldPlayer.getCurrentIP().equals(p.getCurrentIP()))
					count++;
			}

			player.message(messagePrefix + p.getUsername() + " IP address: " + p.getCurrentIP() + " has " + count + " connections");
		}
	}
}
