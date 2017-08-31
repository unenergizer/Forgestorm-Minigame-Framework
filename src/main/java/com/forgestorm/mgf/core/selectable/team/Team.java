package com.forgestorm.mgf.core.selectable.team;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
public class Team {
    private List<Player> teamPlayers = new ArrayList<>();
    private List<Player> deadPlayers = new ArrayList<>();
    private Queue<Player> queuedPlayers = new ConcurrentLinkedQueue<>();

    private int index; // Represents a team via a number.
    private String teamName;
    private ChatColor teamColor;
    private int teamSizes;
    private EntityType teamEntityType;
    private Material teamPlatformMaterials;
    private String[] teamDescription;

    public Team(int index, String teamName, ChatColor teamColor, int teamSizes, EntityType teamEntityType, Material teamPlatformMaterials, String[] teamDescription) {
        this.index = index;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.teamSizes = teamSizes;
        this.teamEntityType = teamEntityType;
        this.teamPlatformMaterials = teamPlatformMaterials;
        this.teamDescription = teamDescription;
    }
}
