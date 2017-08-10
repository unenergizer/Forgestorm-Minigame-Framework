package com.forgestorm.mgf.core.world;

import com.forgestorm.mgf.MinigameFramework;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

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
