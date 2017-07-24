package com.forgestorm.mgf.player;

import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    private Player player;
    private UUID uuid;
    private Kit selectedKit;
    private Team selectedTeam;
    private Team queuedTeam;
    private boolean isSpectator = false;
    private Location arenaSpawnLocation;
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;

    PlayerMinigameData(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
    }

    /**
     * This will temporarily save the players inventory
     * and armor contents right before they join a
     * minigame.
     */
    public void backupInventoryContents() {
        inventoryContents = player.getInventory().getContents();
        armorContents = player.getInventory().getArmorContents();
    }

    /**
     * When a minigame is over, we will restore the players
     * inventory and armor contents.
     */
    public void restoreInventoryContents() {
        if (inventoryContents != null) player.getInventory().setContents(inventoryContents);
        if (armorContents != null) player.getInventory().setArmorContents(armorContents);

        // Wipe the backup contents to prevent duping.
        inventoryContents = null;
        armorContents = null;
    }
}
