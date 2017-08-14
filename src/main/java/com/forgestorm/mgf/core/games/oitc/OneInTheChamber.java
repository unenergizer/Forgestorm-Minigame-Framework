package com.forgestorm.mgf.core.games.oitc;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.oitc.kits.BasicKit;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.scoreboard.ArenaPointsCounter;
import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.core.winmanagement.winevents.IndividualTopScoreWinEvent;
import com.forgestorm.spigotcore.util.math.RandomChance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class OneInTheChamber extends Minigame {

    private final int maxScore = 10;
    private final Map<Player, Integer> playerScore = new HashMap<>();
    private ArenaPointsCounter arenaPointsCounter;
    private boolean gameOver = false;

    public OneInTheChamber(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        cancelPVP = false;
        arenaPointsCounter = new ArenaPointsCounter();
        arenaPointsCounter.addAllPlayers();
    }

    @Override
    public void disableGame() {
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Use your bow to shoot enemy players.");
        tips.add("You get one arrow for each enemy player you kill.");
        tips.add("The first person to get " + maxScore + " kills wins!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Use your bow to shoot enemy players.");
        rules.add("You get one arrow for each enemy player you kill.");
        rules.add("The first person to get " + maxScore + " kills wins!");
        return rules;
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();

        kits.add(new BasicKit());

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(
                0,
                "Player vs Player",
                ChatColor.BLUE,
                8,
                EntityType.DONKEY,
                Material.STONE,
                new String[]{"Kill other players to get the best score possible!"}));
        return teams;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> scoreData = new ArrayList<>();
        scoreData.add(StatType.FIRST_KILL);
        return scoreData;
    }

    /**
     * Gives the player some ammo. Usually called when a
     * player gets a kill.
     *
     * @param player The player who will get ammo.
     */
    private void giveAmmo(Player player) {
        Inventory playerInv = player.getInventory();
        int arrows = 0;

        //Get the players current arrow count.
        if (player.getInventory().getItem(2) != null) {
            arrows = playerInv.getItem(2).getAmount();
        }

        ItemStack arrow = new ItemStack(Material.ARROW, 1 + arrows);
        player.getInventory().setItem(2, arrow);

    }


    /**
     * Adds players scores up as the game progresses.
     *
     * @param player The player we will add a score for.
     * @param amount The amount of wool the player picked up.
     */
    private void addScore(Player player, int amount) {
        if (gameOver) return;
        if (playerScore.containsKey(player)) {
            int current = playerScore.get(player);
            int totalScore = amount + current;
            playerScore.replace(player, amount + current);
            player.sendMessage(ChatColor.GREEN + "You got a point!");

            // Update the scoreboard.
            arenaPointsCounter.setBoardData(playerScore);

            if (totalScore >= maxScore) {
                gameOver = true;
                Bukkit.getPluginManager().callEvent(new IndividualTopScoreWinEvent(playerScore, "players killed"));
            }
        } else {
            playerScore.put(player, amount);
        }
    }

    /**
     * Respawns a killed player to a random location.
     *
     * @param player The player to respawn.
     */
    private void respawnPlayer(Player player) {
        List<Location> locationList = GameManager.getInstance().getTeamSpawnLocations().get(0).getLocations();

        // Get random location
        int index = RandomChance.randomInt(1, locationList.size());

        // Send player to this random location!
        player.teleport(locationList.get(index));
        player.sendMessage(ChatColor.RED + "You were killed! Sending you to next spawn position.");
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (player.getHealth() - event.getDamage() <= 1 && !(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))) {
            event.setCancelled(true);
            respawnPlayer(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player defender = (Player) event.getEntity();
        double hitPoints = defender.getHealth();
        double damage = event.getDamage();

        if (event.getDamager() instanceof Arrow) {
            /////////////////////////////////////////////////////////////
            /// Player killed by arrow
            /////////////////////////////////////////////////////////////

            Arrow arrow = (Arrow) event.getDamager();

            if (!(arrow.getShooter() instanceof Player)) return;

            Player damager = (Player) arrow.getShooter();

            //Make sure the player did not shoot themselves.
            if (damager.equals(defender)) return;
            event.setCancelled(true);

            //Give damager a point.
            addScore(damager, 1);
            giveAmmo(damager);

            //Respawn the defender.
            respawnPlayer(defender);

        } else if (event.getDamager() instanceof Player) {
            /////////////////////////////////////////////////////////////
            /// If player kills player with weapon (besides bow & arrow)
            /////////////////////////////////////////////////////////////

            Player damager = (Player) event.getDamager();

            if (hitPoints - damage > 1) return;
            event.setCancelled(true);

            //Give damager a point.
            addScore(damager, 1);
            giveAmmo(damager);

            //Respawn the defender.
            respawnPlayer(defender);

        } else {
            /////////////////////////////////////////////////////////////
            /// Player hurt themselves
            /////////////////////////////////////////////////////////////

            if (hitPoints - damage > 1) return;
            event.setCancelled(true);

            //Respawn the defender.
            respawnPlayer(defender);
        }
    }
}
