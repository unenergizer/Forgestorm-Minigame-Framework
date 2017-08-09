package com.forgestorm.mgf.core.games.pirateattack;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/6/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class PirateAmmoSpawn extends BukkitRunnable {

    private final MinigameFramework plugin;

    PirateAmmoSpawn(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMinigameData playerMinigameData = plugin.getGameManager().getPlayerManager().getPlayerProfileData(player);
            if (playerMinigameData == null) return;
            if (playerMinigameData.isSpectator()) return;
            checkAmmo(player, Material.SNOW_BALL);
//            // Team 1
//            if (playerMinigameData.getSelectedTeam().getIndex() == 0) checkAmmo(player, Material.SNOW_BALL);
//
//            // Team 2
//            if (playerMinigameData.getSelectedTeam().getIndex() == 1) checkAmmo(player, Material.ENDER_PEARL);
        }
    }

    /**
     * This will check to see if the player needs ammo. If they do, we will add it.
     *
     * @param player   The player who we are checking.
     * @param material The type of ammo to check for and add.
     */
    private void checkAmmo(Player player, Material material) {
        int ammo = 0;

        // Find ammo.
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() != null && item.getType() == material) {
                ammo += item.getAmount();
            }
        }

        // Add ammo.
        if (ammo < 3) player.getInventory().setItem(0,
                new ItemBuilder(material).setTitle(ChatColor.YELLOW + "Pirate Ammo").setAmount(ammo + 1).build(true));
    }
}
