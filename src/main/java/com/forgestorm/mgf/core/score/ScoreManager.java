package com.forgestorm.mgf.core.score;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.score.statlisteners.StatListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<Player, Map<StatType, Integer>> playerStats = new HashMap<>();
    private final List<StatListener> statListeners = new ArrayList<>();
    private Map<StatType, Integer> winConditions;

    public ScoreManager(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    // Choose "main" stat listener for a players/teams placing and scoreboard info.


    /**
     * Register all stats that the game will listen to during the game play.
     *
     * @param players   A list of players.
     * @param statTypes A list of stat types to listen to.
     */
    public void initStats(List<Player> players, List<StatType> statTypes) {
        // For each player register a statType and default value
        players.forEach(player -> {
            System.out.println("ADDING THE PLAYER TO THE MAP: " + player.getName());
            Map<StatType, Integer> statsForPlayer = new HashMap<>();
            statTypes.forEach(type -> statsForPlayer.put(type, 0));
            playerStats.put(player, statsForPlayer);
        });

        // Save stat stat listeners
        statTypes.forEach(statType -> statListeners.add(statType.registerListener(plugin)));
    }

    /**
     * This will add a win condition to the score manager.  Win
     * conditions determine when a game should end.
     *
     * StatType = The stat we want to listen to for a win.
     * Integer = The max points needed for the win.
     *
     * @param winConditions The win conditions for the given game.
     */
    public void initWinConditions(Map<StatType, Integer> winConditions) {
        this.winConditions = winConditions;
    }

    /**
     * This will check to see if a win condition has been met.
     *
     * @param statType The stat to check.
     * @param player   The player who we will check scores for.
     */
    private void checkWinConditions(StatType statType, Player player) {
        if (!winConditions.containsKey(statType)) return;

        int playerPoints = playerStats.get(player).get(statType);
        int maxPoints = winConditions.get(statType);

        if (playerPoints >= maxPoints) {
            // Point threshold has been met. Lets end the current game.
            plugin.getGameManager().getCurrentMinigame().endMinigame();
        }
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
    public void addStat(StatType statType, Player player, int amount) {

        Map<StatType, Integer> stats = playerStats.get(player);
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
            stats.forEach((statType, amount) -> Bukkit.broadcastMessage(player.getName() + " : " + statType + "=" + amount));
        });
    }
}
