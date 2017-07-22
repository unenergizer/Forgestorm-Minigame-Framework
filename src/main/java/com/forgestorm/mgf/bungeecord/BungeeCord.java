package com.forgestorm.mgf.bungeecord;

import com.forgestorm.mgf.MinigameFramework;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Logger;

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

public class BungeeCord implements PluginMessageListener, Listener {
	
	private final MinigameFramework plugin;
	private final Logger log = Logger.getLogger("Minecraft");
	
	public BungeeCord(MinigameFramework plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * This will connect a player to a different server.
	 * @param player The player we will move.
	 * @param server The server the player will move too.
	 */
	public void connectToBungeeServer(Player player, String server) {
		//Send connection message.
		player.sendMessage(ChatColor.RED + "Connecting you to server \"" + ChatColor.YELLOW + server + ChatColor.RED + "\"...");
		
		try {
			Messenger messenger = Bukkit.getMessenger();
			if (!messenger.isOutgoingChannelRegistered(plugin, "BungeeCord")) {
				messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");
			}
			
			if (server.length() == 0) {
				player.sendMessage("&cThe server name was empty!");
				return;
			}
			
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteArray);

			out.writeUTF("Connect");
			out.writeUTF(server);

			player.sendPluginMessage(plugin, "BungeeCord", byteArray.toByteArray());
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Could not handle BungeeCord command from " + player.getName() + ": tried to connect to \"" + server + "\".");
        }
	}

    /**
     * This will disable the bungee cord instance on server quit or server restarts.
     */
	public void onDisable() {
	    PlayerInteractEvent.getHandlerList().unregister(this);
    }

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) { return;}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR)) ||
				(!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))) {
			return;
		}
		if (event.getItem() == null) return;

		//Send player back to the main hub.
		if (event.getItem().getType() == Material.WATCH) {
			plugin.getBungeecord().connectToBungeeServer(event.getPlayer(), "hub-01");
		}
	}
}
