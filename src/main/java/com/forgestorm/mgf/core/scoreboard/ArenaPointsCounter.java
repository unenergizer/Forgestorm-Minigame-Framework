package com.forgestorm.mgf.core.scoreboard;

import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.winmanagement.winevents.IndividualTopScoreWinEvent;
import com.forgestorm.mgf.util.MapUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
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

public class ArenaPointsCounter extends ArenaScoreboard {

    private final int maxScore;
    private final String unit;
    private final Map<Player, Integer> playerScore = new HashMap<>();
    private boolean gameOver = false;

    public ArenaPointsCounter(int maxScore, String unit) {
        this.maxScore = maxScore;
        this.unit = unit;
    }

    /**
     * Set the scores for the lobby scoreboard.
     */
    private void setBoardData(Map<Player, Integer> scores) {
        Map<Player, Integer> sortedMap = MapUtil.sortByValueReverse(scores);

        for (Player player : Bukkit.getOnlinePlayers()) {
            int line = 1;
            for (Map.Entry<Player, Integer> entry : sortedMap.entrySet()) {
                if (line > scores.size() || line > 15) return;

                Player scoreboardEntry = entry.getKey();
                String score = Integer.toString(entry.getValue());

                titleManagerAPI.setScoreboardValue(player, line, score + " " + ChatColor.GREEN + scoreboardEntry.getDisplayName());

                line++;
            }
        }
    }

    @Override
    public void initBeginningLines(Player scoreboardOwner) {
        int line = 1;
        for (Player scoreboardEntry : Bukkit.getOnlinePlayers()) {
            if (GameManager.getInstance().getPlayerMinigameManager().getPlayerProfileData(scoreboardEntry).isSpectator()) continue;
            titleManagerAPI.setScoreboardValue(scoreboardOwner, line, 0 + " " + ChatColor.GREEN + scoreboardEntry.getDisplayName());
            line++;
        }
    }

    /**
     * Adds players scores up as the game progresses.
     *
     * @param player The player we will add a score for.
     * @param amount The amount of points to give the player.
     */
    public void addScore(Player player, int amount) {
        if (gameOver) return;

        // Set default value.
        if (!playerScore.containsKey(player)) {
            playerScore.put(player, 0);
        }

        // Get scores.
        int current = playerScore.get(player);
        int totalScore = amount + current;
        playerScore.replace(player, amount + current);

        // Update the scoreboard.
        setBoardData(playerScore);

        // Test to see if the score goal has been reached.
        if (totalScore >= maxScore) {
            gameOver = true;
            Bukkit.getPluginManager().callEvent(new IndividualTopScoreWinEvent(playerScore, unit));
        }
    }
}
