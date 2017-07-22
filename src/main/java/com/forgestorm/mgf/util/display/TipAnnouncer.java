package com.forgestorm.mgf.util.display;

import com.forgestorm.mgf.MinigameFramework;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

public class TipAnnouncer {

    private final MinigameFramework plugin;
    private final List<String> gameTips;
    private int tipDisplayed;
    private boolean showTips = true;

    public TipAnnouncer(MinigameFramework plugin, List<String> gameTips) {
        this.plugin = plugin;
        this.gameTips = gameTips;
        startTipMessages();
    }

    /**
     * This starts the thread that will loop over and over displaying
     * tips and other useful information to the player.
     */
    private void startTipMessages() {
        showTips = true;
        int numberOfTips = gameTips.size();
        int gameTipTime = 20 * 30;

        //Start a repeating task.
        new BukkitRunnable() {

            @Override
            public void run() {

                if (showTips) {
                    String gameTip = gameTips.get(tipDisplayed);

                    //Show the tip.
                    sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tip"
                            + ChatColor.YELLOW + " #"
                            + Integer.toString(tipDisplayed + 1)
                            + ChatColor.DARK_GRAY + ChatColor.BOLD + ": "
                            + ChatColor.WHITE + gameTip);

                    //Setup to display the next tip.
                    if ((tipDisplayed + 1) == numberOfTips) {
                        //Reset the tip count.  All tips have been displayed.
                        tipDisplayed = 0;
                    } else {
                        //Increment the tip count to display the next tip.
                        tipDisplayed++;
                    }
                } else {
                    //Cancel the tip rotation.
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, gameTipTime);
    }

    /**
     * This will broadcast a core play tip to all players online.
     *
     * @param message The message we will send to all players.
     */
    private void sendMessage(String message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.hasMetadata("NPC")) return;

            //Send Message
            players.sendMessage(message);

            //Play Sound
            players.playSound(players.getEyeLocation(), Sound.UI_BUTTON_CLICK, .5F, .2f);
        }
    }

    /**
     * Set showTips to false to cancel all tip messages.
     *
     * @param showTips A boolean value that can stop tip
     *                 messages form being displayed.
     */
    public void setShowTips(boolean showTips) {
        this.showTips = showTips;
    }
}
