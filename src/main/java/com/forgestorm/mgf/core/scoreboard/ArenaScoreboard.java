package com.forgestorm.mgf.core.scoreboard;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.spigotcore.constants.SpigotCoreMessages;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import org.bukkit.Bukkit;
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
public abstract class ArenaScoreboard {

    private final MinigameFramework plugin;
    final TitleManagerAPI titleManagerAPI;

    ArenaScoreboard() {
        this.plugin = GameManager.getInstance().getPlugin();
        this.titleManagerAPI = plugin.getTitleManagerAPI();
    }

    protected abstract void initBeginningLines(Player scoreboardOwner);

    /**
     * Gives all players a scoreboard.
     */
    public void addAllPlayers() {
        // Give scoreboard
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Tarkan board setup.
            titleManagerAPI.giveScoreboard(player);

            // Set scoreboard title.
            titleManagerAPI.setScoreboardTitle(player, SpigotCoreMessages.SCOREBOARD_TITLE.toString());
        }

        // Give all the players the scoreboard.
        for (Player player : Bukkit.getOnlinePlayers()) {
            initBeginningLines(player);
        }
    }

    /**
     * Removes all player scoreboards.
     */
    public void removeAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!titleManagerAPI.hasScoreboard(player)) return;
            titleManagerAPI.removeScoreboard(player);
        }
    }
}
