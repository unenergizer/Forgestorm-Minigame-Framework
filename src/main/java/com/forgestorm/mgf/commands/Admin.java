package com.forgestorm.mgf.commands;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameArena;
import com.forgestorm.mgf.core.GameLobby;
import com.forgestorm.mgf.core.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    private static final String ERROR = ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private final GameManager gameManager;

    public Admin(MinigameFramework plugin) {
        this.gameManager = plugin.getGameManager();
    }

    private static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    private static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        // Send blank string ahead of other messages if it's a player commandSender.
        if (commandSender instanceof Player) commandSender.sendMessage("");

        //Check command args
        if (args.length == 1) {

            switch (args[0].toLowerCase()) {
                case "start":
                    GameLobby gameLobby = gameManager.getGameLobby();

                    if (!gameManager.isInLobby()) {
                        commandSender.sendMessage(ERROR + ChatColor.RED + "Must be in the game lobby to force start!");
                        return false;
                    }

                    if (!gameLobby.shouldMinigameStart()) {
                        commandSender.sendMessage(ERROR + ChatColor.RED + "Not enough players to start.");
                        String playerCount = Integer.toString(Bukkit.getOnlinePlayers().size());
                        commandSender.sendMessage(ERROR + ChatColor.AQUA + "Run " + ChatColor.DARK_AQUA +
                                "/mga minp " + playerCount + ChatColor.AQUA + " to change the minimal players to start.");
                        return false;
                    }

                    if (gameLobby.getCountdown() <= 2) {
                        commandSender.sendMessage(ERROR + ChatColor.RED + "The game is already about to start.");
                        return false;
                    }

                    gameLobby.setCountdown(1);
                    Bukkit.broadcastMessage(ERROR + ChatColor.GREEN + "" + ChatColor.BOLD + "An administrator has started the game.");
                    break;
                case "stop":
                    GameArena.ArenaState arenaState = gameManager.getGameArena().getArenaState();

                    if (arenaState == GameArena.ArenaState.ARENA_EXIT || arenaState == GameArena.ArenaState.ARENA_SHOW_SCORES) {
                        commandSender.sendMessage(ERROR + ChatColor.RED + "The game is about to end.");
                        return false;
                    }

                    gameManager.endGame(true);
                    Bukkit.broadcastMessage(ERROR + ChatColor.RED + "" + ChatColor.BOLD + "An administrator has stopped the game.");
                    break;
            }
        } else if (args.length == 2) {

            switch (args[0].toLowerCase()) {
                case "minimumplayers":
                case "minplayers":
                case "minp":
                    if (!isInteger(args[1])) {
                        commandSender.sendMessage(ERROR + ChatColor.RED + "" + ChatColor.BOLD + "Please enter a valid number!");
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
                        commandSender.sendMessage(ERROR + ChatColor.RED + "" + ChatColor.BOLD + "Please enter a valid number!");
                        return false;
                    }
                    int oldMax = gameManager.getMaxPlayersOnline();
                    int newMax = Integer.parseInt(args[1]);
                    if (!gameManager.setMaxPlayersOnline(commandSender, newMax)) return false;
                    commandSender.sendMessage(MinigameMessages.ADMIN.toString() +
                            ChatColor.YELLOW + "Set maximum players to " + newMax + " from " + oldMax + ".");
                    break;
            }

        }

        return false;
    }
}
