package com.forgestorm.mgf.core.scoreboard;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.spigotcore.constants.SpigotCoreMessages;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/29/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be
 * reproduced, distributed, or transmitted in any form or by any means,
 * including photocopying, recording, or other electronic or mechanical methods,
 * without the prior written permission of the owner.
 */

public class ArenaPointsCounter extends BukkitRunnable {

    private final MinigameFramework plugin;
    private final StatType statType;
    private final TitleManagerAPI titleManagerAPI;
    @Setter
    private boolean cancelTask = false;


    public ArenaPointsCounter(MinigameFramework plugin, StatType statType) {
        this.plugin = plugin;
        this.statType = statType;
        titleManagerAPI = plugin.getTitleManagerAPI();
    }

    /**
     * Gives all players a scoreboard.
     */
    public void addAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Tarkan board setup
            titleManagerAPI.giveScoreboard(player);

            // Set scoreboard title
            titleManagerAPI.setScoreboardTitle(player, SpigotCoreMessages.SCOREBOARD_TITLE.toString());
        }
    }

    /**
     * Removes all player scoreboards.
     */
    public void removeAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!titleManagerAPI.hasScoreboard(player)) return;
            titleManagerAPI.removeScoreboard(player);
        }
    }

    @Override
    public void run() {
        if (cancelTask) {
            cancel();
            return;
        }

        // TODO: IMPLEMENT NON LOOP BASED SCOREBOARD UPDATES!!

        // Update scoreboard data
//        for (Player player : Bukkit.getOnlinePlayers()) {
//            setBoardData(player, plugin.getGameManager().getScoreManager().generateTopScores(statType));
//        }
    }

    /**
     * Set the scores for the lobby scoreboard.
     *
     * @param player The player that we are setting scores for.
     */
    private void setBoardData(Player player, Map<Player, Double> scores) {
        int line = 1;

        for (Map.Entry<Player, Double> scoreData : scores.entrySet()) {
            if (line > scores.size() || line > 15) return;

            String name = scoreData.getKey().getDisplayName();
            String score = Integer.toString((int) scoreData.getValue().doubleValue());

            titleManagerAPI.setScoreboardValue(player, line, score + " " + ChatColor.GREEN + name);

            line++;
        }
    }
}
