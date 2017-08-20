package com.forgestorm.mgf.core.games;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.ArenaState;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.selectable.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.selectable.team.Team;
import com.forgestorm.mgf.player.PlayerMinigameManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
    private List<String> gamePlayTipsList;
    private List<String> gamePlayRulesList;
    private List<Team> teamList;
    private List<Kit> kitList;
    private List<StatType> statTypeList;
    // Game options
    protected boolean cancelBlockBreak = true;
    protected boolean cancelBlockPlace = true;
    protected boolean cancelPVE = true;
    protected boolean cancelPVP = true;
    protected boolean cancelFoodLevelChange = true;
    protected boolean cancelPlayerDropItems = true;
    protected boolean cancelPlayerPickupItems = true;
    protected boolean cancelCreatureSpawn = true;
    private boolean gameOver = false;

    protected Minigame(MinigameFramework plugin) {
        this.plugin = plugin;
    }

    /**
     * Preinitialize minigame lists.
     */
    public void initMinigameLists() {
        gamePlayTipsList = getGamePlayTips();
        gamePlayRulesList = getGamePlayRules();
        teamList = getTeams();
        kitList = getKits();
        statTypeList = getStatTypes();
    }

    public void initListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public abstract void setupGame();

    protected abstract void disableGame();

    public abstract World getLobbyWorld();

    protected abstract List<String> getGamePlayTips();

    protected abstract List<String> getGamePlayRules();

    protected abstract List<Kit> getKits();

    protected abstract List<Team> getTeams();

    protected abstract List<StatType> getStatTypes();

    public void setupPlayers() {
        PlayerMinigameManager playerMinigameManager = GameManager.getInstance().getPlayerMinigameManager();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMinigameData playerMinigameData = playerMinigameManager.getPlayerProfileData(player);
            if (playerMinigameData.isSpectator()) return;

            // Give the player their kit
            playerMinigameData.getSelectedKit().giveKit(player);

            // Set their held item to slot 0.
            player.getInventory().setHeldItemSlot(0);
        }
    }

    public void endMinigame() {
        disableGame();

        // Unregister stat listeners
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        FoodLevelChangeEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        CreatureSpawnEvent.getHandlerList().unregister(this);

        // Show Scores
        GameManager.getInstance().getGameArena().setArenaState(ArenaState.ARENA_SHOW_SCORES);
    }

    /**
     * Helper method to get the players MinigameData.
     *
     * @param player The player we need the data for.
     * @return The players MiniGameData profile info.
     */
    protected PlayerMinigameData getPlayerMinigameData(Player player) {
        return GameManager.getInstance().getPlayerMinigameManager().getPlayerProfileData(player);
    }

    /**
     * Gets if the player is a spectator or not.
     * @param entity The entity we want to check.
     * @return True if the player is a spectator, false otherwise.
     */
    private boolean isSpectator(Entity entity) {
        return entity instanceof Player && getPlayerMinigameData((Player) entity).isSpectator();
    }

    /**
     * Gets if the player is a spectator or not.
     * @param player The player we want to check.
     * @return True if the player is a spectator, false otherwise.
     */
    private boolean isSpectator(Player player) {
        return getPlayerMinigameData(player).isSpectator();
    }

    /**
     * Helper method to kill the player and move them to spectator.
     *
     * @param player The player to kill.
     */
    protected void killPlayer(Player player) {
        GameManager.getInstance().getGameArena().killPlayer(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
        event.setCancelled(cancelBlockBreak);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
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
        if (isSpectator(event.getEntity())) event.setCancelled(true);
        event.setCancelled(cancelFoodLevelChange);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
        event.setCancelled(cancelPlayerDropItems);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
        event.setCancelled(cancelPlayerPickupItems);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(cancelCreatureSpawn);
    }
}
