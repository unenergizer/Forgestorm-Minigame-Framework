package com.forgestorm.mgf.util.world;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/20/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class PedestalMapping {

    private static final int maxNumberOfPedestals = 9;

    /**
     * Figure out how many pedestals to omit from the left side over.
     * https://stackoverflow.com/questions/45046897/centering-a-line-of-numbers-below-a-main-line
     *
     * @param pedestalAmount The amount of team or kit pedestals to be used.
     * @return The number of omitted pedestal locations from the left side
     */
    public int getShiftAmount(int pedestalAmount) {
        int difference = maxNumberOfPedestals - pedestalAmount;

        if ((pedestalAmount % 2) == 0) {
            return (int) Math.ceil((difference - 1) / 2);
        } else {
            return difference / 2;
        }
    }
}
