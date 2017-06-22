package com.forgestorm.mgf.core.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 6/7/2017
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
@Setter
@AllArgsConstructor
public class WorldData {
    private final int worldIndex; // The index number of the world in the list.
    private final String worldName; // The file name of the world.
}
