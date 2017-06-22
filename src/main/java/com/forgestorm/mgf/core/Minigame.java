package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.team.Team;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/2/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public abstract class Minigame {


    protected final MinigameFramework plugin;
    @Getter
    @Setter
    private boolean gameOver = false;

    public Minigame(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    public abstract void startGame();

    public abstract void stopGame();

    public abstract List<String> getGamePlayTips();

    public abstract List<String> getGamePlayRules();

    public abstract List<Kit> getKits();

    public abstract List<Team> getTeams();

    public void endMinigame() {
        stopGame();
        plugin.getGameManager().getGameArena().setArenaState(GameArena.ArenaState.ARENA_SHOW_SCORES);
    }
}
