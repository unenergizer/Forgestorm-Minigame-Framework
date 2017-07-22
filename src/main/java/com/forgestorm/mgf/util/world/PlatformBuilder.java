package com.forgestorm.mgf.util.world;

import com.forgestorm.mgf.constants.PedestalLocations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

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

public class PlatformBuilder {

    /**
     * Sets blocks between two locations.
     *
     * @param platformLocations A list of two locations for the purpose of setting
     *                          the platform.
     * @param material          The material to set at the location.
     */
    @Deprecated
    public void setPlatform(List<Location> platformLocations, Material material) {
        Location loc1 = platformLocations.get(0);
        Location loc2 = platformLocations.get(1);

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    block.setType(material);
                }
            }
        }
    }

    /**
     * This will create the pedestal platform for an entity to stand on.
     *
     * @param world    The world to place the pedestal in.
     * @param location The location or xyz coordinate to place the block.
     * @param material The material to set as the platform.
     */
    public void setPlatform(World world, PedestalLocations location, Material material) {
        Block block = location.getLocation(world).getBlock();
        block.setType(material);

        Block slab = location.getLocation(world).add(0, 1, 0).getBlock();
        slab.setType(Material.STEP);
    }

    /**
     * This will remove the pedestal platform from the lobby world.
     *
     * @param world             The world to remove the pedestal from.
     * @param pedestalLocations The location of the pedestal
     */
    public void clearPlatform(World world, List<PedestalLocations> pedestalLocations) {
        for (PedestalLocations pedLocs : pedestalLocations) {
            Block block = pedLocs.getLocation(world).getBlock();
            block.setType(Material.AIR);

            Block slab = pedLocs.getLocation(world).add(0, 1, 0).getBlock();
            slab.setType(Material.AIR);
        }
    }
}
