package com.forgestorm.mgf.core.games.testgame;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.Minigame;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

public class TestGame extends Minigame {

    public TestGame(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void startGame() {
        initGame();
    }

    @Override
    public void stopGame() {
        // Unregister listeners
    }

    private void initGame() {
        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("Game ends in: " + countdown + " seconds.");
                }

                if (countdown == 0) {
                    cancel();
                    endMinigame();
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private Kit defaultKit, kit2;

    @Override
    public List<Kit> getKits() {
        World world = Bukkit.getWorld("mg-lobby");
        List<Kit> kits = new ArrayList<>();
        List<String> defaultKitDesc = new ArrayList<>();
        List<String> kit2Desc = new ArrayList<>();
        List<Location> loc1 = new ArrayList<>();
        List<Location> loc2 = new ArrayList<>();

        defaultKitDesc.add("All you really need is a lawn mower.");
        defaultKitDesc.add("But you don't get one. Use your hands!");

        kit2Desc.add("You got the mower, your fists!");

        loc1.add(new Location(world, -1, 73, -17));
        loc1.add(new Location(world, 0, 73, -16));

        loc2.add(new Location(world, 3, 73, -17));
        loc2.add(new Location(world, 2, 73, -16));

        defaultKit = new Kit("Grass Puncher",
                ChatColor.GREEN,
                EntityType.CHICKEN,
                Material.DIRT,
                defaultKitDesc,
                loc1);

        kit2 = new Kit("Mower",
                ChatColor.GREEN,
                EntityType.PIG,
                Material.GRASS,
                kit2Desc,
                loc2);

        kits.add(defaultKit);
        kits.add(kit2);

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<String> description = new ArrayList<>();
        description.add("Mow the grass or your dad's going to be pissed...");
        description.add("So mow that brush you dirty animal!");

        ArrayList<Location> platformLocations = new ArrayList<>();
        World world = Bukkit.getWorld("mg-lobby");
        platformLocations.add(new Location(world, 1, 72, 14));
        platformLocations.add(new Location(world, 0, 72, 13));

        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Brush Bandits",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.BOOKSHELF,
                description,
                platformLocations));
        return team;
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Run around as fast as you can and mow the grass.");
        tips.add("Left-Click the grass to break it.");
        tips.add("The player with the most cut grass wins!!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Run around as fast as you can and mow the grass.");
        rules.add("Left-Click the grass to break it.");
        rules.add("The player with the most cut grass wins!!");
        return rules;
    }
}
