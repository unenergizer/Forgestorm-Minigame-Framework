package com.forgestorm.mgf.core.games.pirateattack;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.pirateattack.kits.Pirate;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.core.winmanagement.winevents.LastTeamStandingWinEvent;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
    private final List<Team> teams = new ArrayList<>();
    private final List<Team> teamsThatDied = new ArrayList<>();

    public PirateAttack(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        cancelPVE = false;
        pirateAmmoSpawn = new PirateAmmoSpawn();
        pirateAmmoSpawn.runTaskTimer(plugin, 0, 20 * 3);
    }

    @Override
    public void disableGame() {
        pirateAmmoSpawn.cancel();
        ProjectileHitEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityRegainHealthEvent.getHandlerList().unregister(this);
    }

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Lob cannon balls at other players.");
        tips.add("Run around to avoid cannon balls.");
        tips.add("If you fall in water you will start to drown!");
        tips.add("Last team alive wins!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Lob cannon balls at other players.");
        rules.add("Run around to avoid cannon balls.");
        rules.add("If you fall in water you will start to drown!");
        rules.add("Last team alive wins!");
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
    public List<StatType> getStatTypes() {
        List<StatType> scoreData = new ArrayList<>();
        scoreData.add(StatType.FIRST_KILL);
        return scoreData;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();

        PlayerMinigameData playerMinigameData = GameManager.getInstance().getPlayerMinigameManager().getPlayerProfileData(player);

        if (playerMinigameData.isSpectator()) return;

        // Prevent lobby world explosions....
        if (Bukkit.getWorlds().get(0).getName().equals(projectile.getLocation().getWorld().getName())) return;

        if (playerMinigameData.getSelectedTeam().getIndex() == 0) {
            if (projectile.getLocation().getX() > 0) return;
            projectile.getWorld().createExplosion(projectile.getLocation(), 4);
        }

        if (playerMinigameData.getSelectedTeam().getIndex() == 1) {
            if (projectile.getLocation().getX() < 0) return;
            projectile.getWorld().createExplosion(projectile.getLocation(), 4);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getDamage() <= 1) {
            event.setCancelled(true);
            removePlayerFromGame(player);

            ColorLogger.INFO.printLog("EntityDamageEvent - removePlayerFromGame()");
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getDamage() <= 1) {
            event.setCancelled(true);
            removePlayerFromGame(player);

            ColorLogger.INFO.printLog("onEntityDamage - removePlayerFromGame()");
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (getPlayerMinigameData(player).isSpectator()) return;
        if (!event.getTo().getBlock().isLiquid()) return;

        double maxDamage = 2;

        if (player.getHealth() - maxDamage <= 1) {
            removePlayerFromGame(player);

            ColorLogger.INFO.printLog("PlayerMoveEvent - removePlayerFromGame()");
        } else {
            player.damage(maxDamage);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getDamage() <= 1) {
            event.setCancelled(true);
            removePlayerFromGame(player);

            ColorLogger.INFO.printLog("EntityDamageByBlockEvent - removePlayerFromGame()");
        }
    }

    private void removePlayerFromGame(Player player) {
        ColorLogger.FATAL.printLog("removePlayerFromGame");

        // kill off the player
        Team team = getPlayerMinigameData(player).getSelectedTeam();
        killPlayer(player);

        // Prevent duplicate remove
        if (team == null || !team.getTeamPlayers().contains(player)) return;

        // Get team results
        if (team.getDeadPlayers().size() == team.getTeamPlayers().size()) {
            teamsThatDied.add(team);
            teamsThatDied.add(team.getIndex() == 0 ? teams.get(1) : teams.get(0));
            Bukkit.getPluginManager().callEvent(new LastTeamStandingWinEvent(teamsThatDied));
        }
    }
}
