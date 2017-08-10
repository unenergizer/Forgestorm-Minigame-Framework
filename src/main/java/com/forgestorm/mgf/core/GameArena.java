package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.ArenaState;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.games.GameType;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.menus.SpectatorFlySpeed;
import com.forgestorm.mgf.core.menus.SpectatorPlayerTracker;
import com.forgestorm.mgf.core.team.TeamSpawnLocations;
import com.forgestorm.mgf.core.winmanagement.WinManager;
import com.forgestorm.mgf.core.world.WorldData;
import com.forgestorm.mgf.player.PlayerManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.spigotcore.constants.CommonSounds;
import com.forgestorm.spigotcore.util.display.BossBarAnnouncer;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import com.forgestorm.spigotcore.util.math.RandomChance;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
@Getter
public class GameArena extends BukkitRunnable implements Listener {

    private final MinigameFramework plugin;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final Minigame minigame;
    private final WinManager winManager;
    private final Configuration configuration;
    private final ItemStack spectatorServerExit = new ItemBuilder(Material.WATCH).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Back To Lobby").build(true);
    private final ItemStack trackPlayers = new ItemBuilder(Material.SKULL_ITEM).setTitle(ChatColor.LIGHT_PURPLE + "Track Players").build(true);
    private final ItemStack flySpeed = new ItemBuilder(Material.MINECART).setTitle(ChatColor.YELLOW + "Move Speed").build(true);
    private final BossBarAnnouncer spectatorBar = new BossBarAnnouncer(MinigameMessages.BOSS_BAR_SPECTATOR_MESSAGE.toString());
    private final List<TeamSpawnLocations> teamSpawnLocations = new ArrayList<>();
    private final int maxCountdown = 11;
    private final boolean showDebug = true;
    private int countdown = maxCountdown;
    private int lastTeamSpawned = 0;
    @Setter
    private ArenaState arenaState = ArenaState.ARENA_WAITING;
    private Location spectatorSpawn;

