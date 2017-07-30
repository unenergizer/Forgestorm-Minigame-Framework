package com.forgestorm.mgf.core.score;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.score.statlisteners.StatListener;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/22/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class ScoreManager {

    private final MinigameFramework plugin;

    private final Map<Player, Map<StatType, Double>> playerStats = new HashMap<>();
    private final List<StatListener> statListeners = new ArrayList<>();
    private final Map<StatType, Double> winConditions = new HashMap<>();
    private boolean winConditionMet = false;
    @Getter
    private Map<Player, Double> finalScore;

    public ScoreManager(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    /**
     * Register all stats that the game will listen to during the game play.
     *
     * @param players   A list of players.
     * @param scoreData A list of data that contains StatTypes to listen to.
     */
    public void initStats(List<Player> players, List<ScoreData> scoreData) {
        // For each player register a statType and default value
        for (Player player : players) {
            Map<StatType, Double> statsForPlayer = new HashMap<>();
            for (ScoreData data : scoreData) {
                statsForPlayer.put(data.getStatType(), 0.0);
            }
            playerStats.put(player, statsForPlayer);
        }

        // Save stat stat listeners
        for (ScoreData data : scoreData) {
            statListeners.add(data.getStatType().registerListener(plugin));
        }
    }

    /**
     * This will add a win condition to the score manager.  Win
     * conditions determine when a game should end.
     * <p>
     * StatType = The stat we want to listen to for a win.
     * Integer = The max points needed for the win.
     *
     * @param scoreData A data class that contains win conditions for the given game.
     */
    public void initWinConditions(List<ScoreData> scoreData) {
        for (ScoreData data : scoreData) {
            if (!data.isWinCondition()) return;
            winConditions.put(data.getStatType(), data.getMaxWinScore());
        }
    }

    /**
     * This will check to see if a win condition has been met.
     *
     * @param statType The stat to check.
     * @param player   The player who we will check scores for.
     */
    private void checkWinConditions(StatType statType, Player player) {
        if (!winConditions.containsKey(statType)) return;
        if (winConditionMet) return;

        double playerPoints = playerStats.get(player).get(statType);
        double maxPoints = winConditions.get(statType);

        if (playerPoints >= maxPoints && !winConditionMet) {
            winConditionMet = true;

            // Generate the top scores for display.
            finalScore = generateTopScores(statType);

            // Point threshold has been met. Lets end the current game.
            plugin.getGameManager().getCurrentMinigame().endMinigame();
        }
    }

    /**
     * Generate a list of the top performing players for end game scores.
     *
     * @param statType The stat type used to reach a end game win condition.
     */
    public Map<Player, Double> generateTopScores(StatType statType) {
        // Build easy map for score sorting.
        Map<Player, Double> unsortedScores = new HashMap<>();

        for (Player player : playerStats.keySet()) {
            unsortedScores.put(player, playerStats.get(player).get(statType));
        }

        // Sort the players scores.
        MyComparator comparator = new MyComparator(unsortedScores);
        Map<Player, Double> scores = new TreeMap<>(comparator);
        scores.putAll(unsortedScores);

        return scores;
    }

    /**
     * This will add 1 point for a given stat point.
     *
     * @param statType The stat type we want to add a point for.
     * @param player   The player who we are adding points for.
     */
    public void addStat(StatType statType, Player player) {
        addStat(statType, player, 1);
    }

    /**
     * This will add a variant amount of stat points to a given stat.
     *
     * @param statType The stat type we want to add a point for.
     * @param player   The player who we are adding points for.
     * @param amount   The amount of points to add.
     */
    public void addStat(StatType statType, Player player, double amount) {

        Map<StatType, Double> stats = playerStats.get(player);
        stats.replace(statType, stats.get(statType) + amount);
        playerStats.replace(player, stats);

        checkWinConditions(statType, player);
    }

    /**
     * Unregister listeners from minigame.
     */
    public void deregisterListeners() {
        statListeners.forEach(StatListener::deregister);
    }

    /**
     * This will take all the given stat points and update the database.
     * TODO: Actually update the database with the new stat points.
     */
    public void updateDatabase() {
        playerStats.forEach((player, stats) -> {
            stats.forEach((statType, amount) -> System.out.println(player.getName() + " : " + statType + "=" + amount));
        });
    }

    /**
     * Code by: Sujan Reddy A
     * Date: Feb 10 '13 at 6:10
     * Source: https://stackoverflow.com/a/14795215/2865125
     *
     * Modified by: Robert Brown
     * Date Added: 7/28/2017
     */
    class MyComparator implements Comparator<Object> {

        Map<Player, Double> map;

        MyComparator(Map<Player, Double> map) {
            this.map = map;
        }

        @SuppressWarnings({"SuspiciousMethodCalls", "NumberEquality"})
        public int compare(Object o1, Object o2) {

            if (map.get(o2) == map.get(o1)) {
                return 1;
            } else {
                return (map.get(o2)).compareTo(map.get(o1));
            }
        }
    }
}
