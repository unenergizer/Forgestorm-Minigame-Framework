package com.forgestorm.mgf.core.menus;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.spigotcore.menus.Menu;
import com.forgestorm.spigotcore.menus.actions.FlySpeed;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/5/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class SpectatorFlySpeed extends Menu {

    public SpectatorFlySpeed(MinigameFramework plugin) {
        super(plugin.getSpigotCore());
        init("Fly Speed", 1);
        makeMenuItems();
    }

    /**
     * Lets make all the menu items for our spectator menu.
     */
    @Override
    protected void makeMenuItems() {
        ItemStack defaultSpeed, speed1, speed2, speed3, speed4, trackPlayers;

        String noSpeedTitle = MinigameMessages.MENU_ITEM_SPECTATOR_NO_SPEED.toString();
        String speed1Title = MinigameMessages.MENU_ITEM_SPECTATOR_SPEED_1.toString();
        String speed2Title = MinigameMessages.MENU_ITEM_SPECTATOR_SPEED_2.toString();
        String speed3Title = MinigameMessages.MENU_ITEM_SPECTATOR_SPEED_3.toString();
        String speed4Title = MinigameMessages.MENU_ITEM_SPECTATOR_SPEED_4.toString();

        defaultSpeed = new ItemBuilder(Material.MINECART).setTitle(noSpeedTitle).build(true);
        speed1 = new ItemBuilder(Material.HOPPER_MINECART).setTitle(speed1Title).build(true);
        speed2 = new ItemBuilder(Material.POWERED_MINECART).setTitle(speed2Title).build(true);
        speed3 = new ItemBuilder(Material.STORAGE_MINECART).setTitle(speed3Title).build(true);
        speed4 = new ItemBuilder(Material.EXPLOSIVE_MINECART).setTitle(speed4Title).build(true);


        setItem(defaultSpeed, 0, new FlySpeed(0.1f, "You selected the default move speed."));
        setItem(speed1, 1, new FlySpeed(0.3f, "You selected move speed 1."));
        setItem(speed2, 2, new FlySpeed(0.5f, "You selected move speed 2."));
        setItem(speed3, 3, new FlySpeed(0.7f, "You selected move speed 3."));
        setItem(speed4, 4, new FlySpeed(0.9f, "You selected move speed 4."));
    }
}
