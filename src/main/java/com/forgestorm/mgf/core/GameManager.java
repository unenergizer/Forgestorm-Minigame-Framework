package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.games.GameType;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.location.GameArena;
import com.forgestorm.mgf.core.location.GameLobby;
import com.forgestorm.mgf.core.location.access.ArenaPlayerAccess;
import com.forgestorm.mgf.core.location.access.ArenaSpectatorAccess;
import com.forgestorm.mgf.core.location.access.LobbyAccess;
import com.forgestorm.mgf.core.score.StatManager;
import com.forgestorm.mgf.core.team.TeamSpawnLocations;
import com.forgestorm.mgf.core.world.WorldData;
import com.forgestorm.mgf.core.world.WorldManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.player.PlayerMinigameManager;
import com.forgestorm.spigotcore.events.UpdateScoreboardEvent;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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

    private static GameManager instance = null;
    private final boolean showDebug = true;
    private boolean isSetup = false;
    private boolean isRunning = false;
    private MinigameFramework plugin;
    private PlayerMinigameManager playerMinigameManager;
    private WorldManager worldManager;
    private List<String> gamesToPlay;
    private StatManager statManager;
    private GameLobby gameLobby;
    private GameArena gameArena;
    private Configuration arenaConfiguration;
    private GameType currentMinigameType;
    private Minigame currentMinigame;
    private int maxPlayersOnline = 16;
    @Setter
    private int minPlayersToStartGame = 2;
    private int currentGameIndex = 0;
    private WorldData currentArenaWorldData;
    private boolean currentArenaWorldLoaded = false;
    private boolean inLobby = true;
    private List<TeamSpawnLocations> teamSpawnLocations;

    private GameManager() {
    }

    /**
     * Gets the current instance of GameManager.
     *
     * @return An instance of GameManager. If not created, it will create one.
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
            return instance;
        }
        return instance;
    }

    /**
     * This will setup GameManager for first time use.
     *
     * @param plugin An instance of the main plugin class.
     */
    public void setup(MinigameFramework plugin) {
        // Prevent setup from being run more than once.
        if (isSetup) return;
        isSetup = true;

        // Initialize needed classes.
        this.plugin = plugin;
        playerMinigameManager = new PlayerMinigameManager();
        worldManager = new WorldManager(plugin);
        gamesToPlay = plugin.getConfigGameList();

        // Start world manager repeating task.
        worldManager.runTaskTimer(plugin, 0, 20);

        // Clear any existing entities in the world.
        clearWorldEntities(Bukkit.getWorlds().get(0).getName());

        // On first load, lets init our first game.
        selectGame();

        // Start the BukkitRunnable thread.
        this.runTaskTimer(plugin, 0, 20);
    }

    /**
     * This method is responsible for selecting which game is going to be played.
     * Games that will be played are assigned in the config file.
     */
    private void selectGame() {
        int totalGames = gamesToPlay.size() - 1;

        // Basic game selection based on array list index.
        currentGameIndex++;

        // Go back to first game if needed.
        if (currentGameIndex > totalGames) currentGameIndex = 0;

        // Grab game selected.
        currentMinigameType = GameType.valueOf(plugin.getConfigGameList().get(currentGameIndex));
        currentMinigame = currentMinigameType.getMinigame(plugin);

        // Get arena configuration (for loading worlds)
        arenaConfiguration = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder() + "/" + currentMinigameType.getFileName()));

        // Now setup the game!
        setupGame();
    }

    /**
     * This will setup the current minigame lobby and arena.
     */
    private void setupGame() {
        // Set defaults
        inLobby = true;
        currentArenaWorldLoaded = false;

        // Load arena world
        WorldData worldToLoad = worldManager.getRandomArenaWorld(arenaConfiguration);

        // If the worldData isn't null and the worldData doesn't equal the current one,
        // then we will unload the current arena and then load a new one.  We do this
        // check to make sure the framework isn't unloading and loading a world that
        // is going to be used again.  While this is rare, we do want to prevent this
        // error.
        if (currentArenaWorldData == null) {
            // An arena world has not been loaded. Lets do that now.
            worldManager.getWorld(currentArenaWorldData = worldToLoad);

        } else {
            // Unload the previous arena world.
            worldManager.unloadWorld(currentArenaWorldData);

            // Load the next world
            worldManager.getWorld(currentArenaWorldData = worldToLoad);
        }

        // Create and setup the lobby
        gameLobby = new GameLobby();
        gameLobby.setupGameLocation();
        gameLobby.allPlayersJoin(new LobbyAccess());

        // Setup the stat manager.
        statManager = new StatManager(plugin);
    }

    /**
     * Called when a game is ready to be started. From here
     * we will teleport all the players into the arena and
     * do the proper countdowns.
     */
    public void switchToArena() {
        inLobby = false;

        // Stop lobby code and remove lobby players.
        gameLobby.destroyGameLocation();
        gameLobby.allPlayersQuit(new LobbyAccess());

        // Clear entities from arena map
        clearWorldEntities(currentArenaWorldData.getWorldName());

        // Backup player inventories.
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerMinigameManager.makePlayerInventoryBackup(player);
        }

        // Switch to the game arena!
        gameArena = new GameArena();
        gameArena.setupGameLocation();
        gameArena.allPlayersJoin(new ArenaPlayerAccess());
        gameArena.showTutorialInfo();

        // Build stat type lists for scores.
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.hasMetadata("NPC"))
                .filter(player -> !playerMinigameManager.getPlayerProfileData(player).isSpectator())
                .collect(Collectors.toList());

        statManager.initStats(players, currentMinigame.getStatTypes());
    }

    /**
     * This is called when a minigame ends or when the
     * server is stopping or restarting.
     *
     * @param startNewGame True if a new game should be
     *                     started. False otherwise.
     */
    public void endGame(boolean startNewGame) {
        // Disable the lobby and the arena
        clearWorldEntities(currentArenaWorldData.getWorldName());
        gameArena.destroyGameLocation();

        // Remove players
        if (inLobby) {
            // Remove lobby players.
            gameLobby.allPlayersQuit(new LobbyAccess());
        } else {
            // Remove arena players and spectators.
            gameArena.allPlayersQuit(new ArenaPlayerAccess());
            gameArena.allPlayersQuit(new ArenaSpectatorAccess());
        }

        // Restore player inventories.
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerMinigameManager.restorePlayerInventoryBackup(player);
        }

        // Update database
        statManager.updateDatabase();

        // Unregister stat stat listeners.
        statManager.deregisterListeners();

        // Determines if a new game should start.
        // Typically if the server is shutting down or
        // reloading a new game will not be started.
        if (startNewGame) selectGame();
    }

    /**
     * Called when the server is shutting down or restarting.
     */
    public void onDisable() {
        // Cancel thread
        if (isRunning) cancel();

        // End game and prevent a new game from starting up.
        endGame(false);

        // Disable player manager.
        playerMinigameManager.onDisable();
    }

    @Override
    public void run() {
        isRunning = true;

        // Check for game completion
        if (!isInLobby() && currentMinigame.isGameOver()) endGame(true);

        // Make sure the arena world is loaded and ready
        if (!currentArenaWorldLoaded && worldManager.isWorldLoaded(currentArenaWorldData)) {
            // Set some bool world loaded.
            currentArenaWorldLoaded = true;
            teamSpawnLocations = worldManager.generateTeamSpawnLocations(currentArenaWorldData, arenaConfiguration);
        }
    }

    /**
     * Clear any existing entities in the world.
     *
     * @param worldName The name of the world to clear entities in.
     */
    private void clearWorldEntities(String worldName) {
        for (Entity entity : Bukkit.getWorld(worldName).getEntities())
            if (!(entity instanceof Player) && !(entity instanceof ArmorStand)) entity.remove();
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

    /**
     * Gets if the minigame should start.
     *
     * @return True if should start, false otherwise.
     */
    public boolean shouldMinigameStart() {
        return Bukkit.getOnlinePlayers().size() >= minPlayersToStartGame;
    }

    /**
     * This is a check to see if the game should end.
     * If there aren't enough players, then we should
     * end the games.  Added to ability to make sure
     * that when we run this check we are filtering
     * out spectator players.
     */
    public boolean shouldMinigameEnd(Player exitPlayer) {
        int minigamePlayers = 0;
        boolean shouldEnd = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == exitPlayer) continue;
            PlayerMinigameData playerMinigameData = playerMinigameManager.getPlayerProfileData(player);
            if (playerMinigameData == null || playerMinigameData.isSpectator()) continue;
            minigamePlayers++;
        }

        if (minigamePlayers < minPlayersToStartGame) {
            shouldEnd = true;

            // Send error message.
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(MinigameMessages.ALERT.toString() + MinigameMessages.GAME_COUNTDOWN_NOT_ENOUGH_PLAYERS.toString());
        }

        return shouldEnd;
    }
}
