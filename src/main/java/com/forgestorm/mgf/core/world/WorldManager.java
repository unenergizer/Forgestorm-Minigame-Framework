package com.forgestorm.mgf.core.world;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.team.TeamSpawnLocations;
import com.forgestorm.spigotcore.util.math.RandomChance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/3/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class WorldManager extends BukkitRunnable {

    private final AsyncFileManager asyncFileManager = new AsyncFileManager(this);
    private final Queue<String> worldLoadQueue = new ConcurrentLinkedQueue<>();
    private final Queue<String> worldUnloadQueue = new ConcurrentLinkedQueue<>();

    public WorldManager(MinigameFramework plugin) {
        asyncFileManager.runTaskTimerAsynchronously(plugin, 0, 20 * 5);
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
    private String getConfigurationPath(WorldData worldData) {
        return "Worlds." + worldData.getWorldIndex();
    }

    /**
     * This will get the team spawn locations for the selected world data.  These locations are
     * used to spawn teams in the world they will be playing in.
     *
     * @param worldData The worldData to use to get the YML path to the values needed.
     * @return A list of TeamSpawnLocation objects.
     */
    public List<TeamSpawnLocations> generateTeamSpawnLocations(WorldData worldData, Configuration configuration) {
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

        return teamSpawnLocations;
    }

    /**
     * Gets the spectator spawn for the given arena world.
     *
     * @param worldData     Information on the given arena world.
     * @param configuration The arena configuration.
     * @return A spectator location.
     */
    public Location getSpectatorLocation(WorldData worldData, Configuration configuration) {
        String spectator = configuration.getString(getConfigurationPath(worldData) + ".Spectator");
        String[] spectatorParts = spectator.split("/");

        return new Location(
                Bukkit.getWorld(worldData.getWorldName()),
                Double.parseDouble(spectatorParts[0]),
                Double.parseDouble(spectatorParts[1]),
                Double.parseDouble(spectatorParts[2])
        );
    }

    /**
     * Check if a given world is loaded.
     *
     * @param worldData The world data and name of the world to check.
     * @return True if the world is loaded, false otherwise.
     */
    public boolean isWorldLoaded(WorldData worldData) {
        for (World world : Bukkit.getWorlds()) if (world.getName().equals(worldData.getWorldName())) return true;
        return false;
    }

    /**
     * Gets the requested world.
     *
     * @param worldData The world data that contains the info
     *                  needed to get the requested world.
     */
    public void getWorld(WorldData worldData) {
        asyncFileManager.addWorldToCopy(worldData.getWorldName());
    }

    /**
     * Prepares to load a world sync with the server.
     *
     * @param worldName The name of the world.
     */
    void loadWorld(String worldName) {
        worldLoadQueue.add(worldName);
    }

    /**
     * Prepares to unload a world from the server.
     *
     * @param worldData Data that represents the world we want
     *                  to unload.
     */
    public void unloadWorld(WorldData worldData) {
        worldUnloadQueue.add(worldData.getWorldName());
    }

    /**
     * This will load a world that is in the SyncLoadQueue.
     * We will load the world in sync with the Spigot API.
     * Async world loading is NOT supported with the Spigot API.
     */
    private void syncWorldLoad() {
        if (worldLoadQueue.isEmpty()) return;
        String worldName = worldLoadQueue.remove();

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.createWorld();

        Bukkit.getWorld(worldName);
    }

    /**
     * This will unload the given world from the server.  We
     * choose to not save the world in the unloadWorld method
     * because when we need the world again, it will be loaded
     * from a backup.
     */
    private void syncWorldUnload() {
        if (worldUnloadQueue.isEmpty()) return;
        String worldName = worldUnloadQueue.remove();

        // Unload the world
        Bukkit.unloadWorld(worldName, false);

        // Prepare the directory for deletion.
        asyncFileManager.addWorldToDelete(worldName);
    }

    /**
     * Repeating task that will load and unload worlds as needed.
     */
    @Override
    public void run() {
        syncWorldLoad();
        syncWorldUnload();
    }
}
