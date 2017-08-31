package com.forgestorm.mgf.core.selectable.team;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/10/2017
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
public class TeamSpawnLocations {
    private final int index;
    private List<Location> locations;
    private int lastTeamSpawnIndex = 0;

    public TeamSpawnLocations(int index, List<Location> locations) {
        this.index = index;
        this.locations = locations;
    }

    /**
     * This will remove the Location reference from the loaded arena world. This allows
     * the arena world to be safely deleted.  Otherwise, files are not removed which
     * will cause chunk files to be locked by the operating system causing holes to
     * appear in the map next time it is loaded.
     */
    public void disable() {
        locations.clear();
        locations = null;
    }
}
