package com.forgestorm.mgf.core.winmanagement;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.selectable.team.Team;
import com.forgestorm.mgf.core.winmanagement.winevents.IndividualTopScoreWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastManStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastTeamStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.TeamTopScoreWinEvent;
import com.forgestorm.mgf.util.MapUtil;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
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

    @Getter
    private final List<String> scoreMessages = new ArrayList<>();

    public WinManager(MinigameFramework plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void endMinigame() {
        GameManager.getInstance().getGameSelector().getMinigame().endMinigame();
    }

    public void printScores() {
        for (String messages : scoreMessages) Bukkit.broadcastMessage(messages);
    }

    private void setScoreMessages(List<String> winMessages) {
        scoreMessages.add("");
        scoreMessages.add("");
        scoreMessages.add("");
        scoreMessages.add(MinigameMessages.GAME_BAR_SCORES.toString());
        scoreMessages.add("");
        for (String winMessage : winMessages) scoreMessages.add(winMessage);
        scoreMessages.add("");
        scoreMessages.add(MinigameMessages.GAME_BAR_BOTTOM.toString());
        scoreMessages.add("");
    }

    @EventHandler
    public void individualTopScores(IndividualTopScoreWinEvent event) {
        Map<Player, Integer> temp = MapUtil.sortByValueReverse(event.getPlayerScoreMap());
        List<String> friendlyScoreList = new ArrayList<>();

        int place = 1;
        for (Map.Entry<Player, Integer> entry : temp.entrySet()) {
            if (place > 3) break;
            friendlyScoreList.add(CenterChatText.centerChatMessage(place + ". " + entry.getKey().getDisplayName() + " got a score of " + entry.getValue()));
            place++;
        }

        setScoreMessages(friendlyScoreList);

        endMinigame();
    }

    @EventHandler
    public void teamTopScores(TeamTopScoreWinEvent event) {
        Map<Team, Integer> temp = MapUtil.sortByValueReverse(event.getTeamScoreMap());
        List<String> friendlyScoreList = new ArrayList<>();

        int place = 1;
        for (Map.Entry<Team, Integer> entry : temp.entrySet()) {
            if (place > 3) break;
            friendlyScoreList.add(CenterChatText.centerChatMessage(place + ". " + entry.getKey().getTeamName() + " got a score of " + entry.getValue()));
            place++;
        }
        
        setScoreMessages(friendlyScoreList);

        endMinigame();
    }

    @EventHandler
    public void lastManStanding(LastManStandingWinEvent event) {
        // Get the winning players, and reverse the list.
        List<Player> players = event.getPlayers();
        Collections.reverse(players);
        List<String> friendlyScoreList = new ArrayList<>();

        // Generate top 3 teams
        int place = 1;

        for (Player player : players) {
            if (place > 3) break;
            String prefix = "";
            if (place == 1) prefix = "st";
            if (place == 2) prefix = "nd";
            if (place == 3) prefix = "rd";
            friendlyScoreList.add(CenterChatText.centerChatMessage(player.getDisplayName() + " got " + place + prefix));
            place++;
        }

        // Set score messages
        setScoreMessages(friendlyScoreList);

        // End the minigame
        endMinigame();
    }

    @EventHandler
    public void lastTeamStanding(LastTeamStandingWinEvent event) {
        // Get the winning teams, and reverse the list.
        List<Team> teamList = event.getTeams();
        Collections.reverse(teamList);
        List<String> friendlyScoreList = new ArrayList<>();

        // Generate top 3 teams
        int place = 1;

        for (Team team : teamList) {
            if (place > 3) break;
            friendlyScoreList.add(CenterChatText.centerChatMessage(place + ". " + team.getTeamColor() + team.getTeamName()));
            place++;
        }

        // Set score messages
        setScoreMessages(friendlyScoreList);

        // End the minigame
        endMinigame();
    }
}
