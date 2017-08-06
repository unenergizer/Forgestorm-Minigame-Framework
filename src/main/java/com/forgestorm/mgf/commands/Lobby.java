package com.forgestorm.mgf.commands;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameArena;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
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

            // Minigame arena exit.
            // Restore player inventory!!
            if (!plugin.getGameManager().isInLobby()) {
                GameArena gameArena = plugin.getGameManager().getGameArena();

                // Check if arena player quit or spectator quit.
                if (!plugin.getGameManager().getPlayerManager().getPlayerProfileData(player).isSpectator()) {
                    // Arena player quit
                    ColorLogger.INFO.printLog("/lobby command ran! " + player.getDisplayName() + " arena quit!");
                    gameArena.removeArenaPlayer(player, true);

                } else {
                    // Spectator player quit
				    gameArena.removeSpectator(player);
                    ColorLogger.INFO.printLog("/lobby command ran! " + player.getDisplayName() + " spectator quit!");
                }
            }

            // Now teleport to lobby.
            plugin.getSpigotCore().getBungeecord().connectToBungeeServer(player, "hub-01");
        }
        return false;
    }
}
