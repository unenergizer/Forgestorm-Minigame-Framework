package com.forgestorm.mgf.core.location.access;

import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.location.GameLobby;
import com.forgestorm.mgf.util.world.TeleportFix2;
import com.forgestorm.mgf.player.PlayerMinigameData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

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

public class LobbyAccess implements AccessBehavior {

    private final GameManager gameManager = GameManager.getInstance();
    private final GameLobby gameLobby = gameManager.getGameLobby();

    @Override
    public void playerJoin(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);

        // Set default kit
        playerMinigameData.setSelectedKit(gameManager.getCurrentMinigame().getKits().get(0));

        // Set default team
        gameLobby.getTeamManager().initPlayer(player);

        // Send the player the boss bar.
        gameLobby.getBar().showBossBar(player);

        // Lets change some player Bukkit/Spigot defaults
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFireTicks(0);

        // Teleport the player to the main spawn.
        player.teleport(gameLobby.getSpawn());

        // Setup player for double jump.
        gameLobby.getDoubleJump().setupPlayer(player);

        // Do teleport fix!
        TeleportFix2.fixTeleport(player);

        // Add the scoreboard if the players profile has been loaded in SpigotCore plugin.
        if (gameManager.getPlugin().getSpigotCore().getProfileManager().getProfile(player) != null)
            gameLobby.getTarkanLobbyScoreboard().addPlayer(player);
    }

    @Override
    public void playerQuit(Player player) {
        // Remove the scoreboard.
        gameLobby.getTarkanLobbyScoreboard().removePlayer(player);

        // Remove the boss bar.
        gameLobby.getBar().removeBossBar(player);

        // Remove player double jump.
        gameLobby.getDoubleJump().removePlayer(player);
    }
}
