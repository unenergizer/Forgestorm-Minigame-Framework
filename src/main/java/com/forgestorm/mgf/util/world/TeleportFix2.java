package com.forgestorm.mgf.util.world;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/21/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class TeleportFix2 {

    public static void fixTeleport(Player teleportedPlayer) {
        // Hide each player from the teleported player.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            teleportedPlayer.hidePlayer(player);
            player.hidePlayer(teleportedPlayer);
        }

        // Unhide/show each player the teleported player.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            teleportedPlayer.showPlayer(player);
            player.showPlayer(teleportedPlayer);
        }
    }
}
