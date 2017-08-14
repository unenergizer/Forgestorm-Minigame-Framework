package com.forgestorm.mgf.core.location.access;

import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.location.GameArena;
import com.forgestorm.mgf.player.PlayerMinigameData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/12/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class ArenaSpectatorAccess implements AccessBehavior {
    
    private final GameManager gameManager = GameManager.getInstance();
    private final GameArena gameArena = gameManager.getGameArena();
    
    @Override
    public void playerJoin(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);

        // Set player as spectator in their profile.
        playerMinigameData.setSpectator(true);

        // Clear the inventory
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Show the spectator a boss bar.
        gameArena.getSpectatorBar().showBossBar(player);

        // Show spectator enter messages
        gameManager.getPlugin().getTitleManagerAPI().sendTitles(
                player,
                MinigameMessages.GAME_ARENA_SPECTATOR_TITLE.toString(),
                MinigameMessages.GAME_ARENA_SPECTATOR_SUBTITLE.toString());

        // Set minecraft defaults
        player.setGameMode(GameMode.ADVENTURE);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Give the spectator invisible potion effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));

        // Hide spectators
        gameArena.hideSpectators();

        // Give Spectator tracker menu items and set their selected slot to 0.
        // Changing the cursor slot prevents from accidental server quiting.
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(3, gameArena.getSpectatorServerExit());
        player.getInventory().setItem(4, gameArena.getTrackPlayers());
        player.getInventory().setItem(5, gameArena.getFlySpeed());
    }

    @Override
    public void playerQuit(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
        playerMinigameData.setSelectedTeam(null);
        playerMinigameData.setQueuedTeam(null);
        playerMinigameData.setSelectedKit(null);

        // Set player as non-spectator in their profile.
        playerMinigameData.setSpectator(false);

        // Remove the spectator boss bar.
        gameArena.getSpectatorBar().removeBossBar(player);

        // Set some minecraft defaults
        player.setGameMode(GameMode.SURVIVAL);
        player.setCollidable(true);
        player.setFlySpeed(.1f);
        player.setAllowFlight(false);
        player.setFlying(false);

        // Clear inventory and armor
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Remove spectator invisible potion effect
        for (PotionEffect potionEffect : player.getActivePotionEffects()) player.removePotionEffect(potionEffect.getType());

        // Show hidden players.
        gameArena.showHiddenPlayers();
    }
}
