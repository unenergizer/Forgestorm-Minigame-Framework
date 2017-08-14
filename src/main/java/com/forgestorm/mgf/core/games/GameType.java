package com.forgestorm.mgf.core.games;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.mobmurder.MobMurder;
import com.forgestorm.mgf.core.games.oitc.OneInTheChamber;
import com.forgestorm.mgf.core.games.pirateattack.PirateAttack;
import com.forgestorm.mgf.core.games.sheersheep.SheerSheep;
import com.forgestorm.mgf.core.games.mowgrass.MowGrass;

import java.lang.reflect.InvocationTargetException;

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

@SuppressWarnings("unused")
public enum GameType {

    MOB_MURDER(MobMurder.class, "Mob Murder"),
    OITC(OneInTheChamber.class, "One In the Chamber"),
    PIRATE_ATTACK(PirateAttack.class, "Pirate Attack"),
    SHEER_SHEEP(SheerSheep.class, "Sheer Sheep"),
    MOW_GRASS(MowGrass.class, "Mow Grass");

    private final Class<? extends Minigame> clazz;
    private final String friendlyName;

    GameType(Class<? extends Minigame> clazz, String friendlyName) {
        this.clazz = clazz;
        this.friendlyName = friendlyName;
    }

    /**
     * Grabs a friendly name for display throughout the framework.
     *
     * @return A text friendly name for display throughout the framework.
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Grabs the main core class for a particular minigame.
     *
     * @param plugin The main instance of this plugin.
     * @return The main core class for the selected minigame.
     */
    public Minigame getMinigame(MinigameFramework plugin) {
        try {
            return clazz.getConstructor(MinigameFramework.class).newInstance(plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFileName() {
        return this.toString() + ".yml";
    }
}
