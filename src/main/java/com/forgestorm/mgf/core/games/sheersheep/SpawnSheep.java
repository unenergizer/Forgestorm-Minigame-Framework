package com.forgestorm.mgf.core.games.sheersheep;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/22/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

class SpawnSheep {

    private final MinigameFramework plugin;
    private final int maxSheepCount = 30;
    private World world;
    private boolean spawnSheep;

    SpawnSheep(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    void spawnSheep() {
        Random random = new Random();
        spawnSheep = true;
        world = Bukkit.getWorld(GameManager.getInstance().getCurrentArenaWorldData().getWorldName());
        ColorLogger.DEBUG.printLog("SpawningSheep!");
        new BukkitRunnable() {

            @Override
            public void run() {
                int currentSheepCount = getSheepCount();

                if (currentSheepCount <= maxSheepCount) {
                    for (int i = 1; i <= maxSheepCount - currentSheepCount; i++) {
                        Location location = new Location(world, random.nextInt(80) - 40, 82, random.nextInt(80) - 40);
                        location.getWorld().spawnEntity(location, EntityType.SHEEP);
                    }
                }

                //If game is over, lets stop spawning sheep.
                if (!spawnSheep) {
                    //Stop the thread first.
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private int getSheepCount() {
        int sheep = 0;

        for(Entity entity : world.getEntities()) {
            if (entity.getType() == EntityType.SHEEP) {
                sheep++;
            }
        }

        return sheep;
    }

    void cancelSheepSpawn() {
        this.spawnSheep = false;
    }
}
