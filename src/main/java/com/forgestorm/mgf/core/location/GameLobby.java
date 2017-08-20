package com.forgestorm.mgf.core.location;

import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.core.scoreboard.TarkanLobbyScoreboard;
import com.forgestorm.mgf.core.selectable.kit.KitSelectable;
import com.forgestorm.mgf.core.selectable.team.TeamSelectable;
import com.forgestorm.mgf.util.display.TipAnnouncer;
import com.forgestorm.mgf.util.world.PlatformBuilder;
import com.forgestorm.spigotcore.player.DoubleJump;
import com.forgestorm.spigotcore.professions.ProfessionToggle;
import com.forgestorm.spigotcore.util.display.BossBarAnnouncer;
import com.forgestorm.spigotcore.util.logger.ColorLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
public class GameLobby extends GameLocation {

    private final PlatformBuilder platformBuilder = new PlatformBuilder();
    private final BossBarAnnouncer bar = new BossBarAnnouncer(MinigameMessages.BOSS_BAR_LOBBY_MESSAGE.toString());
    private final Location spawn = new Location(Bukkit.getWorld("mg-lobby"), 0.5, 101, 0.5);
    private final int maxCountdown = 30;
    private final boolean showDebug = true;
    private TarkanLobbyScoreboard tarkanLobbyScoreboard;
    private DoubleJump doubleJump;
    private TipAnnouncer tipAnnouncer;
    private KitSelectable kitSelectable;
    private TeamSelectable teamSelectable;
    private boolean countdownStarted = false;
    @Setter
    private int countdown = maxCountdown;
    private ProfessionToggle professionToggle;

    @Override
    public void setupGameLocation() {
        // Initialize needed classes
        tarkanLobbyScoreboard = new TarkanLobbyScoreboard();
        doubleJump = new DoubleJump(plugin.getSpigotCore());

        // Kit Setup
        kitSelectable = new KitSelectable(minigame);
        kitSelectable.onEnable();

        // Team Setup
        teamSelectable = new TeamSelectable(minigame);
        teamSelectable.onEnable();

        // Display core tips
        tipAnnouncer = new TipAnnouncer(plugin, minigame.getGamePlayTipsList());

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Enable Professions
        professionToggle = new ProfessionToggle(plugin.getSpigotCore());
        professionToggle.enableProfessions();

        // Set weather
        World world = Bukkit.getWorlds().get(0);
        world.setStorm(false);
        world.setWeatherDuration(0);

        // Start BukkitRunnable thread
        startCountdown();
    }

    @Override
    public void destroyGameLocation() {
        // Destroy previously needed classes
        stopCountdown();

        // Destroy Kit Manager
        kitSelectable.onDisable();

        // Destroy Team Manager
        teamSelectable.onDisable();

        // Stop core play tips.
        tipAnnouncer.setShowTips(false);

        // Test Profession Implementation
        professionToggle.disableProfessions();
        ColorLogger.INFO.printLog(Boolean.toString(professionToggle.isProfessionsEnabled()));

        // Unregister stat listeners
        EntityCombustEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        FoodLevelChangeEvent.getHandlerList().unregister(this);
        ItemSpawnEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
        WeatherChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    protected void showCountdown() {
        // Update TarkanScoreBoard animation
        tarkanLobbyScoreboard.animateScoreboard();

        // Do lobby countdown
        if (!gameManager.isInLobby()) return;
        if (!gameManager.isCurrentArenaWorldLoaded()) return;

        // Do lobby countdown.
        if (gameManager.shouldMinigameStart()) {
            if (!countdownStarted)
                ColorLogger.INFO.printLog(showDebug, "GameLobby - performLobbyCountdown() -> Countdown started!");

            countdownStarted = true;

            // Show countdown message to the players.
            if (countdown == 30 || countdown == 20 || countdown == 10 || countdown <= 5 && countdown > 1) {
                String message = MinigameMessages.GAME_TIME_REMAINING_PLURAL.toString();

                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (players.hasMetadata("NPC")) return;
                    plugin.getTitleManagerAPI().sendActionbar(players, message.replace("%s", Integer.toString(countdown)));
                    players.playSound(players.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                }
            } else if (countdown <= 1) {

                String message = MinigameMessages.GAME_TIME_REMAINING_SINGULAR.toString();

                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (players.hasMetadata("NPC")) return;
                    plugin.getTitleManagerAPI().sendActionbar(players, message);
                    players.playSound(players.getLocation(), Sound.BLOCK_NOTE_HARP, 1f, 1f);
                }

                //Do one last check to make sure the core should start.
                gameManager.switchToArena();
                countdown = maxCountdown;
            }

            countdown--;
        } else if (countdownStarted) {
            ColorLogger.INFO.printLog(showDebug, "GameLobby - performLobbyCountdown() -> Countdown canceled!");

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
                for (Player players : Bukkit.getOnlinePlayers()) {
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
     * Called when a player jumps into the void or into a end portal.
     * This will send them back to the main spawn position.
     *
     * @param player The player to teleport.
     */
    private void sendToSpawn(Player player) {
        ColorLogger.INFO.printLog(showDebug, "GameLobby - sendToSpawn()");
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
     *
     * @param event This is a Bukkit EntityCombustEvent.
     */
    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    /**
     * We listen to the EntityDamageEvent to prevent players
     * from taking damage while in the core lobby.
     * <p>
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
     *
     * @param event This is a Bukkit FoodLevelChangeEvent.
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    /**
     * We listen to the ItemSpawnEvent to watch for and prevent
     * the spawning of eggs. A egg will spawn from chickens who
     * are used as kit or as team entities.
     *
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
     *
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
     *
     * @param event This is a Bukkit WeatherChangeEvent.
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

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
