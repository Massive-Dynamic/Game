package com.openrsc.server.plugins.listeners.action;

import com.openrsc.server.model.entity.player.Player;

public interface PlayerDeathListener {
	/**
	 * Called on a players death
	 *
	 * @param p
	 */
	void onPlayerDeath(Player p);
}
