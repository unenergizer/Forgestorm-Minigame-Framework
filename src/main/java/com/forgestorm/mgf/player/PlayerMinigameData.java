package com.forgestorm.mgf.player;

import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

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

@Getter
@Setter
public class PlayerMinigameData {

    /**
     * SAVE TO DATABASE
     */


    /**
     * DO NOT SAVE TO DATABASE
     */
    private UUID uuid;
    private Kit selectedKit;
    private Team selectedTeam;
    private Team queuedTeam;
    private boolean isSpectator = false;
    private Location arenaSpawnLocation;

    public PlayerMinigameData(Player player) {
        uuid = player.getUniqueId();
    }
}
