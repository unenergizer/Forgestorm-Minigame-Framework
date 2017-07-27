package com.forgestorm.mgf.commands;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameArena;
import com.forgestorm.mgf.core.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/2/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be
 * reproduced, distributed, or transmitted in any form or by any means,
 * including photocopying, recording, or other electronic or mechanical methods,
 * without the prior written permission of the owner.
 */

public class Admin implements CommandExecutor {

	private final MinigameFramework plugin;
	private final GameManager gameManager;

	public Admin(MinigameFramework plugin) {
		this.plugin = plugin;
		this.gameManager = plugin.getGameManager();
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

		// set min players

		// set max players

		//Check command args
		if (args.length == 1) {

			switch (args[0].toLowerCase()) {
				case "stop":
					GameArena.ArenaState arenaState = gameManager.getGameArena().getArenaState();
					if (arenaState == GameArena.ArenaState.ARENA_EXIT || arenaState == GameArena.ArenaState.ARENA_SHOW_SCORES) {
						commandSender.sendMessage(ChatColor.RED + "The game is about to end.");
						return false;
					}
					gameManager.endGame(true);
					Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "An administrator has stopped the game.");
					break;
			}
		} else if (args.length == 2) {

			switch (args[0].toLowerCase()) {
				case "minimumplayers":
				case "minplayers":
				case "minp":
					if (!isInteger(args[1])) {
					    commandSender.sendMessage(MinigameMessages.ADMIN.toString() +
                                ChatColor.RED + "" + ChatColor.BOLD + "Please enter a valid number!");
					    return false;
                    }
					int oldMin = gameManager.getMinPlayersToStartGame();
					int newMin = Integer.parseInt(args[1]);
					gameManager.setMinPlayersToStartGame(newMin);
					commandSender.sendMessage(MinigameMessages.ADMIN.toString() +
                            ChatColor.YELLOW + "Set minimum players to " + newMin + " from " + oldMin + ".");
					break;
				case "maximumplayers":
				case "maxplayers":
				case "maxp":
                    if (!isInteger(args[1])) {
                        commandSender.sendMessage(MinigameMessages.ADMIN.toString() +
                                ChatColor.RED + "" + ChatColor.BOLD + "Please enter a valid number!");
                        return false;
                    }
				    int oldMax = gameManager.getMinPlayersToStartGame();
					int newMax = Integer.parseInt(args[1]);
					if(!gameManager.setMaxPlayersOnline(commandSender, newMax)) return false;
					commandSender.sendMessage(MinigameMessages.ADMIN.toString() +
                            ChatColor.YELLOW + "Set maximum players to " + newMax + " from " + oldMax + ".");
					break;
			}

		}

		return false;
	}

	private static boolean isInteger(String s) {
		return isInteger(s,10);
	}

	private static boolean isInteger(String s, int radix) {
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if(Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}

}
