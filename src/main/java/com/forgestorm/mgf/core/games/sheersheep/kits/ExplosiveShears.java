package com.forgestorm.mgf.core.games.sheersheep.kits;

import com.forgestorm.mgf.core.selectable.kit.Kit;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/27/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class ExplosiveShears extends Kit {

    public ExplosiveShears() {
        super("Explosive Shears",
                ChatColor.GREEN,
                EntityType.CHICKEN,
                Material.STONE,
                new String[]{
                        "Blow up the sheep!",
                        "Get as much wool as you can!"
                }
        );
    }

    @Override
    public void giveKit(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.SHEARS).setTitle("Explosive Shears").build(true));
    }
}
