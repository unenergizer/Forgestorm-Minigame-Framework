package com.forgestorm.mgf.core.games.sheersheep;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.kit.Kit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.team.Team;
import com.forgestorm.mgf.player.PlayerManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
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

@SuppressWarnings("unused")
public class SheerSheep extends Minigame implements Listener {

    private final SpawnSheep spawnSheep;
    private final int maxScore = 80;

    public SheerSheep(MinigameFramework plugin) {
        super(plugin);
        spawnSheep = new SpawnSheep(plugin);
    }

    @Override
    public void setupGame() {
        spawnSheep.spawnSheep();
    }

    @Override
    public void disableGame() {
        // Cancel threads
        spawnSheep.cancelSheepSpawn();

        // Unregister listeners
        PlayerShearEntityEvent.getHandlerList().unregister(this);
    }

    @Override
    public void setupPlayers() {
        PlayerManager playerManager = plugin.getGameManager().getPlayerManager();
        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemStack sword = new ItemStack(Material.GOLD_SWORD);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMinigameData playerMinigameData = playerManager.getPlayerProfileData(player);
            Kit kit = playerMinigameData.getSelectedKit();

            if (kit == defaultKit) {
                player.getInventory().addItem(shears.clone());
            } else if (kit == kit2) {
                player.getInventory().addItem(sword.clone());
            }
        }
    }

    private Kit defaultKit, kit2;

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();
        List<String> defaultKitDesc = new ArrayList<>();
        List<String> kit2Desc = new ArrayList<>();

        defaultKitDesc.add("Blow up the sheep!");
        defaultKitDesc.add("Get as much wool as you can!");

        kit2Desc.add("Knife them till the wool falls off!");


        defaultKit = new Kit("Explosive Shears",
                ChatColor.GREEN,
                EntityType.CHICKEN,
                Material.STONE,
                defaultKitDesc);

        kit2 = new Kit("Knife Party",
                ChatColor.GREEN,
                EntityType.PIG,
                Material.STONE,
                kit2Desc);

        kits.add(defaultKit);
        kits.add(kit2);

        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<String> description = new ArrayList<>();
        description.add("Every player from themselves!!");
        description.add("Compete to be the best!");


        List<Team> team = new ArrayList<>();
        team.add(new Team(
                0,
                "Individual Team",
                ChatColor.GREEN,
                -1,
                EntityType.SHEEP,
                Material.BOOKSHELF,
                description));
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
}
