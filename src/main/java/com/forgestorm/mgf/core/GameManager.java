package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.location.GameArena;
import com.forgestorm.mgf.core.location.GameLobby;
import com.forgestorm.mgf.core.location.access.ArenaPlayerAccess;
import com.forgestorm.mgf.core.location.access.ArenaSpectatorAccess;
import com.forgestorm.mgf.core.location.access.LobbyAccess;
import com.forgestorm.mgf.core.score.StatManager;
import com.forgestorm.mgf.core.selectable.team.TeamSpawnLocations;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.player.PlayerMinigameManager;
import com.forgestorm.mgf.world.WorldData;
import com.forgestorm.mgf.world.WorldManager;
import com.forgestorm.spigotcore.events.UpdateScoreboardEvent;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import com.forgestorm.spigotcore.util.math.RandomChance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private GameSelector gameSelector;
    private PlayerMinigameManager playerMinigameManager;
    private StatManager statManager;
    private GameLobby gameLobby;
    private GameArena gameArena;
    private Configuration arenaConfiguration;
    private int maxPlayersOnline = 16;
    @Setter
    private int minPlayersToStartGame = 2;
    private WorldData currentArenaWorldData;
    private boolean inLobby = true;
    @Setter
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

        // Clear world entities. This will remove any unused holograms and other misc. entities.
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (!(entity instanceof Player)) entity.remove();
        }

        // Initialize needed classes.
        this.plugin = plugin;
        playerMinigameManager = new PlayerMinigameManager();
        gameSelector = new GameSelector();

        // Clear any existing entities in the world.
        clearWorldEntities(Bukkit.getWorlds().get(0));

        // On first load, lets init our first game.
        gameSelector.assignNextGame();

        // Init game locations
        gameArena = new GameArena();
        gameLobby = new GameLobby();

        // Start the BukkitRunnable thread.
        runTaskTimer(plugin, 0, 20);

        // Setup the game!
        setupGame();
    }

    /**
     * This will setup the current minigame lobby.
     */
    private void setupGame() {
        // Set defaults
        inLobby = true;

        // Get arena configuration (for loading worlds)
        arenaConfiguration = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder() + "/" + gameSelector.getMinigameType().getFileName()));

        // Load arena world
        WorldData worldToLoad = getRandomArenaWorld(arenaConfiguration);

        // If the worldData isn't null and the worldData doesn't equal the current one,
        // then we will unload the current arena and then load a new one.  We do this
        // check to make sure the framework isn't unloading and loading a world that
        // is going to be used again.  While this is rare, we do want to prevent this
        // error.
        if (currentArenaWorldData == null) {
            // An arena world has not been loaded. Lets do that now.
            WorldManager.getInstance().copyWorld(currentArenaWorldData = worldToLoad);

        } else {
            // Unload the previous arena world.
            WorldManager.getInstance().unloadWorld(currentArenaWorldData);

            // Load the next world
            WorldManager.getInstance().copyWorld(currentArenaWorldData = worldToLoad);
        }

        // Create and setup the lobby
        gameLobby.setupGameLocation();
        ColorLogger.DARK_AQUA.printLog("GameLobby setup ran");

        // Setup the stat manager.
        statManager = new StatManager(plugin);
    }

    /**
     * Called when a game is ready to be started. From here we will teleport all the players into the arena and
     * do the proper countdowns.
     */
    public void switchToArena() {
        inLobby = false;

        // Stop lobby code and remove lobby players.
        gameLobby.destroyGameLocation();

        // Clear entities from arena map
        clearWorldEntities(WorldManager.getInstance().getWorld(currentArenaWorldData));

        // Backup player inventories.
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerMinigameManager.makePlayerInventoryBackup(player);
        }

        // Switch to the game arena!
        gameArena.setupGameLocation();

        // Start stat manager
        statManager.initStats(gameSelector.getMinigame().getStatTypeList());
    }

    /**
     * This is called when a minigame ends or when the server is stopping or restarting.
     *
     * @param startNewGame True if a new game should be
     *                     started. False otherwise.
     */
    public void endGame(boolean startNewGame) {
        // Disable the lobby and the arena
        clearWorldEntities(WorldManager.getInstance().getWorld(currentArenaWorldData));
        if (gameArena != null) gameArena.destroyGameLocation();

        // Remove players
        if (inLobby) {
            if (gameLobby == null) return;

            // Remove lobby players.
            gameLobby.allPlayersQuit(new LobbyAccess());
        } else {
            // Remove arena players and spectators.
            gameArena.allPlayersQuit(new ArenaPlayerAccess());
            gameArena.allPlayersQuit(new ArenaSpectatorAccess());
        }

        // Set reference to null
        if (teamSpawnLocations != null) {

            // Loop through and clean all locations.
            for (TeamSpawnLocations locations : teamSpawnLocations) locations.disable();

            // Clear the reference
            teamSpawnLocations.clear();
            teamSpawnLocations = null;
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
        if (startNewGame) {
            gameSelector.assignNextGame();
            setupGame();
        }
    }

    /**
     * Called when the server is shutting down or restarting.
     */
    public void onDisable() {
        // Cancel thread
        if (isRunning) cancel();

        // End game and prevent a new game from starting up.
        endGame(false);

        // Disable Game Arena and Game Lobby
        if (gameArena != null) gameArena.destroyGameLocation();
        if (gameLobby != null) gameLobby.destroyGameLocation();

        // Disable player manager.
        playerMinigameManager.onDisable();
    }

    @Override
    public void run() {
        isRunning = true;

        // Check for game completion
        if (!isInLobby() && gameSelector.getMinigame().isGameOver()) endGame(true);
    }

    /**
     * Clear any existing entities in the world.
     *
     * @param world The world to clear entities in.
     */
    private void clearWorldEntities(World world) {
        if (world == null) return;

        for (Entity entity : world.getEntities())
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
        ColorLogger.GREEN.printLog(showDebug, "GameManager - setMaxPlayersOnline()");
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
     * This is a check to see if the game should end. If there aren't enough players, then we should
     * end the games.  Added to ability to make sure that when we run this check we are filtering
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

    /**
     * Responsible for getting a random world from the configuration.  This random world
     * will be the world that players will play in in the minigames.
     * <p>
     * So what happens here, we have to loop through all the world index's represented as
     * numbers, get the name of the world, then at the end we will select a random world
     * to play in.
     *
     * @return This will return the world data for one particular world.
     */
    public WorldData getRandomArenaWorld(Configuration configuration) {
        List<WorldData> worldDataList = new ArrayList<>();
        ConfigurationSection outerSection = configuration.getConfigurationSection("Worlds");

        // Get all worlds
        for (String worldNumber : outerSection.getKeys(false)) {
            String worldName = configuration.getString("Worlds." + worldNumber + ".Name");
            worldDataList.add(new WorldData(Integer.valueOf(worldNumber), worldName));
        }

        // Return random world data.
        if (worldDataList.size() - 1 > 1) return worldDataList.get(RandomChance.randomInt(0, worldDataList.size() - 1));

        // If only one world exists, then just return the first one.
        return worldDataList.get(0);
    }

    /**
     * Gets the path of the arena configuration file.
     *
     * @param worldData The world data needed for the path.
     * @return A string that contains the YAML configuration path.
     */
    private static String getConfigurationPath(WorldData worldData) {
        return "Worlds." + worldData.getWorldIndex();
    }

    /**
     * This will get the team spawn locations for the selected world data.  These locations are
     * used to spawn teams in the world they will be playing in.
     *
     * @param worldData The worldData to use to get the YML path to the values needed.
     * @return A list of TeamSpawnLocation objects.
     */
    public void generateTeamSpawnLocations(WorldData worldData, Configuration configuration) {
        List<TeamSpawnLocations> teamSpawnLocations = new ArrayList<>();

        // Get team spawn locations.
        ConfigurationSection innerSection = configuration.getConfigurationSection(getConfigurationPath(worldData) + ".Teams");

        // Loop through all teams
        for (String teamNumber : innerSection.getKeys(false)) {
            List<Location> locations = new ArrayList<>();
            List<String> locationsAsStr = innerSection.getStringList(teamNumber);

            // For each location that a team has, we will add it to the locations list above.
            locationsAsStr.forEach((locationAsStr) -> {
                String[] parts = locationAsStr.split("/");
                locations.add(new Location(
                        Bukkit.getWorld(worldData.getWorldName()),
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]))
                );
            });

            // Add each team we find to the list.
            teamSpawnLocations.add(new TeamSpawnLocations(Integer.valueOf(teamNumber), locations));
        }

        this.teamSpawnLocations = teamSpawnLocations;
    }

    /**
     * Gets the spectator spawn for the given arena world.
     *
     * @return A spectator location.
     */
    public Location getSpectatorLocation() {
        GameManager gameManager = GameManager.getInstance();
        WorldData worldData = gameManager.getCurrentArenaWorldData();
        String spectator = gameManager.getArenaConfiguration().getString(getConfigurationPath(worldData) + ".Spectator");
        String[] spectatorParts = spectator.split("/");

        return new Location(
                Bukkit.getWorld(worldData.getWorldName()),
                Double.parseDouble(spectatorParts[0]),
                Double.parseDouble(spectatorParts[1]),
                Double.parseDouble(spectatorParts[2])
        );
    }
}
