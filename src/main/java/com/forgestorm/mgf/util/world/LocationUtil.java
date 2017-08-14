package com.forgestorm.mgf.util.world;

import org.bukkit.Location;
import org.bukkit.World;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-mgframework
 * DATE: 6/1/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

class LocationUtil {

    public static Location getCenterLocation(Location loc1, Location loc2) {

        //Gets the smallest and largest value in the X and Z plane and
        //puts it in minimum and maximum variables.
        double minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        double minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        double maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        double maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        //Gets the center of the two locations.
        double x = (minX + maxX) / 2;
        double z = (minZ + maxZ) / 2;

        World world = loc1.getWorld();

        return new Location(world, x + .5, loc1.getY() + 1, z + .5);
    }
}
