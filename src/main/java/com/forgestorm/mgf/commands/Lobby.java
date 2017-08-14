package com.forgestorm.mgf.commands;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.location.GameArena;
import com.forgestorm.mgf.core.location.access.ArenaPlayerAccess;
import com.forgestorm.mgf.core.location.access.ArenaSpectatorAccess;
import com.forgestorm.mgf.player.PlayerMinigameManager;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

@AllArgsConstructor
public class Lobby implements CommandExecutor {

    private final MinigameFramework plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GameManager gameManager = GameManager.getInstance();

            // Minigame arena exit.
            // Restore player inventory!!
            if (!gameManager.isInLobby()) {
                GameArena gameArena = gameManager.getGameArena();
                PlayerMinigameManager playerMinigameManager = gameManager.getPlayerMinigameManager();

                // Check if arena player quit or spectator quit.
                if (!gameManager.getPlayerMinigameManager().getPlayerProfileData(player).isSpectator()) {
                    // Arena player quit
                    gameArena.playerQuit(new ArenaPlayerAccess(), player);
                } else {
                    // Spectator player quit
                    gameArena.playerQuit(new ArenaSpectatorAccess(), player);
                }

                // Restore player backup
                playerMinigameManager.restorePlayerInventoryBackup(player);
            }

            // Now teleport to lobby.
            plugin.getSpigotCore().getBungeecord().connectToBungeeServer(player, "hub-01");
        }
        return false;
    }
}
