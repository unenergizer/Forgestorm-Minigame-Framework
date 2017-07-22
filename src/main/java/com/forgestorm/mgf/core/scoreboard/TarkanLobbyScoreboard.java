package com.forgestorm.mgf.core.scoreboard;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameLobby;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.spigotcore.SpigotCore;
import com.forgestorm.spigotcore.constants.SpigotCoreMessages;
import com.forgestorm.spigotcore.database.PlayerProfileData;
import com.forgestorm.spigotcore.database.ProfileLoadedEvent;
import com.forgestorm.spigotcore.events.UpdateScoreboardEvent;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

public class TarkanLobbyScoreboard implements Listener {

    private final MinigameFramework plugin;
    private final SpigotCore spigotCore;
    private final GameManager gameManager;
    private final GameLobby gameLobby;
    private final TitleManagerAPI titleManagerAPI;
    //private final Animation animation;
    private int gameWaitingAnimate = 1;


    public TarkanLobbyScoreboard(MinigameFramework plugin, GameManager gameManager, GameLobby gameLobby) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.gameLobby = gameLobby;
        spigotCore = plugin.getSpigotCore();
        titleManagerAPI = plugin.getTitleManagerAPI();
        //animation = titleManagerAPI.getRegisteredAnimations().get("scoreboard-title");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a player and gives them a scoreboard.
     *
     * @param player The player that will receive a scoreboard.
     */
    private void addPlayer(Player player) {
        // Tarkan board setup
        titleManagerAPI.giveScoreboard(player);

        //Setup player's scoreboard.
        setBoardData(player);
        updatePlayerCountAndGameStatus(Bukkit.getOnlinePlayers().size());

        // Set scoreboard title
        titleManagerAPI.setScoreboardTitle(player, SpigotCoreMessages.SCOREBOARD_TITLE.toString());

//        SendableAnimation sendableAnimation = titleManagerAPI.toScoreboardTitleAnimation(animation, player, false);
//        sendableAnimation.setContinuous(true);
//        sendableAnimation.start();
    }

    /**
     * Removes a player scoreboard..
     *
     * @param player The player that will have their scoreboard removed.
     */
    public void removePlayer(Player player) {
        if (!titleManagerAPI.hasScoreboard(player)) return;
        titleManagerAPI.removeScoreboard(player);
    }


    /**
     * This updates the player counts on peoples scoreboard when
     * players join or leave the server.
     *
     * @param currentPlayers The player count to display in the
     *                       scoreboard
     */
    public void updatePlayerCountAndGameStatus(int currentPlayers) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            String maxPlayers = Integer.toString(gameManager.getMaxPlayersOnline());

            // Game status
            if (currentPlayers >= gameManager.getMinPlayersToStartGame()) {
                titleManagerAPI.setScoreboardValue(player, 7, MinigameMessages.TSB_STATUS.toString() +
                        MinigameMessages.SB_GAME_STATUS_READY.toString());
            } else {
                titleManagerAPI.setScoreboardValue(player, 7, MinigameMessages.TSB_STATUS.toString() +
                        MinigameMessages.SB_GAME_STATUS_WAITING_1.toString());
            }

