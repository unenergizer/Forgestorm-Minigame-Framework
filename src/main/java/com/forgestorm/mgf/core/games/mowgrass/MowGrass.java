package com.forgestorm.mgf.core.games.mowgrass;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.mowgrass.kits.GrassPuncher;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class MowGrass extends Minigame {

    public MowGrass(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        initGame();
    }

    @Override
    public void disableGame() {
        // TODO: Do game ending code here.
    }

    private void initGame() {
        new BukkitRunnable() {
            int countdown = 30;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasMetadata("NPC")) return;
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

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();

        kits.add(new GrassPuncher());

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<String> description = new ArrayList<>();
        description.add("Cut the grass or your dad's going to be pissed...");
        description.add("So mow that brush you dirty animal!");

        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Brush Bandits",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.STONE,
                description));
        return team;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> statTypes = new ArrayList<>();
        statTypes.add(StatType.FIRST_KILL);
        return statTypes;
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
