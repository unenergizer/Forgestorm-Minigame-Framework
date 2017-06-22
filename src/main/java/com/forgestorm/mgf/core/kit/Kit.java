package com.forgestorm.mgf.core.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-mgframework
 * DATE: 6/1/2017
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
public class Kit {
    private String kitName;
    private ChatColor kitColor;
    private EntityType kitEntityType;
    private Material kitPlatformMaterials;
    private List<String> kitDescription;
    private List<Location> kitPlatformLocation;
}