            // Player count
            String pCount = Integer.toString(currentPlayers) + ChatColor.GRAY + "/" + ChatColor.RESET + maxPlayers;
            titleManagerAPI.setScoreboardValue(player, 8, MinigameMessages.TSB_PLAYERS.toString() + pCount);
        }
    }

    /**
     * Updates the scoreboard with the players new Kit selection.
     *
     * @param player             The player who's scoreboard we will update.
     * @param playerMinigameData The players minigame data that contains the new kit they clicked.
     */
    public void updatePlayerKit(Player player, PlayerMinigameData playerMinigameData) {
        Kit kit = playerMinigameData.getSelectedKit();
        String kitName = kit.getKitColor() + kit.getKitName();
        titleManagerAPI.setScoreboardValue(player, 11, trimString(MinigameMessages.TSB_KIT.toString() +
                kitName));
    }

    /**
     * Updates the scoreboard with the players new Team selection.
     *
     * @param player             The player who's scoreboard we will update.
     * @param playerMinigameData The players minigame data that contains the new team they clicked.
     */
    public void updatePlayerTeam(Player player, PlayerMinigameData playerMinigameData) {
        Team team = playerMinigameData.getSelectedTeam();
        String teamName = team.getTeamColor() + team.getTeamName();
        titleManagerAPI.setScoreboardValue(player, 10, trimString(MinigameMessages.TSB_TEAM.toString() +
                teamName));
    }

    /**
     * Set the scores for the lobby scoreboard.
     *
     * @param player The player that we are setting scores for.
     */
    private void setBoardData(Player player) {
        PlayerProfileData playerProfileData = spigotCore.getProfileManager().getProfile(player);
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        String gameName = gameManager.getCurrentMinigameType().getFriendlyName();

        // Blank line 1
        titleManagerAPI.setScoreboardValue(player, 1, SpigotCoreMessages.SCOREBOARD_BLANK_LINE_1.toString());

        // GEMS
        titleManagerAPI.setScoreboardValue(player, 2, SpigotCoreMessages.SCOREBOARD_GEMS.toString() +
                playerProfileData.getCurrency());

        // LEVEL
        titleManagerAPI.setScoreboardValue(player, 3, SpigotCoreMessages.SCOREBOARD_LEVEL.toString() +
                playerProfileData.getPlayerLevel());

        // XP
        titleManagerAPI.setScoreboardValue(player, 4, SpigotCoreMessages.SCOREBOARD_XP.toString() +
                playerProfileData.getExpPercent() + "%");

        // Blank line 2
        titleManagerAPI.setScoreboardValue(player, 5, SpigotCoreMessages.SCOREBOARD_BLANK_LINE_2.toString());

        // GAME
        titleManagerAPI.setScoreboardValue(player, 6, trimString(MinigameMessages.TSB_GAME.toString() +
                gameName));

        // STATUS
        titleManagerAPI.setScoreboardValue(player, 7, MinigameMessages.TSB_STATUS.toString());

        // PLAYERS
        titleManagerAPI.setScoreboardValue(player, 8, MinigameMessages.TSB_PLAYERS.toString());

        // Blank line 3
        titleManagerAPI.setScoreboardValue(player, 9, MinigameMessages.TSB_BLANK_LINE_3.toString());

        // TEAM
        Team team = playerMinigameData.getSelectedTeam();
        String teamName = team.getTeamColor() + team.getTeamName();
        titleManagerAPI.setScoreboardValue(player, 10, trimString(MinigameMessages.TSB_TEAM.toString() +
                teamName));

        // KIT
        Kit kit = playerMinigameData.getSelectedKit();
        String kitName = kit.getKitColor() + kit.getKitName();
        titleManagerAPI.setScoreboardValue(player, 11, trimString(MinigameMessages.TSB_KIT.toString() +
                kitName));

        // Blank line 4
        titleManagerAPI.setScoreboardValue(player, 12, MinigameMessages.TSB_BLANK_LINE_4.toString());

        // SERVER
        titleManagerAPI.setScoreboardValue(player, 13, SpigotCoreMessages.SCOREBOARD_SERVER.toString());

        titleManagerAPI.setScoreboardValue(player, 14, plugin.getServer().getServerName());
    }

    /**
     * This will make sure a string is not longer than 14 characters.
     * If it is, we will shorten the string.
     *
     * @param input The string we want to trim.
     * @return The trimmed string.
     */
    private String trimString(String input) {
        final int maxWidth = 42; // Tarkan Scoreboard length is 42.

        // Check to see if the input length is greater than the maxWidth of characters.
        if (input.length() > maxWidth) {
            int amountOver = input.length() - maxWidth;
            return input.substring(0, input.length() - amountOver - 2) + "..";
        } else {
            // The input is less than 15 characters so it does not need to be trimmed.
            return input;
        }
    }

    /**
     * This will do a small scoreboard animation letting the player know
     * we are currently waiting on more players before the core is ready
     * to begin the initial countdown.
     */
    public void animateScoreboard() {
        // Update animation frame.
        if (gameWaitingAnimate != 5) {
            gameWaitingAnimate++;
        } else {
            gameWaitingAnimate = 1;
        }

        // Loop through player list and update all players with new animation.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;

            // Make sure the player has the scoreboard.
            if (!titleManagerAPI.hasScoreboard(player)) {
                addPlayer(player);
            }

            // Update game countdown
            if (gameLobby.isCountdownStarted() && gameLobby.getCountdown() <= 20) {
                String msg = MinigameMessages.TSB_STATUS.toString() + ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + gameLobby.getCountdown();
                titleManagerAPI.setScoreboardValue(player, 7, msg);
            } else {
                updatePlayerCountAndGameStatus(Bukkit.getOnlinePlayers().size());
            }
        }
    }

    @EventHandler
    public void onUpdateScoreboard(UpdateScoreboardEvent event) {
        setBoardData(event.getPlayer());
    }

    @EventHandler
    public void onProfileLoad(ProfileLoadedEvent event) {
        addPlayer(event.getPlayer());
    }
}
