package com.forgestorm.mgf.core.games.sheersheep.kits;

import com.forgestorm.mgf.core.kit.Kit;
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

public class KnifeParty extends Kit {

    public KnifeParty() {
        super("Knife Party",
                ChatColor.GREEN,
                EntityType.PIG,
                Material.STONE,
                new String[]{
                        "Knife them till the wool falls off!"
                }
        );
    }

    @Override
    public void giveKit(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.GOLD_SWORD).setTitle("Golden Knife").build(true));
    }
}
