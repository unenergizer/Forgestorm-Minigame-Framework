package com.forgestorm.mgf.core.games.mowgrass;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.mowgrass.kits.GrassPuncher;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.selectable.kit.Kit;
import com.forgestorm.mgf.core.selectable.team.Team;
import com.forgestorm.spigotcore.util.display.BossBarAnnouncer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
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

    private final BossBarAnnouncer bossBarAnnouncer = new BossBarAnnouncer("init");

    public MowGrass(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        for (Player player : Bukkit.getOnlinePlayers()) bossBarAnnouncer.showBossBar(player);
        initGame();
    }

    @Override
    public void disableGame() {
        BlockBreakEvent.getHandlerList().unregister(this);
        for (Player player : Bukkit.getOnlinePlayers()) bossBarAnnouncer.removeBossBar(player);
    }

    private void initGame() {
        new BukkitRunnable() {
            int countdown = 60;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasMetadata("NPC")) return;
                    bossBarAnnouncer.setBossBarTitle(ChatColor.BOLD + "Time left " +
                            ChatColor.YELLOW + ChatColor.BOLD + countdown +
                            ChatColor.WHITE + ChatColor.BOLD + " seconds.");
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
        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Brush Bandits",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.STONE,
                new String[]{"Cut the grass or your dad's going to be pissed...", "So mow that brush you dirty animal!"}));
        return team;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> scoreData = new ArrayList<>();
        scoreData.add(StatType.FIRST_KILL);
        return scoreData;
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

    @EventHandler
    public void onGrassBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().equals(Material.DOUBLE_PLANT)) return;
        event.setCancelled(false);

        //PLUGIN.getScore().givePoint(event.getPlayer(), 1);
    }
}
