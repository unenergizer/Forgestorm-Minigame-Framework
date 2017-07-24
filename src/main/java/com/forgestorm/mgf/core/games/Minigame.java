package com.forgestorm.mgf.core.games;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameArena;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.List;

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
@Getter
@Setter
public abstract class Minigame implements Listener {

    protected final MinigameFramework plugin;
    // Game options
    protected boolean cancelBlockBreak = true;
    protected boolean cancelBlockPlace = true;
    protected boolean cancelPVE = true;
    protected boolean cancelPVP = true;
    protected boolean cancelFoodLevelChange = true;
    protected boolean cancelPlayerDropItems = true;
    protected boolean cancelPlayerPickupItems = true;
    private boolean gameOver = false;

    public Minigame(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    public void initListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public abstract void setupGame();

    public abstract void disableGame();

    public abstract void setupPlayers();

    public abstract World getLobbyWorld();

    public abstract List<String> getGamePlayTips();

    public abstract List<String> getGamePlayRules();

    public abstract List<Kit> getKits();

    public abstract List<Team> getTeams();

    public abstract List<StatType> getStatTypes();

    public void endMinigame() {
        disableGame();

        // Unregister stat listeners
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        FoodLevelChangeEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);

        // Show Scores
        plugin.getGameManager().getGameArena().setArenaState(GameArena.ArenaState.ARENA_SHOW_SCORES);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getGameManager().getPlayerManager().getPlayerProfileData(event.getPlayer()).isSpectator()) event.setCancelled(true);
        event.setCancelled(cancelBlockBreak);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getGameManager().getPlayerManager().getPlayerProfileData(event.getPlayer()).isSpectator()) event.setCancelled(true);
        event.setCancelled(cancelBlockPlace);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) event.setCancelled(cancelPVE);
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) event.setCancelled(cancelPVP);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (plugin.getGameManager().getPlayerManager().getPlayerProfileData((Player) event.getEntity()).isSpectator()) event.setCancelled(true);
        }
        event.setCancelled(cancelFoodLevelChange);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getGameManager().getPlayerManager().getPlayerProfileData(event.getPlayer()).isSpectator()) event.setCancelled(true);
        event.setCancelled(cancelPlayerDropItems);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (plugin.getGameManager().getPlayerManager().getPlayerProfileData(event.getPlayer()).isSpectator()) event.setCancelled(true);
        event.setCancelled(cancelPlayerPickupItems);
    }
}
