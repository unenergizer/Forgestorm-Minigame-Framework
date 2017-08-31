package com.forgestorm.mgf.core.location;

import com.forgestorm.mgf.constants.ArenaState;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.location.access.ArenaPlayerAccess;
import com.forgestorm.mgf.core.location.access.ArenaSpectatorAccess;
import com.forgestorm.mgf.core.menus.SpectatorFlySpeed;
import com.forgestorm.mgf.core.menus.SpectatorPlayerTracker;
import com.forgestorm.mgf.core.winmanagement.WinManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.player.PlayerMinigameManager;
import com.forgestorm.spigotcore.constants.CommonSounds;
import com.forgestorm.spigotcore.util.display.BossBarAnnouncer;
import com.forgestorm.spigotcore.util.item.ItemBuilder;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/14/2017
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
public class GameArena extends GameLocation {

    private final ItemStack spectatorServerExit = new ItemBuilder(Material.WATCH).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Back To Lobby").build(true);
    private final ItemStack trackPlayers = new ItemBuilder(Material.SKULL_ITEM).setTitle(ChatColor.LIGHT_PURPLE + "Track Players").build(true);
    private final ItemStack flySpeed = new ItemBuilder(Material.MINECART).setTitle(ChatColor.YELLOW + "Move Speed").build(true);
    private final BossBarAnnouncer spectatorBar = new BossBarAnnouncer(MinigameMessages.BOSS_BAR_SPECTATOR_MESSAGE.toString());
    private final int maxCountdown = 11;
    private final boolean showDebug = false;
    private PlayerMinigameManager playerMinigameManager;
    private WinManager winManager;
    private int countdown = maxCountdown;
    private int lastTeamSpawned = 0;
    @Setter
    private ArenaState arenaState = ArenaState.ARENA_WAITING;
    private Location spectatorSpawn;

    @Override
    public void setupGameLocation() {
        this.playerMinigameManager = gameManager.getPlayerMinigameManager();
        this.winManager = new WinManager(plugin);

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Setup Spectator spawn location
        spectatorSpawn = gameManager.getSpectatorLocation();

        // Start BukkitRunnable thread
        startCountdown();

        // Add players and show countdown
        allPlayersJoin(new ArenaPlayerAccess());
        showTutorialInfo();
    }

