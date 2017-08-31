package com.forgestorm.mgf.core.location.access;

import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/12/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class ArenaPlayerAccess implements AccessBehavior {

    private final GameManager gameManager = GameManager.getInstance();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ColorLogger.DARK_RED.printLog(this.getClass().getSimpleName() + " class object destroyed.");
    }

    @Override
    public void playerJoin(Player player) {
//        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
//        TeamSpawnLocations teamLocations = gameManager.getTeamSpawnLocations().get(playerMinigameData.getSelectedTeam().getIndex());
//        int lastTeamSpawnIndex = teamLocations.getLastTeamSpawnIndex();

        // Clear the inventory
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Teleport player
//        Location location = teamLocations.getLocations().get(lastTeamSpawnIndex);
//        player.teleport(location);
//        playerMinigameData.setArenaSpawnLocation(location);
//
//        // Increment teleport counter
//        if (lastTeamSpawnIndex > (teamLocations.getLocations().size() - 1)) {
//            teamLocations.setLastTeamSpawnIndex(0);
//        } else {
//            teamLocations.setLastTeamSpawnIndex(lastTeamSpawnIndex + 1);
//        }
    }

    @Override
    public void playerQuit(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
        playerMinigameData.setSelectedTeam(null);
        playerMinigameData.setQueuedTeam(null);
        playerMinigameData.setSelectedKit(null);

        // Remove potion effects
        for (PotionEffect potionEffect : player.getActivePotionEffects()) player.removePotionEffect(potionEffect.getType());

        // Clear inventory and armor
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
}