    GameArena(MinigameFramework plugin, GameManager gameManager, Minigame minigame, GameType gameType) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.playerManager = gameManager.getPlayerManager();
        this.minigame = minigame;
        this.winManager = new WinManager(plugin, minigame);
        this.configuration = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder() + "/" + gameType.getFileName())
        );
    }

    /**
     * This will register stat listeners and setup the game arena.
     */
    void setupArena() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - setupArena()");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Clear entities from arena map
        gameManager.clearWorldEntities(gameManager.getCurrentArenaWorldData().getWorldName());
    }

    /**
     * Here we will unregister stat listeners and destroy the game
     * arena.
     */
    void destroyArena() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - destroyArena()");
        // Unregister stat listeners
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        //noinspection deprecation
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerKickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    /**
     * This will add a player to the minigame arena as a arena player.
     *
     * @param player The player to add to the arena.
     */
    private void addArenaPlayer(Player player) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - addArenaPlayer()");
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        TeamSpawnLocations teamLocations = teamSpawnLocations.get(playerMinigameData.getSelectedTeam().getIndex());
        int lastTeamSpawnIndex = teamLocations.getLastTeamSpawnIndex();

        // Backup player inventory
        if (!playerMinigameData.isInventoryBackedUp()) {
            ColorLogger.WARNING.printLog("Saved ArenaPlayer " + player.getDisplayName() + " Added! - addArenaPlayer()");
            playerMinigameData.backupInventoryContents();
            playerMinigameData.setInventoryBackedUp(true);
        }

        // Clear the inventory
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Teleport player
        Location location = teamLocations.getLocations().get(lastTeamSpawnIndex);
        player.teleport(location);
        playerMinigameData.setArenaSpawnLocation(location);

        // Increment teleport counter
        if (lastTeamSpawnIndex > (teamLocations.getLocations().size() - 1)) {
            teamLocations.setLastTeamSpawnIndex(0);
        } else {
            teamLocations.setLastTeamSpawnIndex(lastTeamSpawnIndex + 1);
        }
    }

    /**
     * This will remove a player from the arena.
     *
     * @param player The player to remove.
     */
    public void removeArenaPlayer(Player player, boolean restoreInventory) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - removeArenaPlayer()");
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        playerMinigameData.setSelectedTeam(null);
        playerMinigameData.setQueuedTeam(null);
        playerMinigameData.setSelectedKit(null);

        // Remove potion effects
        for (PotionEffect potionEffect : player.getActivePotionEffects()) player.removePotionEffect(potionEffect.getType());

        // Clear inventory and armor
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Restore player inventory
        if (playerMinigameData.isInventoryBackedUp() && restoreInventory) {
            ColorLogger.WARNING.printLog("Restored arenaPlayer " + player.getDisplayName() + " inventory! - removeArenaPlayer()");
            playerMinigameData.restoreInventoryContents();
            playerMinigameData.setInventoryBackedUp(false);
        } else {
            ColorLogger.WARNING.printLog("Removing Arena player. Not backing up inventory! (moving to spectator?)");
        }
    }

    /**
     * This will send the spectator to the main spectator spawn point.
     *
     * @param spectator The player spectator we want to teleport.
     */
    public void teleportSpectator(Player spectator) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - teleportSpectator()");
        CommonSounds.ACTION_FAILED.playSound(spectator);
        spectator.teleport(spectatorSpawn);
    }

    /**
     * This will add a spectator to the arena.
     *
     * @param spectator The spectator to add.
     */
    public void addSpectator(Player spectator, boolean backupInventory) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - addSpectator()");
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(spectator);

        // Set player as spectator in their profile.
        playerMinigameData.setSpectator(true);

        // Backup player inventory
        if (!playerMinigameData.isInventoryBackedUp() && backupInventory) {
            ColorLogger.WARNING.printLog("Saved " + spectator.getDisplayName() + " inventory! - addSpectator()");
            playerMinigameData.backupInventoryContents();
            playerMinigameData.setInventoryBackedUp(true);
        } else {
            ColorLogger.WARNING.printLog("Spectator " + spectator.getDisplayName() + " Added! - Inventory backup already exists!");
        }

        // Clear the inventory
        spectator.getInventory().clear();
        spectator.getInventory().setHelmet(null);
        spectator.getInventory().setChestplate(null);
        spectator.getInventory().setLeggings(null);
        spectator.getInventory().setBoots(null);

        // Show the spectator a boss bar.
        spectatorBar.showBossBar(spectator);

        // Show spectator join messages
        gameManager.getPlugin().getTitleManagerAPI().sendTitles(
                spectator,
                MinigameMessages.GAME_ARENA_SPECTATOR_TITLE.toString(),
                MinigameMessages.GAME_ARENA_SPECTATOR_SUBTITLE.toString());

        // Set minecraft defaults
        spectator.setGameMode(GameMode.ADVENTURE);
        spectator.setCollidable(false);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectator.setHealth(20);
        spectator.setFoodLevel(20);
        spectator.getInventory().clear();
        spectator.getInventory().setHelmet(null);
        spectator.getInventory().setChestplate(null);
        spectator.getInventory().setLeggings(null);
        spectator.getInventory().setBoots(null);

        // Give the spectator invisible potion effect
        spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));

        // Hide spectators
        hideSpectators();

        //Give Spectator tracker menu items.
        spectator.getInventory().setItem(0, spectatorServerExit);
        spectator.getInventory().setItem(1, trackPlayers);
        spectator.getInventory().setItem(2, flySpeed);
    }

    /**
     * This will remove a spectator from the arena.
     *
     * @param spectator The player to remove from spectator.
     */
    public void removeSpectator(Player spectator) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - removeSpectator()");

        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(spectator);
        playerMinigameData.setSelectedTeam(null);
        playerMinigameData.setQueuedTeam(null);
        playerMinigameData.setSelectedKit(null);

        // Set player as non-spectator in their profile.
        playerMinigameData.setSpectator(false);

        // Remove the spectator boss bar.
        spectatorBar.removeBossBar(spectator);

        // Set some minecraft defaults
        spectator.setGameMode(GameMode.SURVIVAL);
        spectator.setCollidable(true);
        spectator.setFlySpeed(.1f);
        spectator.setAllowFlight(false);
        spectator.setFlying(false);

        // Clear inventory and armor
        spectator.getInventory().clear();
        spectator.getInventory().setHelmet(null);
        spectator.getInventory().setChestplate(null);
        spectator.getInventory().setLeggings(null);
        spectator.getInventory().setBoots(null);

        // Remove spectator invisible potion effect
        for (PotionEffect potionEffect : spectator.getActivePotionEffects()) spectator.removePotionEffect(potionEffect.getType());

        // Restore player inventory
        if (playerMinigameData.isInventoryBackedUp()) {
            ColorLogger.WARNING.printLog("Restored spectator " + spectator.getDisplayName() + " inventory! - removeSpectator()");
            playerMinigameData.restoreInventoryContents();
            playerMinigameData.setInventoryBackedUp(false);
        }

        showHiddenPlayers();
    }

    /**
     * Responsible for getting a random world from the configuration.  This random world
     * will be the world that players will play in in the minigames.
     * <p>
     * So what happens here, we have to loop through all the world index's represented as
     * numbers, get the name of the world, then at the end we will select a random world
     * to play in.
     *
     * @return This will return the world data for one particular world.
     */
    WorldData getRandomArenaWorld() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - getRandomArenaWorld()");
        List<WorldData> worldDataList = new ArrayList<>();
        ConfigurationSection outerSection = configuration.getConfigurationSection("Worlds");

        // Get all worlds
        for (String worldNumber : outerSection.getKeys(false)) {
            String worldName = configuration.getString("Worlds." + worldNumber + ".Name");
            worldDataList.add(new WorldData(Integer.valueOf(worldNumber), worldName));
        }

        // Return random world data.
        if (worldDataList.size() - 1 > 1) return worldDataList.get(RandomChance.randomInt(0, worldDataList.size() - 1));

        // If only one world exists, then just return the first one.
        return worldDataList.get(0);
    }

    /**
     * This will get the team spawn locations for the selected world data.  These locations are
     * used to spawn teams in the world they will be playing in.
     *
     * @param worldData The worldData to use to get the YML path to the values needed.
     */
    void generateTeamSpawnLocations(WorldData worldData) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - generateTeamSpawnLocations()");
        String path = "Worlds." + worldData.getWorldIndex();

        // Get spectator spawn location
        String spectator = configuration.getString(path + ".Spectator");
        String[] spectatorParts = spectator.split("/");
        spectatorSpawn = new Location(
                Bukkit.getWorld(worldData.getWorldName()),
                Double.parseDouble(spectatorParts[0]),
                Double.parseDouble(spectatorParts[1]),
                Double.parseDouble(spectatorParts[2])
        );

        // Get team spawn locations.
        ConfigurationSection innerSection = configuration.getConfigurationSection(path + ".Teams");

        // Loop through all teams
        for (String teamNumber : innerSection.getKeys(false)) {
            List<Location> locations = new ArrayList<>();
            List<String> locationsAsStr = innerSection.getStringList(teamNumber);

            // For each location that a team has, we will add it to the locations list above.
            locationsAsStr.forEach((locationAsStr) -> {
                String[] parts = locationAsStr.split("/");
                locations.add(new Location(
                        Bukkit.getWorld(worldData.getWorldName()),
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]))
                );
            });

            // Add each team we find to the list.
            teamSpawnLocations.add(new TeamSpawnLocations(Integer.valueOf(teamNumber), locations));
        }
    }

    /**
     * Shows hidden players.
     */
    private void showHiddenPlayers() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - showHiddenPlayers()");
        for (Player hiddenPlayer : Bukkit.getOnlinePlayers()) {
            if (hiddenPlayer.hasMetadata("NPC")) return;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (players.hasMetadata("NPC")) return;
                players.showPlayer(hiddenPlayer);
            }
        }
    }

    /**
     * Hides spectators.
     */
    private void hideSpectators() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - hideSpectators()");
        PlayerManager playerManager = gameManager.getPlayerManager();

        for (Player spectators : Bukkit.getOnlinePlayers()) {

            if (spectators.hasMetadata("NPC")) return;

            //If this player is a spectator lets hide them from the other players.
            if (!playerManager.getPlayerProfileData(spectators).isSpectator()) continue;

            //Now loop through all players and hide them from spectators.
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerManager.getPlayerProfileData(players).isSpectator()) continue;
                players.hidePlayer(spectators);
            }
        }
    }

    /**
     * This will display the current games rules.
     * Game rules are defined in the minigame class
     * that is currently loaded and being played.
     */
    void showTutorialInfo() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - showTutorialInfo()");
        arenaState = ArenaState.ARENA_TUTORIAL;

        //Show the core rules.
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MinigameMessages.GAME_BAR_RULES.toString());
        Bukkit.broadcastMessage("");

        //Loop through and show the core rules.
        for (String gameRule : minigame.getGamePlayRules()) {
            Bukkit.broadcastMessage(CenterChatText.centerChatMessage(ChatColor.YELLOW + gameRule));
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MinigameMessages.GAME_BAR_BOTTOM.toString());
    }

    /**
     * This is the countdown that is shown after the tutorial is displayed.
     */
    private void showTutorialCountdown() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - showTutorialCountdown()");
        String timeLeft = Integer.toString(countdown);

        // Test if the game should end.
        if (shouldMinigameEnd(null)) {
            cancel();
            gameManager.endGame(true);

            // Send error message.
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(MinigameMessages.ALERT.toString() + MinigameMessages.GAME_COUNTDOWN_NOT_ENOUGH_PLAYERS.toString());
        }

        // Do the tutorial countdown.
        if (countdown > 5 && countdown <= 10) {
            tutorialCountdownMessage(ChatColor.YELLOW + timeLeft, "", Sound.BLOCK_NOTE_PLING);
        } else if (countdown <= 5 && countdown > 0) {
            tutorialCountdownMessage(ChatColor.RED + timeLeft, "", Sound.BLOCK_NOTE_PLING);
        } else if (countdown == 0) {
            tutorialCountdownMessage("", ChatColor.GREEN + "Go!", Sound.BLOCK_NOTE_HARP);

            //Change the core state.
            arenaState = ArenaState.ARENA_GAME_PLAYING;

            // Reset countdown time.
            countdown = maxCountdown;

            // Lets start the minigame!
            minigame.initListeners();
            minigame.setupGame();
            minigame.setupPlayers();
        }
        countdown--;
    }

    /**
     * This sends the countdown message to all the players.
     *
     * @param title    The top message to send.
     * @param subtitle The bottom message to send.
     * @param sound    The sound to play when text is displayed.
     */
    private void tutorialCountdownMessage(String title, String subtitle, Sound sound) {
        ColorLogger.INFO.printLog(showDebug, "GameArena - tutorialCountdownMessage()");
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.hasMetadata("NPC")) return;
            plugin.getTitleManagerAPI().sendTitles(players, title, subtitle);
            players.playSound(players.getLocation(), sound, 1f, 1f);
        }
    }

    /**
     * After the game play is finished we will show the game scores.
     */
    private void showScores() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - showScores()");
        arenaState = ArenaState.ARENA_SHOW_SCORES;

        if (countdown == 10) winManager.printScores();
        if (countdown == 2) Bukkit.broadcastMessage(MinigameMessages.GAME_END_RETURNING_TO_LOBBY.toString());

        // Scores Countdown
        if (countdown <= 0) {
            arenaState = ArenaState.ARENA_EXIT;
            countdown = maxCountdown;
        }

        countdown--;
    }

    /**
     * Here we will do the tutorial info countdown and
     * we will show the player scores.
     */
    @Override
    public void run() {

        switch (arenaState) {
            case ARENA_TUTORIAL:
                showTutorialCountdown();
                break;
            case ARENA_SHOW_SCORES:
                showScores();
                break;
            case ARENA_EXIT:
                minigame.setGameOver(true);
                break;
        }
    }

    /**
     * Helper method to add all arena players.
     */
    void addAllArenaPlayers() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - addAllArenaPlayers()");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            addArenaPlayer(player);
        }
    }

    /**
     * Helper method to remove all arena players.
     */
    void removePlayersFromArena() {
        ColorLogger.INFO.printLog(showDebug, "GameArena - removePlayersFromArena()");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;

            // Remove players correctly
            if (!playerManager.getPlayerProfileData(player).isSpectator()) {
                removeArenaPlayer(player, true);
            } else {
                removeSpectator(player);
            }
        }
    }

    /**
     * This is a check to see if the game should end.
     * If there aren't enough players, then we should
     * end the games.  Added to ability to make sure
     * that when we run this check we are filtering
     * out spectator players.
     */
    private boolean shouldMinigameEnd(Player exitPlayer) {
        int minigamePlayers = 0;
        boolean shouldEnd = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == exitPlayer) continue;
            PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
            if (playerMinigameData == null || playerMinigameData.isSpectator()) continue;
            minigamePlayers++;
        }

        if (minigamePlayers < gameManager.getMinPlayersToStartGame()) {
            shouldEnd = true;

            // Send error message.
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(MinigameMessages.ALERT.toString() + MinigameMessages.GAME_COUNTDOWN_NOT_ENOUGH_PLAYERS.toString());
        }

        return shouldEnd;
    }

    /**
     * If we need to kill the player, we can remove them from the arena
     * and run the setup code to make them a spectator.
     *
     * @param player The player we want to kill.
     */
    public void killPlayer(Player player) {
        PlayerMinigameData playerMinigameData = playerManager.getPlayerProfileData(player);
        if (playerMinigameData.isSpectator()) return;

        // Update team info
        playerMinigameData.getSelectedTeam().getDeadPlayers().add(player);

        // Convert to spectator
        removeArenaPlayer(player, false);
        addSpectator(player, false);
        teleportSpectator(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    /**
     * Here we listen for VOID damage. If a player jumps
     * into the void, set them up as a spectator.
     *
     * @param event This is a Bukkit EntityDamageEvent.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        if (arenaState == ArenaState.ARENA_SHOW_SCORES || arenaState == ArenaState.ARENA_EXIT) {
            // Prevent any addition entity damage when the game is finally finished.
            event.setCancelled(true);
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Cancel void damage.
        event.setCancelled(true);

        // Run on the next tick to prevent teleport bug.
        new BukkitRunnable() {
            public void run() {
                killPlayer(player);
                cancel();
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            boolean spectator = gameManager.getPlayerManager().getPlayerProfileData(player).isSpectator();
            boolean tutorial = arenaState == ArenaState.ARENA_TUTORIAL;
            if (spectator || tutorial) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPickupItem(@SuppressWarnings("deprecation") PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    /**
     * Prevent spectators from interacting with the environment.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onSpectatorInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!playerManager.getPlayerProfileData(player).isSpectator()) return;

        // Test for spectator Item clicks
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
                event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (event.getItem() == null) return;
            Material material = event.getItem().getType();

            ColorLogger.DEBUG.printLog("Spectator Item Clicked");

            if (material == spectatorServerExit.getType()) {
                removeSpectator(player);
                plugin.getSpigotCore().getBungeecord().connectToBungeeServer(player, "hub-01");
                ColorLogger.INFO.printLog("Spectator watch clicked!! " + player.getDisplayName() + " leaving game!!");
                ColorLogger.DEBUG.printLog("spectatorServerExit");
            }

            if (material == trackPlayers.getType()) {
                new SpectatorPlayerTracker(plugin).open(player);
                ColorLogger.DEBUG.printLog("trackPlayers");
            }

            if (material == flySpeed.getType()) {
                new SpectatorFlySpeed(plugin).open(player);
                ColorLogger.DEBUG.printLog("flySpeed");
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {

        // Stop the player from moving if the game is showing the rules.
        if (arenaState != ArenaState.ARENA_TUTORIAL) return;
        Player player = event.getPlayer();
        PlayerMinigameData playerMinigameData = playerManager.getPlayerProfileData(player);
        boolean isSpectator = playerMinigameData.isSpectator();

        double moveX = event.getFrom().getX();
        double moveZ = event.getFrom().getZ();
        double moveToX = event.getTo().getX();
        double moveToZ = event.getTo().getZ();
        float pitch = event.getTo().getPitch();
        float yaw = event.getTo().getYaw();

        // If the countdown has started, then let the player look around and jump, but not walk/run.
        if ((moveX == moveToX && moveZ == moveToZ) || isSpectator) return;

        Location location = playerMinigameData.getArenaSpawnLocation();
        location.setPitch(pitch);
        location.setYaw(yaw);

        // Teleport player back to their arena spawn location.
        player.teleport(location);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKick(PlayerKickEvent event) {
        if (shouldMinigameEnd(event.getPlayer())) minigame.endMinigame();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (shouldMinigameEnd(event.getPlayer())) minigame.endMinigame();
    }
}
