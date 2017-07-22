package com.forgestorm.mgf.core.score;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.sheersheep.statlisteners.PickupItem;
import com.forgestorm.mgf.core.score.statlisteners.FirstKill;
import com.forgestorm.mgf.core.score.statlisteners.StatListener;

import java.lang.reflect.InvocationTargetException;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/22/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public enum StatType {

    FIRST_KILL(FirstKill.class),
    PICKUP_ITEM(PickupItem.class);

    private Class<? extends StatListener> listener;

    StatType(Class<? extends StatListener> listener) {
        this.listener = listener;
    }

    public StatListener registerListener(MinigameFramework plugin) {
        try {
            return listener.getConstructor(MinigameFramework.class).newInstance(plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
