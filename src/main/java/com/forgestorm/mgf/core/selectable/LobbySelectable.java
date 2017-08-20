package com.forgestorm.mgf.core.selectable;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.PedestalLocations;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.util.world.PlatformBuilder;
import com.forgestorm.spigotcore.constants.UserGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.ArrayList;
import java.util.List;

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

public abstract class LobbySelectable implements Listener {

    protected final MinigameFramework plugin;
    protected final GameManager gameManager;
    protected final Minigame minigame;
    protected final PlatformBuilder platformBuilder = new PlatformBuilder();
    protected final List<PedestalLocations> pedestalLocations = new ArrayList<>();

    public LobbySelectable(Minigame minigame) {
        this.gameManager = GameManager.getInstance();
        this.plugin = gameManager.getPlugin();
        this.minigame = minigame;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setup();
    }

    public void onDisable() {
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        destroy();
        pedestalLocations.clear();
    }

    public abstract void setup();

    public abstract void destroy();

    public abstract void toggleInteract(Player player, Entity entity);

    /**
     * This will spawn the selectable entity in the lobby world.
     *
     * @param name             The name to display over the entity
     * @param type             The type of entity to display.
     * @param pedestalLocation The location to spawn the entity
     * @param userGroup        The usergroup of the entity. Used to put a prefix before its name.
     * @return The generated entity.
     */
    protected LivingEntity spawnSelectableEntity(String name, EntityType type, PedestalLocations pedestalLocation, UserGroup userGroup) {
        World world = minigame.getLobbyWorld();
        Location entityLocation = pedestalLocation.getLocation(world).add(.5, 1.5, .5);

        // Generate and spawn the entity.
        LivingEntity entity = (LivingEntity) world.spawnEntity(entityLocation, type);

        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);
        entity.setCanPickupItems(false);
        entity.setCollidable(false);
        entity.setAI(false);

        // Fix to make mobs face the correct direction.
        entity.teleport(entityLocation);

        // Add the prefix to the entity username.
        plugin.getSpigotCore().getScoreboardManager().addEntityToTeam(entity, userGroup.getTeamName());

        return entity;
    }

    @EventHandler
    public void onTeamRightClick(PlayerInteractAtEntityEvent event) {
        toggleInteract(event.getPlayer(), event.getRightClicked());
    }

    @EventHandler
    public void onTeamLeftClick(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            event.setCancelled(true); // Cancel damage
            toggleInteract((Player) event.getDamager(), event.getEntity());
        }
    }
}
