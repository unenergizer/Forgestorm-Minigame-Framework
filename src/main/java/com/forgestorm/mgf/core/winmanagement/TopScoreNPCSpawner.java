package com.forgestorm.mgf.core.winmanagement;

import com.forgestorm.mgf.core.GameManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/22/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class TopScoreNPCSpawner {

    private final List<NPC> npcs = new ArrayList<>();

    /**
     * This will spawn a NPC at a given location.
     *
     * @param player         The player to spawn a NPC for.
     * @param placeLocations The location to spawn them.
     */
    public void spawnNPC(Player player, PlaceLocations placeLocations) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getDisplayName());
        npc.teleport(placeLocations.getLocation(
                GameManager.getInstance().getGameSelector().getMinigame().getLobbyWorld()),
                PlayerTeleportEvent.TeleportCause.PLUGIN);

        npcs.add(npc);
    }

    /**
     * Remove all NPCs.
     */
    public void clearNPCs() {
        for (NPC npc : npcs) {
            npc.despawn();
        }

        npcs.clear();
    }
}
