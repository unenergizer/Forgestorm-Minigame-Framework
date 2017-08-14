package com.forgestorm.mgf.core.games.mobmurder;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/14/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class SpawnMobs {

    private static final int MAX_MOB_COUNT = 30;
    private static final int MAX_BAD_MOB_COUNT = 10;
    private final MinigameFramework plugin;
    private World world = Bukkit.getWorld(GameManager.getInstance().getCurrentArenaWorldData().getWorldName());
    private int goodMobs;
    private int badMobs;
    private boolean cancel = false;

    SpawnMobs(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    void cancelRunnable() {
        cancel = true;
    }

    public void run() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (cancel) cancel();
                getMobCount();

                //Spawn good mobs
                if (goodMobs <= MAX_MOB_COUNT) {
                    spawnMob(false);
                }

                //Spawn bad mobs
                if (badMobs <= MAX_BAD_MOB_COUNT) {
                    spawnMob(true);
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void spawnMob(boolean badMob) {
        Random generator = new Random();
        Location location = new Location(world, generator.nextInt(80) - 40, 82, generator.nextInt(80) - 40);
        String name = "";

        int spawnChance = generator.nextInt(100) + 1;

        //Assign name.
        if (badMob) {
            name = ChatColor.RED + "-5 Point";
        } else {
            int rarity = generator.nextInt(100) + 1;

            if (rarity >= 50) {
                name = ChatColor.WHITE + "+1 Point";
            } else if (rarity >= 25 && rarity < 50) {
                name = ChatColor.BLUE + "+2 Point";
            } else if (rarity >= 10 && rarity < 25) {
                name = ChatColor.GREEN + "+3 Point";
            } else if (rarity >= 3 && rarity < 10) {
                name = ChatColor.YELLOW + "+4 Point";
            } else if (rarity >= 1 && rarity < 3) {
                name = ChatColor.GOLD + "+5 Point";
            }
        }

        //Choose and spawn mob.
        if (spawnChance >= 75 && spawnChance < 100) {
            Entity entity = location.getWorld().spawnEntity(location, EntityType.MUSHROOM_COW);
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        } else if (spawnChance >= 50 && spawnChance < 75) {

            Entity entity = location.getWorld().spawnEntity(location, EntityType.PIG);
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        } else if (spawnChance >= 25 && spawnChance < 50) {

            Entity entity = location.getWorld().spawnEntity(location, EntityType.COW);
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        } else if (spawnChance >= 1 && spawnChance < 25) {

            Entity entity = location.getWorld().spawnEntity(location, EntityType.SHEEP);
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        }
    }

    private void getMobCount() {
        int positiveMobs = 0;
        int negativeMobs = 0;

        for (LivingEntity entity : world.getLivingEntities()) {
            if (!(entity instanceof Player)) {
                if (entity.getName().contains("-")) {
                    negativeMobs++;
                } else {
                    positiveMobs++;
                }
            }
        }

        goodMobs = positiveMobs;
        badMobs = negativeMobs;
    }
}
