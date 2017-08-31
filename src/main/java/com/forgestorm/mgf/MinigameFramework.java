package com.forgestorm.mgf;

import com.forgestorm.mgf.commands.Admin;
import com.forgestorm.mgf.commands.Lobby;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.world.WorldManager;
import com.forgestorm.spigotcore.SpigotCore;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

@Getter
public class MinigameFramework extends JavaPlugin {
    
    @SuppressWarnings("unchecked")
    private final List<String> configGameList = (List<String>) getConfig().getList("Games");
    private final TitleManagerAPI titleManagerAPI = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManager");
    private final SpigotCore spigotCore = (SpigotCore) Bukkit.getServer().getPluginManager().getPlugin("FS-SpigotCore");

    @Override
    public void onEnable() {
        ColorLogger.LIGHT_PURPLE.printLog("STARTING UP MinigameFramework");

        // Load First
        WorldManager.getInstance().setup(this);
        ColorLogger.LIGHT_PURPLE.printLog("WorldManager started!!");

        // Load Last
        GameManager.getInstance().setup(this);
        ColorLogger.LIGHT_PURPLE.printLog("GameManager started!!");

        // Register commands
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Disable the core manager
        GameManager.getInstance().onDisable();
    }

    private void registerCommands() {
        getCommand("mgadmin").setExecutor(new Admin(this));
        getCommand("lobby").setExecutor(new Lobby(this));
    }
}
