package com.forgestorm.mgf.core.games.pirateattack.kits;

import com.forgestorm.mgf.core.selectable.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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

public class Pirate extends Kit {

    public Pirate() {
        super("MurderKit", ChatColor.AQUA, EntityType.CAVE_SPIDER, Material.BEDROCK, new String[] { "This is a pirate kit." });
    }

    @Override
    public void giveKit(Player player) {}
}
