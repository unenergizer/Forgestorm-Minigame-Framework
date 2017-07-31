package com.forgestorm.mgf.player;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameArena;
import com.forgestorm.mgf.core.GameLobby;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.util.logger.ColorLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

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

public class PlayerManager implements Listener {

    private final MinigameFramework plugin;
    private final GameManager gameManager;
    private final Map<Player, PlayerMinigameData> playerProfiles = new HashMap<>();
    private final boolean showDebug = true;

    public PlayerManager(MinigameFramework plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;

        // Register the PlayerManager event stat listeners.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // If server reloaded, lets create a profile for all the online players.
        for (Player player : Bukkit.getOnlinePlayers()) {
            createProfile(player);
        }
    }

    /**
     * Called when the server is stopping or restarting.
     */
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeProfileData(player);
        }

        // Unregister Events
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerKickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    /**
     * Used for grabbing the players profile data.
     *
     * @param player The player we are grabbing data for.
     * @return The PlayerMinigameData needed for player data editing.
     */
    public PlayerMinigameData getPlayerProfileData(Player player) {
        return playerProfiles.get(player);
    }

    /**
     * Creates a profile for the player to be used on the plugin.
     *
     * @param player The player we will create a profile for.
     */
    private void createProfile(Player player) {
        if (player.hasMetadata("NPC")) return;
        // TODO: GET MONGO DATABASE DATA
        playerProfiles.put(player, new PlayerMinigameData(player));
    }

    /**
     * Removes a player from the playerProfiles HashMap and their profile data.
     *
     * @param player The player we will be removing.
     */
    private void removeProfileData(Player player) {
        if (player.hasMetadata("NPC")) return;
        // TODO: SAVE MONGO DATABASE DATA
        playerProfiles.remove(player);
    }

    /**
     * Called when a player quits the server.
     *
     * @param player The player who left the server.
     * @return A quit message for server players.
     */
    private String onPlayerQuit(Player player) {
        PlayerMinigameData playerMinigameData = playerProfiles.get(player);
        String playerName = player.getName();
        boolean isSpectator = playerMinigameData.isSpectator();

        // Player quit specific actions depending
        // if the player is in the lobby or the
        // core arena.
        if (gameManager.isInLobby()) {
            ///////////////////////////
            //// LOBBY PLAYER QUIT ////
            ///////////////////////////
            ColorLogger.INFO.printLog(showDebug, "PlayerManager - onPlayerQuit() -> Lobby Quit");

            // Remove from the core lobby.
            GameLobby gameLobby = gameManager.getGameLobby();
            gameLobby.removePlayer(player, true);
            gameLobby.getTarkanLobbyScoreboard().updatePlayerCountAndGameStatus(Bukkit.getOnlinePlayers().size() - 1);
            gameLobby.getTeamManager().playerQuit(player);

            // Remove profile and save data.
            removeProfileData(player);

            // Lobby quit message
            String onlinePlayers = Integer.toString(Bukkit.getOnlinePlayers().size() - 1);
            String maxPlayers = Integer.toString(gameManager.getMaxPlayersOnline());
            return MinigameMessages.PLAYER_QUIT_LOBBY.toString()
                    .replace("%s", onlinePlayers) // Number of players.
                    .replace("%f", maxPlayers) // Max Players Allowed
                    .replace("%e", playerName); // Player Name
        } else {
            ////////////////////
            //// ARENA QUIT ////
            ////////////////////

            GameArena gameArena = gameManager.getGameArena();
            if (isSpectator) {
                ////////////////////////
                //// SPECTATOR QUIT ////
                ////////////////////////
                ColorLogger.INFO.printLog(showDebug, "PlayerManager - onPlayerQuit() -> Spectator Quit");

                // Remove spectator from the arena.
                gameArena.removeSpectator(player);

                // Remove profile and save data.
                removeProfileData(player);

                // Show spectator quit message.
                return MinigameMessages.SPECTATOR_QUIT.toString().replace("%s", playerName);
            } else {
                ///////////////////////////
                //// ARENA PLAYER QUIT ////
                ///////////////////////////
                ColorLogger.INFO.printLog(showDebug, "PlayerManager - onPlayerQuit() -> Arena Player Quit");

                // Remove the player from the arena.
                gameArena.removeArenaPlayer(player);

                // Remove profile and save data.
                removeProfileData(player);

                // Show arena player quit message.
                return MinigameMessages.PLAYER_QUIT_GAME.toString().replace("%s", playerName);
            }
        }
    }

    /**
     * On the PlayerJoinEvent we notify the plugin
     * that a new player has joined the server. From
     * here we setup their profile and place them in
     * the appropriate area (core lobby or core
     * arena).
     *
     * @param event A Bukkit event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String joinMessage = "";

        // Create the players profile
        createProfile(player);

        if (gameManager.isInLobby()) {
            ColorLogger.INFO.printLog(showDebug, "PlayerManager - onPlayerJoin() -> Lobby Join");
            //////////////////
            /// LOBBY JOIN ///
            //////////////////

            // Lobby join message.
            String onlinePlayers = Integer.toString(Bukkit.getOnlinePlayers().size());
            String maxPlayers = Integer.toString(gameManager.getMaxPlayersOnline());
            joinMessage = joinMessage.concat(MinigameMessages.PLAYER_JOIN_LOBBY.toString()
                    .replace("%s", onlinePlayers) // Number of players.
                    .replace("%f", maxPlayers) // Max Players Allowed
                    .replace("%e", playerName)); // Player Name

            // Show the join message.
            event.setJoinMessage(joinMessage);


            // Setup lobby player.
            GameLobby gameLobby = gameManager.getGameLobby();
            gameLobby.setupPlayer(player);
            gameLobby.getTarkanLobbyScoreboard().updatePlayerCountAndGameStatus(Bukkit.getOnlinePlayers().size());

        } else {
            ColorLogger.INFO.printLog(showDebug, "PlayerManager -onPlayerJoin() -> Spectator Join");
            //////////////////////
            /// SPECTATOR JOIN ///
            //////////////////////

            // Spectator join message.
            joinMessage = joinMessage.concat(MinigameMessages.SPECTATOR_JOIN.toString().replace("%s", playerName));

            // Show the join message.
            event.setJoinMessage(joinMessage);

            // Setup spectator player
            // Run on the next tick to prevent teleport bug.

            System.out.println("Doing spectator specific code! :)");

            PlayerMinigameData playerMinigameData = getPlayerProfileData(player);
            playerMinigameData.backupInventoryContents();
            gameManager.getGameArena().addSpectator(player);
            gameManager.getGameArena().teleportSpectator(player);
        }
    }

    /**
     * On the PlayerQuitEvent we notify the plugin
     * when a player quits out the server.
     *
     * @param event A Bukkit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(onPlayerQuit(event.getPlayer()));
    }

    /**
     * On the PlayerKickEvent we notify the plugin
     * that a player has been kicked from the server.
     *
     * @param event A Bukkit event.
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(onPlayerQuit(event.getPlayer()));
    }
}