    @Override
    public void destroyGameLocation() {
        // Stop Bukkit runnable thread
        stopCountdown();

        // Set to null
        spectatorSpawn = null;

        // Unregister stat listeners
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        CreatureSpawnEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        //noinspection deprecation
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerKickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @Override
    protected void showCountdown() {

        switch (arenaState) {
            case ARENA_TUTORIAL:
                showTutorialCountdown();
                break;
            case ARENA_SHOW_SCORES:
                showScores();
                break;
            case ARENA_EXIT:
                gameManager.getGameSelector().getMinigame().setGameOver(true);
                break;
        }
    }

    /**
     * This will display the current games rules.
     * Game rules are defined in the minigame class
     * that is currently loaded and being played.
     */
    public void showTutorialInfo() {
        ColorLogger.GREEN.printLog(showDebug, "GameArena - showTutorialInfo()");
        arenaState = ArenaState.ARENA_TUTORIAL;

        //Show the core rules.
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MinigameMessages.GAME_BAR_RULES.toString());
        Bukkit.broadcastMessage("");

        //Loop through and show the core rules.
        for (String gameRule : gameManager.getGameSelector().getMinigame().getGamePlayRulesList()) {
            Bukkit.broadcastMessage(CenterChatText.centerChatMessage(ChatColor.YELLOW + gameRule));
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(MinigameMessages.GAME_BAR_BOTTOM.toString());
    }

    /**
     * This is the countdown that is shown after the tutorial is displayed.
     */
    private void showTutorialCountdown() {
        ColorLogger.GREEN.printLog(showDebug, "GameArena - showTutorialCountdown()");
        String timeLeft = Integer.toString(countdown);

        // Test if the game should end.
        if (gameManager.shouldMinigameEnd(null)) {
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
            gameManager.getGameSelector().getMinigame().initListeners();
            gameManager.getGameSelector().getMinigame().setupGame();
            gameManager.getGameSelector().getMinigame().setupPlayers();
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
        ColorLogger.GREEN.printLog(showDebug, "GameArena - tutorialCountdownMessage()");
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
        ColorLogger.GREEN.printLog(showDebug, "GameArena - showScores()");
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
     * This will send the spectator to the main spectator spawn point.
     *
     * @param spectator The player spectator we want to teleport.
     */
    public void teleportSpectator(Player spectator) {
        CommonSounds.ACTION_FAILED.playSound(spectator);
        spectator.teleport(spectatorSpawn);
    }

    /**
     * If we need to kill the player, we can remove them from the arena
     * and run the setup code to make them a spectator.
     *
     * @param player The player we want to kill.
     */
    public void killPlayer(Player player) {
        PlayerMinigameData playerMinigameData = playerMinigameManager.getPlayerProfileData(player);
        if (playerMinigameData.isSpectator()) return;

        // Heal the player
        player.setHealth(20);
        player.setFoodLevel(20);

        // Update team info
        playerMinigameData.getSelectedTeam().getDeadPlayers().add(player);

        // Convert to spectator
        playerQuit(new ArenaPlayerAccess(), player);
        playerJoin(new ArenaSpectatorAccess(), player);
        teleportSpectator(player);
    }

    /**
     * Shows hidden players.
     */
    public void showHiddenPlayers() {
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
    public void hideSpectators() {
        PlayerMinigameManager playerMinigameManager = gameManager.getPlayerMinigameManager();

        for (Player spectators : Bukkit.getOnlinePlayers()) {

            if (spectators.hasMetadata("NPC")) return;

            //If this player is a spectator lets hide them from the other players.
            if (!playerMinigameManager.getPlayerProfileData(spectators).isSpectator()) continue;

            //Now loop through all players and hide them from spectators.
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (playerMinigameManager.getPlayerProfileData(players).isSpectator()) continue;
                players.hidePlayer(spectators);
            }
        }
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
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            boolean spectator = gameManager.getPlayerMinigameManager().getPlayerProfileData(player).isSpectator();
            boolean tutorial = arenaState == ArenaState.ARENA_TUTORIAL;
            if (spectator || tutorial) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    /**
     * Prevent spectators from interacting with the environment.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpectatorInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!playerMinigameManager.getPlayerProfileData(player).isSpectator()) return;

        // Test for spectator Item clicks
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
                event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (event.getItem() == null) return;
            Material material = event.getItem().getType();

            ColorLogger.YELLOW.printLog("Spectator Item Clicked");

            if (material == spectatorServerExit.getType()) {
                playerQuit(new ArenaSpectatorAccess(), player);
                plugin.getSpigotCore().getBungeecord().connectToBungeeServer(player, "hub-01");
                ColorLogger.GREEN.printLog("Spectator watch clicked!! " + player.getDisplayName() + " leaving game!!");
                ColorLogger.YELLOW.printLog("spectatorServerExit");
            }

            if (material == trackPlayers.getType()) {
                new SpectatorPlayerTracker(plugin).open(player);
                ColorLogger.YELLOW.printLog("trackPlayers");
            }

            if (material == flySpeed.getType()) {
                new SpectatorFlySpeed(plugin).open(player);
                ColorLogger.YELLOW.printLog("flySpeed");
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {

        // Stop the player from moving if the game is showing the rules.
        if (arenaState != ArenaState.ARENA_TUTORIAL) return;
        Player player = event.getPlayer();
        PlayerMinigameData playerMinigameData = playerMinigameManager.getPlayerProfileData(player);
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
        if (gameManager.shouldMinigameEnd(event.getPlayer())) gameManager.getGameSelector().getMinigame().endMinigame();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (gameManager.shouldMinigameEnd(event.getPlayer())) gameManager.getGameSelector().getMinigame().endMinigame();
    }
}
