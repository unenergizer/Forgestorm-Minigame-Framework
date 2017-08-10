package com.forgestorm.mgf.core.score;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.score.statlisteners.StatListener;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
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

    private final Map<Player, Map<StatType, Double>> playerStats = new HashMap<>();
    private final List<StatListener> statListeners = new ArrayList<>();

    public ScoreManager(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    /**
     * Register all stats that the game will listen to during the game play.
     *
     * @param players  A list of players.
     * @param statType A list of StatTypes to listen to.
     */
    public void initStats(List<Player> players, List<StatType> statType) {
        // For each player register a statType and default value
        for (Player player : players) {
            Map<StatType, Double> statsForPlayer = new HashMap<>();
            playerStats.put(player, statsForPlayer);
        }

        // Save stat stat listeners
        for (StatType stat : statType) {
            statListeners.add(stat.registerListener(plugin));
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
    public void addStat(StatType statType, Player player, double amount) {

        Map<StatType, Double> stats = playerStats.get(player);
        stats.replace(statType, stats.get(statType) + amount);
        playerStats.replace(player, stats);
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
        playerStats.forEach((player, stats) ->
                stats.forEach((statType, amount) -> ColorLogger.DEBUG.printLog(player.getName() + " : " + statType + "=" + amount))
        );
    }
}
