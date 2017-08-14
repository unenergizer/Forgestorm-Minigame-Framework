package com.forgestorm.mgf.core.location;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.location.access.AccessBehavior;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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

public abstract class GameLocation extends BukkitRunnable implements Listener {

    final GameManager gameManager;
    final MinigameFramework plugin;
    final Minigame minigame;

    GameLocation() {
        GameManager gameManager = GameManager.getInstance();
        this.gameManager = gameManager;
        this.plugin = gameManager.getPlugin();
        this.minigame = gameManager.getCurrentMinigame();
    }

    public abstract void setupGameLocation();
    public abstract void destroyGameLocation();
    protected abstract void showCountdown();

    void startCountdown() {
        this.runTaskTimer(plugin, 0, 20);
    }

    void stopCountdown() {
        cancel();
    }

    @Override
    public void run() {
        showCountdown();
    }

    /**
     * Called when a player joins this game location.
     *
     * @param accessBehavior The behavior we want when a player joins.
     * @param player         The player who is joining.
     */
    public void playerJoin(AccessBehavior accessBehavior, Player player) {
        accessBehavior.playerJoin(player);
    }

    /**
     * Called when a player leaves ths game location.
     *
     * @param accessBehavior The behavior we want when a player quits.
     * @param player         The player who is quitting.
     */
    public void playerQuit(AccessBehavior accessBehavior, Player player) {
        accessBehavior.playerQuit(player);
    }

    /**
     * This is a helper method to add all players to the game location.
     *
     * @param accessBehavior The type of player join we want to perform.
     */
    public void allPlayersJoin(AccessBehavior accessBehavior) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerJoin(accessBehavior, player);
        }
    }

    /**
     * This is a helper method to remove all players to the game location.
     *
     * @param accessBehavior The type of player exit we want to perform.
     */
    public void allPlayersQuit(AccessBehavior accessBehavior) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerQuit(accessBehavior, player);
        }
    }
}
