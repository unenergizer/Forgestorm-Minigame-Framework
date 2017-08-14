package com.forgestorm.mgf.core.winmanagement.winevents;

import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.core.winmanagement.ScoreData;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
@Getter
public class TeamTopScoreWinEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Map<Team, ScoreData> teamScoreMap;
    private final String unit;

    public TeamTopScoreWinEvent(Map<Team, ScoreData> teamScoreMap, String unit) {
        this.teamScoreMap = teamScoreMap;
        this.unit = unit;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}