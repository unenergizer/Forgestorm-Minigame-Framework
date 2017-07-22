package com.forgestorm.mgf.core.games.sheersheep;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
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

public class SheerSheep extends Minigame {

    private int maxScore = 80;

    public SheerSheep(MinigameFramework plugin) {
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

    private Kit defaultKit, kit2;

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();
        List<String> defaultKitDesc = new ArrayList<>();
        List<String> kit2Desc = new ArrayList<>();

        defaultKitDesc.add("Blow up them sheeps!");
        defaultKitDesc.add("Get as much wool as you can!");

        kit2Desc.add("Knife them till the wool falls off!");


        defaultKit = new Kit("Explosive Shears",
                ChatColor.GREEN,
                EntityType.CHICKEN,
                Material.STONE,
                defaultKitDesc);

        kit2 = new Kit("Knife Party",
                ChatColor.GREEN,
                EntityType.PIG,
                Material.STONE,
                kit2Desc);

        kits.add(defaultKit);
        kits.add(kit2);

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<String> description = new ArrayList<>();
        description.add("Every player from themselves!!");
        description.add("Compete to be the best!");


        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Individual Team",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.BOOKSHELF,
                description));
        return team;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> statTypes = new ArrayList<>();
        statTypes.add(StatType.PICKUP_ITEM);
        return statTypes;
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Run around as fast as you can to shear the sheeps.");
        tips.add("Right click with your sheers to shear a sheep.");
        tips.add("The first person to get " + maxScore + " wool wins!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Run around as fast as you can to shear the sheeps.");
        rules.add("Right click with your sheers to shear a sheep.");
        rules.add("The first person to get \" + maxScore + \" wool wins!");
        return rules;
    }
}
