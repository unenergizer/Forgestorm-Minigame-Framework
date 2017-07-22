package com.forgestorm.mgf.core;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.kit.KitManager;
import com.forgestorm.mgf.core.scoreboard.TarkanLobbyScoreboard;
import com.forgestorm.mgf.core.team.TeamManager;
import com.forgestorm.mgf.core.world.TeleportFix2;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.util.display.TipAnnouncer;
import com.forgestorm.mgf.util.world.PlatformBuilder;
import com.forgestorm.spigotcore.events.UpdateScoreboardEvent;
import com.forgestorm.spigotcore.player.DoubleJump;
import com.forgestorm.spigotcore.util.display.BossBarAnnouncer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
public class GameLobby extends BukkitRunnable implements Listener {

    private final MinigameFramework plugin;
    private final GameManager gameManager;
    private final Minigame minigame;
    private final TarkanLobbyScoreboard tarkanLobbyScoreboard;
    private final PlatformBuilder platformBuilder = new PlatformBuilder();
    private final BossBarAnnouncer bar = new BossBarAnnouncer(MinigameMessages.BOSS_BAR_LOBBY_MESSAGE.toString());
    private final Location spawn = new Location(Bukkit.getWorld("mg-lobby"), 0.5, 101, 0.5);
    private DoubleJump doubleJump;
    private TipAnnouncer tipAnnouncer;
    private KitManager kitManager;
    private TeamManager teamManager;
    private boolean cancelTask = false;
    private boolean countdownStarted = false;
    private final int maxCountdown = 30;
    private int countdown = maxCountdown;
    private TeleportFix2 teleportFix2 = new TeleportFix2();

