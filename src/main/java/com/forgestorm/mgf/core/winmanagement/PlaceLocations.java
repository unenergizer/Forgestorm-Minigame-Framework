package com.forgestorm.mgf.core.winmanagement;

import org.bukkit.Location;
import org.bukkit.World;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/22/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public enum PlaceLocations {

    FIRST(11, 100.5, -10, 45, 30),
    SECOND(6, 99.5, -13, 84, 19),
    THIRD(14, 98.5, -5, 6, 24);

    private final double x;
    private final double y;
    private final double z;
    private final double yaw;
    private final double pitch;

    PlaceLocations(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location getLocation(World world) {
        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }
}
