package com.forgestorm.mgf.constants;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/20/2017
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
public enum PedestalLocations {
    // X, Y, Z, YAW, PITCH
    KIT_1(17, 97, 4, 102, 15.5),
    KIT_2(16, 97, 6, 109.4, 15.5),
    KIT_3(15, 97, 8, 117.3, 15.5),
    KIT_4(14, 97, 10, 125.3, 15.5),
    KIT_5(12, 97, 12, 135, 15.5),
    KIT_6(10, 97, 14, 114.8, 15.5),
    KIT_7(8, 97, 15, 152.3, 15.5),
    KIT_8(6, 97, 16, 160, 15.5),
    KIT_9(4, 97, 17, 168, 15.5),

    TEAM_1(-17, 97, -4, -77.8, 15.5),
    TEAM_2(-16, 97, -6, -70.3, 15.5),
    TEAM_3(-15, 97, -8, -62.7, 15.5),
    TEAM_4(-14, 97, -10, -54.9, 15.5),
    TEAM_5(-12, 97, -12, -45.3, 15.5),
    TEAM_6(-10, 97, -14, -35.2, 15.5),
    TEAM_7(-8, 97, -15, -27.6, 15.5),
    TEAM_8(-6, 97, -16, -19.9, 15.5),
    TEAM_9(-4, 97, -17, -12.1, 15.5);

    private final int x;
    private final int y;
    private final int z;
    private final double yaw;
    private final double pitch;

    PedestalLocations(int x, int y, int z, double yaw, double pitch) {
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