    GameLobby(MinigameFramework plugin, GameManager gameManager, Minigame minigame) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.minigame = minigame;
        tarkanLobbyScoreboard = new TarkanLobbyScoreboard(plugin, gameManager, this);
        doubleJump = new DoubleJump(plugin.getSpigotCore());
    }

    /**
     * This will setup a lobby for the supplied minigame.
     */
    void setupLobby() {

        // Kit Setup
        kitManager = new KitManager(plugin, gameManager, platformBuilder, minigame.getKits(), minigame.getLobbyWorld());
        kitManager.setupKits();

        // Team Setup
        teamManager = new TeamManager(plugin, gameManager, platformBuilder, minigame.getTeams(), minigame.getLobbyWorld());
        teamManager.setupTeams();

        // Display core tips
        tipAnnouncer = new TipAnnouncer(plugin, minigame.getGamePlayTips());

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Set weather
        World world = Bukkit.getWorlds().get(0);
        world.setStorm(false);
        world.setWeatherDuration(0);
    }

    /**
     * This will remove the lobby from the core world.
     */
    void destroyLobby() {
        cancelTask = true;

        // Destroy Kit Manager
        kitManager.destroyKits();

        // Destroy Team Manager
        teamManager.destroyTeams();

        // Stop core play tips.
        tipAnnouncer.setShowTips(false);

        // Unregister stat listeners
        EntityCombustEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        FoodLevelChangeEvent.getHandlerList().unregister(this);
        ItemSpawnEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
        WeatherChangeEvent.getHandlerList().unregister(this);
    }

    /**
     * This will setup a player for the lobby display.
     *
     * @param player The player we are going to setup.
     */
    public void setupPlayer(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);

        // Set default kit
        playerMinigameData.setSelectedKit(minigame.getKits().get(0));

        // Set default team
        teamManager.initPlayer(player);

        // Send the player the boss bar.
        bar.showBossBar(player);

        // Lets change some player Bukkit/Spigot defaults
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFireTicks(0);

        // Teleport the player to the main spawn.
        player.teleport(spawn);

        // Setup player for double jump.
        doubleJump.setupPlayer(player);

        // Do teleport fix!
        teleportFix2.fixTeleport(player);
    }

    /**
     * This will remove a player from the lobby setup. The removal
     * process can happen if they quit the lobby or right before
     * they are put into a minigame arena.
     *
     * @param player The player we will remove.
     */
    public void removePlayer(Player player) {
        // Remove the scoreboard.
        tarkanLobbyScoreboard.removePlayer(player);

        // Remove the boss bar.
        bar.removeBossBar(player);

        // Remove player double jump.
        doubleJump.removePlayer(player);
    }

    /**
     * This will reset all of the players scoreboards.
     * This is done after players have been assigned a
     * kit and a team.
     */
    void resetPlayerScoreboards() {
        // Reset all player scoreboards.  This must be done after
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;

            // Setup the player scoreboard.
            Bukkit.getPluginManager().callEvent(new UpdateScoreboardEvent(player));
        }
    }

    /**
     * Here we will do the lobby countdown.
     */
    @Override
    public void run() {
        if (cancelTask) cancel();

        // Update TarkanScoreBoard animation
        tarkanLobbyScoreboard.animateScoreboard();

        // Do lobby countdown
        performLobbyCountdown();
    }

    /**
     * This is the lobby countdown.  The countdown will only run if certain conditions
     * are met. If the countdown is successful, we will start the core.
     */
    private void performLobbyCountdown() {
        // Do lobby countdown
        if (!gameManager.isInLobby()) return;
        if (!gameManager.isCurrentArenaWorldLoaded()) return;
        if (Bukkit.getOnlinePlayers().size() >= gameManager.getMinPlayersToStartGame()) {
            countdownStarted = true;

            // Show countdown message to the players.
            if (countdown == 30 || countdown == 20 || countdown == 10 || countdown <= 5 && countdown > 1) {
                String message = MinigameMessages.GAME_TIME_REMAINING_PLURAL.toString();

                for (Player players: Bukkit.getOnlinePlayers()) {
                    if (players.hasMetadata("NPC")) return;
                    plugin.getTitleManagerAPI().sendActionbar(players, message.replace("%s", Integer.toString(countdown)));
                    players.playSound(players.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                }
            } else if (countdown <= 1) {

                String message = MinigameMessages.GAME_TIME_REMAINING_SINGULAR.toString();

                for (Player players: Bukkit.getOnlinePlayers()) {
                    if (players.hasMetadata("NPC")) return;
                    plugin.getTitleManagerAPI().sendActionbar(players, message);
                    players.playSound(players.getLocation(), Sound.BLOCK_NOTE_HARP, 1f, 1f);
                }

                //Do one last check to make sure the core should start.
                gameManager.switchToArena();
                countdown = maxCountdown;
            }

            countdown--;
        } else {

            // If the countdown message does not equal the maxCountdown time,
            // then it is safe to assume that a countdown started, but then a
            // player left the server, thus stopping the countdown. Since this
            // happened, lets reset the countdown!
            if (countdown != maxCountdown) {
                // Reset defaults
                countdownStarted = false;
                countdown = maxCountdown;

                // Send countdown fail message.
                String message = MinigameMessages.GAME_COUNTDOWN_NOT_ENOUGH_PLAYERS.toString();
                for (Player players: Bukkit.getOnlinePlayers()) {
                    if (players.hasMetadata("NPC")) return;

                    //Show message
                    plugin.getTitleManagerAPI().sendActionbar(players, message);

                    //Play notification sound.
                    players.playSound(players.getLocation(), Sound.BLOCK_NOTE_BASS, 1F, .5F);
                }
            }
        }
    }

    /**
     * Helper method to setup all players currently in the lobby.
     */
    void setupAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            setupPlayer(player);
        }
    }

    /**
     * Helper method to remove all players currently in the lobby.
     */
    void removeAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC")) return;
            removePlayer(player);
        }
    }

    /**
     * Called when a player jumps into the void or into a end portal.
     * This will send them back to the main spawn position.
     *
     * @param player The player to teleport.
     */
    private void sendToSpawn(Player player) {
        //Teleport the player.
        player.teleport(spawn);
        player.setFallDistance(0F);

        //Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1F, .5F);

        //Give player potion effect.
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 100));
    }

    /**
     * We listen to the EntityCombustEvent to prevent entities
     * from catching fire in the lobby. This usually happens
     * when we use a Zombie or a Skeleton as a kit or team
     * entity.
     * @param event This is a Bukkit EntityCombustEvent.
     */
    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) { event.setCancelled(true); }

    /**
     * We listen to the EntityDamageEvent to prevent players
     * from taking damage while in the core lobby.
     *
     * Additionally we listen for VOID damage. If a player
     * jumps into the void, we will teleport them back to
     * the main spawn position.
     *
     * @param event This is a Bukkit EntityDamageEvent.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true); // Prevent all damage in the lobby.
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(event.getEntity() instanceof Player)) return;

        //Run on the next tick to prevent teleport bug.
        new BukkitRunnable() {
            public void run() {
                sendToSpawn((Player) event.getEntity());

                cancel();
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * We listen to the FoodLevelChangeEvent to prevent players
     * food levels from to prevent them from starving and/or
     * loosing hunger.
     * @param event This is a Bukkit FoodLevelChangeEvent.
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) { event.setCancelled(true); }

    /**
     * We listen to the ItemSpawnEvent to watch for and prevent
     * the spawning of eggs. A egg will spawn from chickens who
     * are used as kit or as team entities.
     * @param event This is a Bukkit ItemSpawnEvent.
     */
    @EventHandler
    public void onEggSpawn(ItemSpawnEvent event) {
        //Prevent chickens from laying eggs.
        if (event.getEntity().getItemStack().getType() == (Material.EGG)) {
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    /**
     * We listen to the PlayerDropItemEvent to make sure players
     * are not dropping the WATCH device which will take them
     * back to the main hub.
     * @param event This is a Bukkit PlayerDropItemEvent.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.WATCH) event.setCancelled(true);
    }

    /**
     * We listen to the WeatherChangeEvent to prevent the world
     * from having weather changes.  The rain can become really
     * annoying to players, so we will disable that functionality
     * here.
     * @param event This is a Bukkit WeatherChangeEvent.
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) { if (event.toWeatherState()) event.setCancelled(true); }



    @EventHandler
    public void onPlayerTeleport(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        //If player enters a Ender portal, teleport them back to spawn pad.
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {

            //Cancel teleportation to the END_GAME
            event.setCancelled(true);

            new BukkitRunnable() {
                public void run() {

                    //Teleport the player.
                    sendToSpawn(player);

                    cancel();
                }
            }.runTaskLater(plugin, 1L);
        }

        //If player enters a Ender portal, teleport them back to spawn pad.
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {

            // Cancel teleportation to the NETHER
            event.setCancelled(true);

            // Send player to the lobby
            player.chat("/lobby");
        }
    }
}
