package com.forgestorm.mgf.core.selectable.kit;

import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.constants.PedestalLocations;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.selectable.LobbySelectable;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.util.world.PedestalMapping;
import com.forgestorm.spigotcore.constants.UserGroup;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/17/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class KitSelectable extends LobbySelectable {

    private final Map<LivingEntity, Kit> kitEntities = new HashMap<>();

    public KitSelectable(Minigame minigame) {
        super(minigame);
    }

    @Override
    public void setup() {
        List<Kit> kitsList = minigame.getKitList();

        // Determine the number of kits and get the center pedestal locations appropriately.
        PedestalMapping pedestalMapping = new PedestalMapping();
        int shiftOver = pedestalMapping.getShiftAmount(kitsList.size());

        int kitsSpawned = 0;

        // Spawn the kits
        for (Kit kit : kitsList) {

            // Get platform location
            PedestalLocations pedLoc = PedestalLocations.values()[shiftOver + kitsSpawned];
            pedestalLocations.add(pedLoc);

            // Spawn platform
            platformBuilder.setPlatform(minigame.getLobbyWorld(), pedLoc, kit.getKitPlatformMaterials());

            // Spawn entities
            LivingEntity entity = spawnSelectableEntity(
                    kit.getKitColor() + kit.getKitName(),
                    kit.getKitEntityType(), pedLoc,
                    UserGroup.MINIGAME_KIT);

            // Add the kit selection entities UUID's to an array list.
            // This is used to keep track of which one is being clicked for kit selection.
            kitEntities.put(entity, kit);

            kitsSpawned++;
        }
    }

    @Override
    public void destroy() {
        // Remove entities
        for (LivingEntity entity : kitEntities.keySet()) {
            plugin.getSpigotCore().getScoreboardManager().removeEntityFromTeam(entity, UserGroup.MINIGAME_KIT.getTeamName());
            entity.remove();
        }

        // Remove platforms
        platformBuilder.clearPlatform(minigame.getLobbyWorld(), pedestalLocations);

        // Clear entity map
        kitEntities.clear();
    }

    @Override
    public void toggleInteract(Player player, Entity entity) {
        if (!kitEntities.containsKey(entity)) return;

        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
        Kit clickedKit = kitEntities.get(entity);
        Kit currentKit = playerMinigameData.getSelectedKit();
        String sameKitMessage = "";

        boolean clickedSameKit = false;
        if (currentKit != null) {
            if (currentKit.equals(clickedKit)) {
                clickedSameKit = true;
            }
        }

        //If the player has interacted with a team they are on, add a little message to the description.
        if (clickedSameKit) {
            sameKitMessage = " " + MinigameMessages.KIT_ALREADY_HAVE_KIT.toString();
        }

        //Set the the players kit.
        playerMinigameData.setSelectedKit(clickedKit);

        //Set player a confirmation message.
        player.sendMessage("");
        player.sendMessage(MinigameMessages.GAME_BAR_KIT.toString());
        player.sendMessage("");
        player.sendMessage(CenterChatText.centerChatMessage(ChatColor.GRAY + "Kit: " +
                clickedKit.getKitColor() + clickedKit.getKitName() + sameKitMessage));
        player.sendMessage("");

        for (String description : clickedKit.getKitDescription()) {
            String message = CenterChatText.centerChatMessage(ChatColor.YELLOW + description);
            player.sendMessage(message);
        }

        player.sendMessage("");
        player.sendMessage(MinigameMessages.GAME_BAR_BOTTOM.toString());
        player.sendMessage("");

        //Play a confirmation sound.
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, .5f, .6f);

        //Update the lobby scoreboard.
        gameManager.getGameLobby().getTarkanLobbyScoreboard().updatePlayerKit(player, playerMinigameData);
    }
}
