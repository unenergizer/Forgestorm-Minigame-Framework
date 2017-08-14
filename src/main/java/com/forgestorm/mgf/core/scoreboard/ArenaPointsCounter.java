package com.forgestorm.mgf.core.scoreboard;

import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.util.MapUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/29/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be
 * reproduced, distributed, or transmitted in any form or by any means,
 * including photocopying, recording, or other electronic or mechanical methods,
 * without the prior written permission of the owner.
 */

public class ArenaPointsCounter extends ArenaScoreboard {

    /**
     * Set the scores for the lobby scoreboard.
     */
    public void setBoardData(Map<Player, Integer> scores) {
        Map<Player, Integer> sortedMap = MapUtil.sortByValueReverse(scores);

        for (Player player : Bukkit.getOnlinePlayers()) {
            int line = 1;
            for (Map.Entry<Player, Integer> entry : sortedMap.entrySet()) {
                if (line > scores.size() || line > 15) return;

                Player scoreboardEntry = entry.getKey();
                String score = Integer.toString(entry.getValue());

                titleManagerAPI.setScoreboardValue(player, line, score + " " + ChatColor.GREEN + scoreboardEntry.getDisplayName());

                line++;
            }
        }
    }

    @Override
    public void initBeginningLines(Player scoreboardOwner) {
        int line = 1;
        for (Player scoreboardEntry : Bukkit.getOnlinePlayers()) {
            if (GameManager.getInstance().getPlayerMinigameManager().getPlayerProfileData(scoreboardEntry).isSpectator()) continue;
            titleManagerAPI.setScoreboardValue(scoreboardOwner, line, 0 + " " + ChatColor.GREEN + scoreboardEntry.getDisplayName());
            line++;
        }
    }
}
