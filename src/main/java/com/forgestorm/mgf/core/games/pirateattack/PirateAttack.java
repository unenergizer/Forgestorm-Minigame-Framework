package com.forgestorm.mgf.core.games.pirateattack;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.pirateattack.kits.Pirate;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.ScoreData;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.player.PlayerMinigameData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;
import java.util.List;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/6/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class PirateAttack extends Minigame {

    private PirateAmmoSpawn pirateAmmoSpawn;

    public PirateAttack(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        pirateAmmoSpawn = new PirateAmmoSpawn(plugin);
        pirateAmmoSpawn.runTaskTimer(plugin, 0, 20 * 3);

        plugin.getGameManager().getScoreManager().setTestableWinCondition(
                (statType, player) -> {


                    return false;
                }
        );

    }

    @Override
    public void disableGame() {
        pirateAmmoSpawn.cancel();
        ProjectileHitEvent.getHandlerList().unregister(this);
    }

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Uhh...");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Do not get blown up.");
        return rules;
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();

        kits.add(new Pirate());

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(
                0,
                "Blue Team",
                ChatColor.BLUE,
                8,
                EntityType.GUARDIAN,
                Material.STONE,
                new String[]{"Blue team rules!"}));
        teams.add(new Team(
                1,
                "Red Team",
                ChatColor.RED,
                8,
                EntityType.COW,
                Material.STONE,
                new String[]{"Red team rocks!"}));
        return teams;
    }

    @Override
    public List<ScoreData> getScoreData() {
        List<ScoreData> scoreData = new ArrayList<>();
        scoreData.add(new ScoreData(StatType.FIRST_KILL, true, 1.0));
        return scoreData;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();

        PlayerMinigameData playerMinigameData = plugin.getGameManager().getPlayerManager().getPlayerProfileData(player);

        if (playerMinigameData.isSpectator()) return;

        if (playerMinigameData.getSelectedTeam().getIndex() == 0) {
            if (projectile.getLocation().getX() > 0) return;
            projectile.getWorld().createExplosion(projectile.getLocation(), 4);
        }

        if (playerMinigameData.getSelectedTeam().getIndex() == 1) {
            if (projectile.getLocation().getX() < 0) return;
            projectile.getWorld().createExplosion(projectile.getLocation(), 4);
        }
    }
}
