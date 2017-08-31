package com.forgestorm.mgf.core;

import com.forgestorm.mgf.core.games.GameType;
import com.forgestorm.mgf.core.games.Minigame;
import lombok.Getter;

import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/27/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class GameSelector {

    private final List<String> gamesToPlay;
    private final int totalGames;
    private int currentGameIndex = 0;
    @Getter
    private GameType minigameType;
    @Getter
    private Minigame minigame;

    GameSelector() {
        //noinspection unchecked
        gamesToPlay = (List<String>) GameManager.getInstance().getPlugin().getConfig().getList("Games");
        totalGames = gamesToPlay.size() - 1;
    }

    void assignNextGame() {
        // Basic game selection based on array list index.
        currentGameIndex++;

        // Go back to first game if needed.
        if (currentGameIndex > totalGames) currentGameIndex = 0;

        // Grab game selected.
        minigameType = GameType.valueOf(gamesToPlay.get(currentGameIndex));
        minigame = minigameType.getMinigame();
        minigame.initMinigameLists();
    }
}
