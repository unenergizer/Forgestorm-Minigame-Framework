package com.forgestorm.mgf.core.kit;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.constants.PedestalLocations;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.util.world.PedestalMapping;
import com.forgestorm.mgf.util.world.PlatformBuilder;
import com.forgestorm.spigotcore.constants.UserGroup;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class KitManager implements Listener {

    private final MinigameFramework plugin;
    private final PlatformBuilder platformBuilder;
    private final List<Kit> kitsList;
    private final GameManager gameManager;
    private final Map<Entity, Kit> kitEntities = new HashMap<>();
    private final World lobbyWorld;
    private final List<PedestalLocations> pedestalLocations = new ArrayList<>();

    public KitManager(MinigameFramework plugin, GameManager gameManager, PlatformBuilder platformBuilder, List<Kit> kitsList, World lobbyWorld) {
        this.plugin = plugin;
        this.platformBuilder = platformBuilder;
        this.kitsList = kitsList;
        this.gameManager = gameManager;
        this.lobbyWorld = lobbyWorld;
    }

    /**
     * This will setup kits for the current minigame.
     * This will spawn the platform the kit stands on
     * and it will place the entity on it for kit
     * representation.
     *
     * Registers stat listeners for this class.
     */
    public void setupKits() {

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
            platformBuilder.setPlatform(lobbyWorld, pedLoc, kit.getKitPlatformMaterials());

            // Spawn entities
            spawnEntity(kit, pedLoc);

            kitsSpawned++;
        }

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * This will remove kit related objects from our
     * lobby world. Some things removed are the
     * platforms entities stand on and the entities
     * themselves.
     *
     * Unregister stat listeners for this class
     */
    public void destroyKits() {

        // Remove entities
        for (Entity entity : kitEntities.keySet()) {
            plugin.getSpigotCore().getScoreboardManager().getScoreboard().getTeam(UserGroup.MINIGAME_KIT.getTeamName()).removeEntry(entity.getUniqueId().toString());
            entity.remove();
        }

        // Remove platforms
        platformBuilder.clearPlatform(lobbyWorld, pedestalLocations);

        // Unregister Listeners
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    /**
     * Called when a player clicks on a kit mob.
     *
     * @param player The player who left or right clicked a kit mob.
     * @param entity The entity the player left or right clicked.
     */
    private void toggleKitInteract(Player player, Entity entity) {
        if (!kitEntities.containsKey(entity)) return;

        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
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

    /**
     * This will spawn the entity that players click on
     * for kit selection.
     *
     * @param kit The kit we are spawning an entity for.
     */
    private void spawnEntity(Kit kit, PedestalLocations pedLoc) {

        Location entityLocation = pedLoc.getLocation(lobbyWorld).add(.5, 1.5, .5);

        // Generate and spawn the entity.
        LivingEntity entity = (LivingEntity) lobbyWorld.spawnEntity(entityLocation, kit.getKitEntityType());

        entity.setCustomName(kit.getKitColor() + kit.getKitName());
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);
        entity.setCanPickupItems(false);
        entity.setCollidable(false);
        entity.setAI(false);

        // Fix to make mobs face the correct direction.
        entity.teleport(entityLocation);

        // Add the prefix to the kit entity username.
        plugin.getSpigotCore().getScoreboardManager().getScoreboard().getTeam(UserGroup.MINIGAME_KIT.getTeamName()).addEntry(entity.getUniqueId().toString());

        // Add the kit selection entities UUID's to an array list.
        // This is used to keep track of which one is being clicked for kit selection.
        kitEntities.put(entity, kit);
    }

    @EventHandler
    public void onKitRightClick(PlayerInteractAtEntityEvent event) {
        toggleKitInteract(event.getPlayer(), event.getRightClicked());
    }

    @EventHandler
    public void onKitLeftClick(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            event.setCancelled(true); // Cancel damage
            toggleKitInteract((Player) event.getDamager(), event.getEntity());
        }
    }
}
