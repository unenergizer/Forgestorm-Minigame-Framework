package com.forgestorm.mgf.core.games.infection.kits;

import com.forgestorm.mgf.core.selectable.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/15/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class BasicInfectionKit extends Kit {

    public BasicInfectionKit() {
        super(
                "Infection Kit",
                ChatColor.RED,
                EntityType.IRON_GOLEM,
                Material.STONE,
                new String[] {"This is the basic infection kit."});
    }

    @Override
    public void giveKit(Player player) {
        player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
    }
}
