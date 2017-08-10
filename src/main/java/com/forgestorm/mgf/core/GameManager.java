package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.games.GameType;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.score.ScoreManager;
import com.forgestorm.mgf.core.world.WorldData;
import com.forgestorm.mgf.core.world.WorldManager;
import com.forgestorm.mgf.player.PlayerManager;
import com.forgestorm.spigotcore.events.UpdateScoreboardEvent;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

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
@Getter
public class GameManager extends BukkitRunnable {

    private final MinigameFramework plugin;
    private final PlayerManager playerManager;
    private final WorldManager worldManager;
    private final List<String> gamesToPlay;
    private ScoreManager scoreManager;
    private GameLobby gameLobby;
    private GameArena gameArena;
    private GameType currentMinigameType;
    private Minigame currentMinigame;
    private int maxPlayersOnline = 16;
    @Setter
    private int minPlayersToStartGame = 2;
    private int currentGameIndex = 0;
    private WorldData currentArenaWorldData;
    private boolean currentArenaWorldLoaded = false;
    private boolean inLobby = true;
    private final boolean showDebug = true;

    public GameManager(MinigameFramework plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin, this);
        this.worldManager = new WorldManager(plugin);
        this.gamesToPlay = plugin.getConfigGameList();

        // Start world manager repeating task.
        worldManager.runTaskTimer(plugin, 0, 20);

        // Clear any existing entities in the world.
        clearWorldEntities(Bukkit.getWorlds().get(0).getName());

        // On first load, lets init our first game.
        selectGame();
    }

    /**
     * This method is responsible for selecting which game is going to be played.
     * Games that will be played are assigned in the config file.
     */
    private void selectGame() {
        ColorLogger.INFO.printLog(showDebug, "GameManager - selectGame()");
        int totalGames = gamesToPlay.size() - 1;

        // Basic game selection based on array list index.
        currentGameIndex++;

        // Go back to first game if needed.
        if (currentGameIndex > totalGames) currentGameIndex = 0;

        // Grab game selected.
        currentMinigameType = GameType.valueOf(plugin.getConfigGameList().get(currentGameIndex));
        currentMinigame = currentMinigameType.getMinigame(plugin);

        // Now setup the game!
        setupGame();
    }

    /**
     * This will setup the current minigame lobby and arena.
     */
    private void setupGame() {
        ColorLogger.INFO.printLog(showDebug, "GameManager - setupGame()");
        // Set defaults
        inLobby = true;
        currentArenaWorldLoaded = false;

        // Create and setup the lobby
        gameLobby = new GameLobby(plugin, this, currentMinigame);
        gameLobby.setupLobby();
        gameLobby.setupAllPlayers();
        //gameLobby.resetPlayerScoreboards();
        gameLobby.runTaskTimer(plugin, 0, 20);

        // Create Arena
        gameArena = new GameArena(plugin, this, currentMinigame, currentMinigameType);
        gameArena.runTaskTimer(plugin, 0, 20);

        // Setup the score manager.
        scoreManager = new ScoreManager(plugin);

        // Load arena world
        WorldData worldToLoad = gameArena.getRandomArenaWorld();

        // If the worldData isn't null and the worldData doesn't equal the current one,
        // then we will unload the current arena and then load a new one.  We do this
        // check to make sure the framework isn't unloading and loading a world that
        // is going to be used again.  While this is rare, we do want to prevent this
        // error.
        if (currentArenaWorldData == null) {
            // An arena world has not been loaded. Lets do that now.
            worldManager.getWorld(currentArenaWorldData = worldToLoad);

        } else if (!currentArenaWorldData.getWorldName().equals(worldToLoad.getWorldName())) {
            // Unload the previous arena world.
            worldManager.unloadWorld(currentArenaWorldData);

            // Load the next world
            worldManager.getWorld(currentArenaWorldData = worldToLoad);
        }
    }

    /**
     * Called when a game is ready to be started. From here
     * we will teleport all the players into the arena and
     * do the proper countdowns.
     */
    void switchToArena() {
        ColorLogger.INFO.printLog(showDebug, "GameManager - switchToArena()");
        inLobby = false;

        // Stop lobby code and remove lobby players.
        gameLobby.destroyLobby();
        gameLobby.removeAllPlayers();

        // Switch to the lobby!
        gameArena.setupArena();
        gameArena.addAllArenaPlayers();
        gameArena.showTutorialInfo();

        // Build stat type lists for scores.
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.hasMetadata("NPC"))
                .filter(player -> !playerManager.getPlayerProfileData(player).isSpectator())
                .collect(Collectors.toList());

        scoreManager.initStats(players, currentMinigame.getStatTypes());
    }

    /**
     * This is called when a minigame ends or when the
     * server is stopping or restarting.
     *
     * @param startNewGame True if a new game should be
     *                     started. False otherwise.
     */
    @SuppressWarnings("WeakerAccess")
    public void endGame(boolean startNewGame) {
        ColorLogger.INFO.printLog(showDebug, "GameManager - endGame()");

        // Disable the lobby and the arena
        clearWorldEntities(currentArenaWorldData.getWorldName());
        gameArena.destroyArena();

        // Remove players
        if (inLobby) {
            // Remove lobby players.
            gameLobby.removeAllPlayers();
        } else {
            // Remove arena players and spectators.
            gameArena.removePlayersFromArena();
        }

        // Update database
        scoreManager.updateDatabase();

        // Unregister stat stat listeners.
        scoreManager.deregisterListeners();

        // Determines if a new game should start.
        // Typically if the server is shutting down or
        // reloading a new game will not be started.
        if (startNewGame) selectGame();
    }

    /**
     * Called when the server is shutting down or restarting.
     */
    public void onDisable() {
        endGame(false);

        playerManager.onDisable();
    }

    @Override
    public void run() {
        // Check for game completion
        if (!isInLobby() && currentMinigame.isGameOver()) endGame(true);

        // Make sure the arena world is loaded and ready
        if (!currentArenaWorldLoaded && worldManager.isWorldLoaded(currentArenaWorldData)) {
            // Set some bool world loaded.
            currentArenaWorldLoaded = true;
            gameArena.generateTeamSpawnLocations(currentArenaWorldData);
        }
    }

    /**
     * Clear any existing entities in the world.
     *
     * @param worldName The name of the world to clear entities in.
     */
    public void clearWorldEntities(String worldName) {
        for (Entity entity : Bukkit.getWorld(worldName).getEntities()) if (!(entity instanceof Player)) entity.remove();
    }

    /**
     * Sets the maximum number of players allowed on the server.
     *
     * @param commandSender The command sender who requested this number to be changed.
     * @param num           The new number of max players allowed online.
     * @return True if the request was a success, false otherwise.
     */
    public boolean setMaxPlayersOnline(CommandSender commandSender, int num) {
        ColorLogger.INFO.printLog(showDebug, "GameManager - setMaxPlayersOnline()");
        // Make sure the max players online is greater than the minimum needed to start a game.
        if (num < minPlayersToStartGame) {
            commandSender.sendMessage(MinigameMessages.ADMIN.toString() + ChatColor.RED + "" + ChatColor.BOLD +
                    "The number must be greater than the minimum players needed to start a game!");
            return false;
        }

        // If in the lobby, update the scoreboard to reflect the new max players change.
        if (inLobby) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Bukkit.getPluginManager().callEvent(new UpdateScoreboardEvent(player));
            }
        }

        maxPlayersOnline = num;
        return true;
    }
}
