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

    // Register stat stat listeners to game.
    // Choose "main" stat listener for a players/teams placing.

    // StatType => kills

    private Map<Player, Map<StatType, Integer>> playerStats = new HashMap<>();
    private List<StatListener> statListeners = new ArrayList<>();

    public void initStats(List<Player> players, List<StatType> statTypes, MinigameFramework plugin) {
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

    public void addStat(StatType statType, Player player) {
        addStat(statType, player, 1);
    }

    public void addStat(StatType statType, Player player, int amount) {

        Map<StatType, Integer> stats = playerStats.get(player);
        stats.replace(statType, stats.get(statType) + amount);
        playerStats.replace(player, stats);

    }

    public void deregisterListeners() {
        statListeners.forEach(StatListener::deregister);
    }

    public void updateDatabase() {
        playerStats.forEach((player, stats) -> {
            stats.forEach((statType, amount) -> Bukkit.broadcastMessage(player.getName() + " : " + statType + "=" + amount));
        });
    }
}
