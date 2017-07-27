package com.forgestorm.mgf.core.games.mowgrass.kits;

import com.forgestorm.mgf.core.kit.Kit;
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

public class GrassPuncher extends Kit {

    public GrassPuncher() {
        super("Grass Puncher",
                ChatColor.GREEN,
                EntityType.CHICKEN,
                Material.STONE,
                new String[]{
                        "All you really need is a lawn mower.",
                        "But you don't get one. Use your hands!"
                }
        );
    }

    @Override
    public void giveKit(Player player) {
        //TODO
    }
}
