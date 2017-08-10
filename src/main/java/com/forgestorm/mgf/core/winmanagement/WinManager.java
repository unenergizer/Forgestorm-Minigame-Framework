package com.forgestorm.mgf.core.winmanagement;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.winmanagement.winevents.IndividualTopScoreWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastManStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastTeamStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.TeamTopScoreWinEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/9/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class WinManager implements Listener {

    private final MinigameFramework plugin;
    private final Minigame minigame;
    @Getter
    private List<String> scoreMessages = new ArrayList<>();

    public WinManager(MinigameFramework plugin, Minigame minigame) {
        this.plugin = plugin;
        this.minigame = minigame;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void endMinigame() {
        minigame.endMinigame();
    }

    public void printScores() {
        for (String messages : scoreMessages) Bukkit.broadcastMessage(messages);
    }

    private void setScoreMessages(String[] winMessages) {
        scoreMessages.add("");
        scoreMessages.add("");
        scoreMessages.add("");
        scoreMessages.add(MinigameMessages.GAME_BAR_SCORES.toString());
        scoreMessages.add("");
        Collections.addAll(scoreMessages, winMessages);
        scoreMessages.add("");
        scoreMessages.add(MinigameMessages.GAME_BAR_BOTTOM.toString());
        scoreMessages.add("");
    }

    @EventHandler
    public void individualTopScores(IndividualTopScoreWinEvent event) {

        // TODO: do something

        // showScores specific to this type of event)
        // minigame.endGame();
        endMinigame();
    }

    @EventHandler
    public void teamTopScores(TeamTopScoreWinEvent event) {

        endMinigame();
    }

    @EventHandler
    public void lastManStanding(LastManStandingWinEvent event) {

        endMinigame();
    }

    @EventHandler
    public void lastTeamStanding(LastTeamStandingWinEvent event) {

        setScoreMessages(new String[] {event.getTeams().get(0).getTeamName(), event.getTeams().get(1).getTeamName()});

        endMinigame();
    }

//    /**
//     * Generate a list of the top performing players for end game scores.
//     *
//     * @param statType The stat type used to reach a end game win condition.
//     */
//    public Map<Player, Double> generateTopScores(StatType statType) {
//        // Build easy map for score sorting.
//        Map<Player, Double> unsortedScores = new HashMap<>();
//
//        for (Player player : playerStats.keySet()) {
//            unsortedScores.put(player, playerStats.get(player).get(statType));
//        }
//
//        // Sort the players scores.
//        MyComparator comparator = new MyComparator(unsortedScores);
//        Map<Player, Double> scores = new TreeMap<>(comparator);
//        scores.putAll(unsortedScores);
//
//        return scores;
//    }

    /**
     * Code by: Sujan Reddy A
     * Date: Feb 10 '13 at 6:10
     * Source: https://stackoverflow.com/a/14795215/2865125
     * <p>
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
