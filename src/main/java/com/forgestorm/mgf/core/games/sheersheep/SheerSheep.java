package com.forgestorm.mgf.core.games.sheersheep;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.sheersheep.kits.ExplosiveShears;
import com.forgestorm.mgf.core.games.sheersheep.kits.KnifeParty;
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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class SheerSheep extends Minigame {

    private final int maxScore = 80;
    private final Map<Player, Integer> playerScore = new HashMap<>();
    private SpawnSheep spawnSheep;
    private ArenaPointsCounter arenaPointsCounter;
    private boolean gameOver = false;

    public SheerSheep(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        arenaPointsCounter = new ArenaPointsCounter();
        arenaPointsCounter.addAllPlayers();

        spawnSheep = new SpawnSheep(plugin);
        spawnSheep.run();
    }

    @Override
    public void disableGame() {
        // Cancel threads
        spawnSheep.cancelRunnable();

        // This can be null if the game ends during the tutorial stage.
        if (arenaPointsCounter != null) {
            arenaPointsCounter.removeAllPlayers();
        }

        // Clear score map
        playerScore.clear();

        // Unregister listeners
        PlayerShearEntityEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        CreatureSpawnEvent.getHandlerList().unregister(this);
    }

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();

        kits.add(new ExplosiveShears());
        kits.add(new KnifeParty());

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Individual Team",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.BOOKSHELF,
                new String[]{"Every player from themselves!!", "Compete to be the best!"}));
        return team;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> statTypes = new ArrayList<>();
        statTypes.add(StatType.PICKUP_ITEM);
        return statTypes;
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Run around as fast as you can to shear the sheeps.");
        tips.add("Right click with your sheers to shear a sheep.");
        tips.add("The first person to get " + maxScore + " wool wins!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Run around as fast as you can to shear the sheeps.");
        rules.add("Right click with your sheers to shear a sheep.");
        rules.add("The first person to get " + maxScore + " wool wins!");
        return rules;
    }

    @EventHandler
    public void onEntitySheer(PlayerShearEntityEvent event) {
        Sheep sheep = (Sheep) event.getEntity();
        Location sheepEyeLocation = sheep.getEyeLocation();
        World world = sheep.getWorld();
        Random rn = new Random();
        int randCount = RandomChance.randomInt(1, 5);

        world.spawnParticle(Particle.EXPLOSION_LARGE, sheepEyeLocation, 1);
        world.playSound(sheepEyeLocation, Sound.ENTITY_SHEEP_DEATH, .7f, .7f);
        world.playSound(sheepEyeLocation, Sound.ENTITY_GENERIC_EXPLODE, .6f, .5f);

        for (int i = 0; i <= randCount; i++) {
            Item item = sheep.getWorld().dropItem(sheepEyeLocation, new ItemStack(Material.BONE));
            item.setVelocity(new Vector(rn.nextDouble() - 0.5, rn.nextDouble() / 2.0 + 0.3, rn.nextDouble() - 0.5).multiply(0.4));
        }

        sheep.setHealth(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickUp(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() != Material.WOOL) return;
        event.setCancelled(false);
        Player player = event.getPlayer();
        int amount = event.getItem().getItemStack().getAmount();
        addScore(player, amount);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Sheep)) return;
        event.setCancelled(false);
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

            // Update the scoreboard.
            arenaPointsCounter.setBoardData(playerScore);

            if (totalScore >= maxScore) {
                gameOver = true;
                Bukkit.getPluginManager().callEvent(new IndividualTopScoreWinEvent(playerScore, "wool picked up"));
            }
        } else {
            playerScore.put(player, amount);
        }
    }
}
