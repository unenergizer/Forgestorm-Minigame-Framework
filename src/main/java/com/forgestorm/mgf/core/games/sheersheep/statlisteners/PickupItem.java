package com.forgestorm.mgf.core.games.sheersheep.statlisteners;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.score.statlisteners.StatListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/26/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

@SuppressWarnings("unused")
public class PickupItem implements StatListener {

    private MinigameFramework plugin;

    public PickupItem(MinigameFramework plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void deregister() {
        PlayerPickupItemEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickUp(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() != Material.WOOL) return;
        event.setCancelled(false);
        Player player = event.getPlayer();
        int amount = event.getItem().getItemStack().getAmount();
        plugin.getGameManager().getScoreManager().addStat(StatType.PICKUP_ITEM, player, amount);
        event.getPlayer().sendMessage(ChatColor.GREEN + "+" + ChatColor.RESET + amount);
    }
}
