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

    private final Minigame minigame;
    @Getter
    private final List<String> scoreMessages = new ArrayList<>();

    public WinManager(MinigameFramework plugin, Minigame minigame) {
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

        setScoreMessages(new String[] {event.getPlayerScoreMap().toString()});

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
}
