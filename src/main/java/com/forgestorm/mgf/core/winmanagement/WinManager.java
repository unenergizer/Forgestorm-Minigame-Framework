package com.forgestorm.mgf.core.winmanagement;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.selectable.team.Team;
import com.forgestorm.mgf.core.winmanagement.winevents.IndividualTopScoreWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastManStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.LastTeamStandingWinEvent;
import com.forgestorm.mgf.core.winmanagement.winevents.TeamTopScoreWinEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        // TODO: Create friendly end game score list
        List<String> list = new ArrayList<>();
        list.add(event.getPlayerScoreMap().toString());
        setScoreMessages(list);

        endMinigame();
    }

    @EventHandler
    public void teamTopScores(TeamTopScoreWinEvent event) {
        // TODO: Create friendly end game score list
        endMinigame();
    }

    @EventHandler
    public void lastManStanding(LastManStandingWinEvent event) {
        // TODO: Create friendly end game score list
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
            if (place > 3) continue;
            friendlyScoreList.add(place + ". " + team.getTeamColor() + team.getTeamName());
            place++;
        }

        // Set score messages
        setScoreMessages(friendlyScoreList);

        // End the minigame
        endMinigame();
    }
}
