package com.forgestorm.mgf.core.games.pirateattack;

import com.forgestorm.mgf.core.GameManager;
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

class PirateAmmoSpawn extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMinigameData playerMinigameData = GameManager.getInstance().getPlayerMinigameManager().getPlayerProfileData(player);
            if (playerMinigameData == null) return;
            if (playerMinigameData.isSpectator()) return;
            checkAmmo(player);
        }
    }

    /**
     * This will check to see if the player needs ammo. If they do, we will add it.
     *  @param player   The player who we are checking.
     *
     */
    private void checkAmmo(Player player) {
        int ammo = 0;

        // Find ammo.
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() != null && item.getType() == Material.SNOW_BALL) {
                ammo += item.getAmount();
            }
        }

        // Add ammo.
        if (ammo < 3) player.getInventory().setItem(0,
                new ItemBuilder(Material.SNOW_BALL).setTitle(ChatColor.YELLOW + "Cannon Balls").setAmount(ammo + 1).build(true));
    }
}
