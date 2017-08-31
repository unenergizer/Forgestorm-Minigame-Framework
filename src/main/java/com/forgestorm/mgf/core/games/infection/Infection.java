package com.forgestorm.mgf.core.games.infection;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.games.infection.kits.BasicInfectionKit;
import com.forgestorm.mgf.core.score.StatType;
import com.forgestorm.mgf.core.selectable.kit.Kit;
import com.forgestorm.mgf.core.selectable.team.Team;
import com.forgestorm.mgf.core.winmanagement.winevents.LastManStandingWinEvent;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import com.forgestorm.spigotcore.util.math.RandomChance;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
 * DATE: 8/15/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class Infection extends Minigame {

    private final MobDisguise mobDisguise = new MobDisguise(DisguiseType.ZOMBIE);

    private final Map<Player, Boolean> isHuman = new HashMap<>(); // Player / human = true || zombie == false
    private final List<Player> playerScoreList = new ArrayList<>(); // players first dead
    private final List<Material> doors = new ArrayList<>();

    public Infection(MinigameFramework plugin) {
        super(plugin);
    }

    @Override
    public void setupGame() {
        // TODO: make sure the zombie didn't quit during game rules countdown

        ColorLogger.RED.printLog("INFECTION SETUP CALLED!");

        // init human map
        getTeamList().get(0).getTeamPlayers().forEach(player -> isHuman.put(player, true));
        getTeamList().get(1).getTeamPlayers().forEach(player -> {
            isHuman.put(player, false); // Add zombie to main map
            playerScoreList.add(player); // Add zombie to score map
            DisguiseAPI.disguiseToAll(player, mobDisguise); // Give player the disguise
            ColorLogger.BLUE.printLog("Disguising " + player.getName());
        });

        // Change some default settings
        cancelBlockBreak = false;
        cancelPVP = false;

        ColorLogger.RED.printLog("INFECTION SETUP COMPLETE!");

        // Test to see how many humans and zombies we have.
        int zombies = 0;
        int humans = 0;
        for (Map.Entry<Player, Boolean> entry : isHuman.entrySet()) {
            if (entry.getValue()) {
                humans++;
            } else {
                zombies++;
            }
        }
        ColorLogger.GREEN.printLog("Humans: " + humans + " / Zombies: " + zombies);

        // Add all minecraft door types to the list
        doors.add(Material.ACACIA_DOOR);
        doors.add(Material.BIRCH_DOOR);
        doors.add(Material.DARK_OAK_DOOR);
        doors.add(Material.IRON_DOOR);
        doors.add(Material.JUNGLE_DOOR);
        doors.add(Material.SPRUCE_DOOR);
        doors.add(Material.TRAP_DOOR);
        doors.add(Material.WOOD_DOOR);
        doors.add(Material.WOODEN_DOOR);
        doors.add(Material.IRON_TRAPDOOR);
    }

    @Override
    public void disableGame() {
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerKickEvent.getHandlerList().unregister(this);

        // Remove all disguises
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (DisguiseAPI.isDisguised(player)) DisguiseAPI.undisguiseToAll(player);
        }
    }

    @Override
    public World getLobbyWorld() {
        return Bukkit.getWorld("mg-lobby");
    }

    @Override
    public List<String> getGamePlayTips() {
        ArrayList<String> tips = new ArrayList<>();
        tips.add("Humans, avoid becoming infected by escaping the zombies!");
        tips.add("Zombies, get the humans!!1!!111! Mrrrmmmhh Brrrraiiinzzz");
        tips.add("You can break torches and glass windows to escape being infected!");
        return tips;
    }

    @Override
    public List<String> getGamePlayRules() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("Humans: Escape the zombies to avoid being infected.");
        rules.add("Zombies: Kill humans for their braiinnssszzz.");
        rules.add("Last player alive wins!");
        return rules;
    }

    @Override
    public List<Kit> getKits() {
        List<Kit> kits = new ArrayList<>();
        kits.add(new BasicInfectionKit());
        return kits;
    }

    @Override
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(
                0,
                "Humans",
                ChatColor.BLUE,
                15,
                EntityType.VILLAGER,
                Material.STONE,
                new String[]{"You are a human. Don't get infected!"}));
        teams.add(new Team(
                1,
                "Zombies",
                ChatColor.GREEN,
                1,
                EntityType.ZOMBIE,
                Material.STONE,
                new String[]{"Kill all human's brains!"}));
        return teams;
    }

    @Override
    public List<StatType> getStatTypes() {
        List<StatType> scoreData = new ArrayList<>();
        scoreData.add(StatType.FIRST_KILL);
        return scoreData;
    }

    /**
     * Spawns a zombie player.
     *
     * @param player The player zombie to spawn.
     */
    private void spawnZombie(Player player) {
        // Find random respawn location
        List<Location> locationList = GameManager.getInstance().getTeamSpawnLocations().get(0).getLocations();
        int index = RandomChance.randomInt(1, locationList.size()) - 1;
        player.teleport(locationList.get(index));

        // Give the zombie a weapon
        player.getInventory().setItem(0, new ItemBuilder(Material.IRON_SWORD).setTitle("Human Slayer").build(true));

        // Heal
        player.setHealth(20);
        player.setFoodLevel(20);

        // Disguise the player
        if (DisguiseAPI.isDisguised(player)) return;
        DisguiseAPI.disguiseToAll(player, mobDisguise);

        // Do any team changing needed
        isHuman.replace(player, false);

        // Insert new zombie player
        playerScoreList.add(player);

        // Test game end
        testGameEnd();
    }

    private void testGameEnd() {
        // Test game end
        int testCount = 0;
        Player survivingHuman = null;
        for (Map.Entry<Player, Boolean> entry : isHuman.entrySet()) {
            if (entry.getValue()) {
                survivingHuman = entry.getKey();
                ++testCount;
            }
        }
        if (testCount <= 1) {
            // add last surviving human to list
            playerScoreList.add(survivingHuman);

            // end game
            Bukkit.getPluginManager().callEvent(new LastManStandingWinEvent(playerScoreList));
        }
    }

    private boolean isZombie(Player player) {
        return !isHuman.get(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Material brokenMaterial = event.getBlock().getType();

        // Generic broken materials
        switch (brokenMaterial) {
            case CHEST:
            case STAINED_GLASS_PANE:
            case THIN_GLASS:
            case GLASS:
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case DEAD_BUSH:
            case LONG_GRASS:
            case DOUBLE_PLANT:
            case SUGAR_CANE_BLOCK:
            case TRAPPED_CHEST:
                event.setCancelled(false);
                break;
            default:
                event.setCancelled(true);
                break;
        }

        // Zombie specific broken materials
        if (!isZombie(event.getPlayer())) return;

        ColorLogger.RED.printLog("Zombie broke: " + brokenMaterial.name());

        // Let zombies break doors
        if (doors.contains(brokenMaterial)) {
            event.setCancelled(false);

            ColorLogger.RED.printLog("Zombie broke a door");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isSpectator(event.getPlayer())) return;
        if (!isZombie(event.getPlayer())) return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        Material clickedMaterial = event.getClickedBlock().getType();

        ColorLogger.YELLOW.printLog("Zombie is interacting with: " + clickedMaterial);

        // Prevent zombies from opening doors
        if (doors.contains(clickedMaterial)) {
            event.setCancelled(true);
            ColorLogger.YELLOW.printLog("Zombie is interacting with a door.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {

        ColorLogger.BLUE.printLog("EntityDamageEvent EVENT");
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.getHealth() - event.getDamage() <= 1 && !(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))) {
                event.setCancelled(true);
                spawnZombie(player);
                //PLUGIN.getEndMiniGame().shouldGameEnd();
                player.sendMessage(ChatColor.RED + "You have died. Teleporting you to next spawn position.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        ColorLogger.BLUE.printLog("EntityDamageByEntityEvent EVENT");
        if (!(event.getDamager() instanceof Player) && !(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player defender = (Player) event.getEntity();

            double defenderHP = defender.getHealth() - event.getFinalDamage();

            //Human attacking zombie.
            if (!isZombie(damager) && isZombie(defender)) {

                damager.getWorld().playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1f, 1f);

                if (defenderHP <= 1) {
                    event.setCancelled(true);

                    //Respawn the defender.
                    spawnZombie(defender);
                    defender.sendMessage(ChatColor.RED + "You were killed! But zombes never die, so KILL THE HUMANS!");
                }

                //Zombie attacking human.
            } else if (isZombie(damager) && !isZombie(defender)) {

                //If the player is about to die.
                if (defenderHP <= 1) {
                    event.setCancelled(true);

                    //Respawn the defender.
                    spawnZombie(defender);
                    defender.sendMessage(ChatColor.RED + "You were infected! Kill the humans!");

                    //Play sound
                    damager.getWorld().playSound(defender.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1f, 1f);
                }
            } else {
                event.setCancelled(true);
            }

        } else if (event.getEntity() instanceof Player) {
            // TODO: IS THIS NEEDED?
            //If player harms themselves.
            Player defender = (Player) event.getEntity();

            if (defender.getHealth() - event.getDamage() <= 1) {
                event.setCancelled(true);

                //Respawn the defender.
                spawnZombie(defender);
                defender.sendMessage(ChatColor.RED + "You were infected! Kill the humans!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLeftGame(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        playerLeftGame(event.getPlayer());
    }

    private void playerLeftGame(Player player) {
        if (getPlayerMinigameData(player).isSpectator()) return;
        isHuman.remove(player);

        boolean zombieExist = false;
        for (boolean test : isHuman.values()) if (!test) zombieExist = true;

        if (zombieExist) return;
        int randomIndex = new Random().nextInt(isHuman.size());
        for (int i = 0; i < isHuman.size(); i++) {
            if (randomIndex == i) spawnZombie(player);
        }
    }
}
