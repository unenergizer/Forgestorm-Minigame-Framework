package com.forgestorm.mgf.core.team;

import com.forgestorm.mgf.MinigameFramework;
import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.constants.PedestalLocations;
import com.forgestorm.mgf.core.GameManager;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.util.world.PedestalMapping;
import com.forgestorm.mgf.util.world.PlatformBuilder;
import com.forgestorm.spigotcore.constants.UserGroup;
import com.forgestorm.spigotcore.util.display.Hologram;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class TeamManager implements Listener {

    private final MinigameFramework plugin;
    private final GameManager gameManager;
    private final PlatformBuilder platformBuilder;
    private final List<Team> teamsList;
    private final Map<Entity, Team> teamEntities = new HashMap<>();
    private final Map<Team, Hologram> teamHolograms = new HashMap<>();
    private final World lobbyWorld;
    private final List<PedestalLocations> pedestalLocations = new ArrayList<>();

    public TeamManager(MinigameFramework plugin, GameManager gameManager, PlatformBuilder platformBuilder, List<Team> teamsList, World lobbyWorld) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.platformBuilder = platformBuilder;
        this.teamsList = teamsList;
        this.lobbyWorld = lobbyWorld;
    }

    /**
     * This will setup teams for the current minigame.
     * This will spawn the platform the entity stands
     * on and it will place the entity on it for team
     * representation.
     * <p>
     * Registers stat listeners for this class.
     */
    public void setupTeams() {

        // Determine the number of teams and get the center pedestal locations appropriately.
        PedestalMapping pedestalMapping = new PedestalMapping();
        int shiftOver = pedestalMapping.getShiftAmount(teamsList.size());

        int teamsSpawned = 0;

        // Spawn the teams
        for (Team team : teamsList) {
            // Get platform location
            PedestalLocations pedLoc = PedestalLocations.values()[9 + shiftOver + teamsSpawned];
            pedestalLocations.add(pedLoc);

            // Spawn the platform
            platformBuilder.setPlatform(lobbyWorld, pedLoc, team.getTeamPlatformMaterials());

            // Spawn entities
            spawnEntity(team, pedLoc);

            teamsSpawned++;
        }

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * This will remove team related objects from our
     * lobby world. Some things removed are the
     * platforms entities stand on and the entities
     * themselves.
     * <p>
     * Unregister stat listeners for this class
     */
    public void destroyTeams() {
        // Remove entities
        for (Entity entity : teamEntities.keySet()) {
            plugin.getSpigotCore().getScoreboardManager().getScoreboard().getTeam(UserGroup.MINIGAME_TEAM.getTeamName()).removeEntry(entity.getUniqueId().toString());
            entity.remove();
        }

        // Remove platforms
        platformBuilder.clearPlatform(lobbyWorld, pedestalLocations);

        // Remove Holograms
        Iterator<Hologram> hologramIterator = teamHolograms.values().iterator();
        while (hologramIterator.hasNext()) hologramIterator.next().removeHolograms();
        teamHolograms.clear();

        // Unregister Listeners
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    /**
     * Conditional test to make sure all the teams have players.
     *
     * @return True if all teams have players, false otherwise.
     */
    public boolean allTeamsHavePlayers() {
        for (Team team : teamsList) {
            if (team.getTeamSizes() < 1) return false;
        }
        return true;
    }

    /**
     * This will select the players default team.
     *
     * @param player The player to setup.
     */
    public void initPlayer(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        Team smallestTeam = null;
        Team teamToJoin = null;

        for (Team team : teamsList) {
            // Try to join empty team first.
            if (team.getTeamPlayers().size() == 0) {
                teamToJoin = team;
                break;
            } else {
                if (smallestTeam != null) {
                    if (team.getTeamPlayers().size() < smallestTeam.getTeamPlayers().size()) {
                        smallestTeam = team;
                    }
                } else {
                    smallestTeam = team;
                }
            }
        }

        if (teamToJoin == null) {
            teamToJoin = smallestTeam;
        }

        teamToJoin.getTeamPlayers().add(player);
        playerMinigameData.setSelectedTeam(teamToJoin);

        // Team join message.
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "You have joined the " +
                teamToJoin.getTeamColor() + ChatColor.BOLD + teamToJoin.getTeamName() + ChatColor.GREEN + " team.");

        // Update the holograms
        updateHolograms();
    }

    /**
     * Called when a player clicks on a team mob.
     *
     * @param player The player who left or right clicked a team mob.
     * @param entity The entity the player left or right clicked.
     */
    private void toggleTeamInteract(Player player, Entity entity) {
        if (!teamEntities.containsKey(entity)) return;

        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        Team clickedTeam = teamEntities.get(entity);
        Team currentTeam = playerMinigameData.getSelectedTeam();
        Team queuedTeam = playerMinigameData.getQueuedTeam();
        String sameTeamMessage = "";

        boolean clickedSameTeam = false;
        if (currentTeam != null) {
            if (currentTeam.equals(clickedTeam)) {
                clickedSameTeam = true;
            }
        }

        boolean clickedSameQueuedTeam = false;
        if (queuedTeam != null) {
            if (queuedTeam.equals(clickedTeam)) {
                clickedSameQueuedTeam = true;
            }
        }

        //If the player has interacted with a team they are on, add a little message to the description.
        if (clickedSameTeam) {
            sameTeamMessage = " " + MinigameMessages.TEAM_ALREADY_ON_TEAM.toString();
        } else if (clickedSameQueuedTeam) {
            sameTeamMessage = " " + MinigameMessages.TEAM_ALREADY_ON_QUEUE.toString();
        }

        //Set the the players team.
        joinTeam(player, clickedTeam);

        //Set player a confirmation message.
        player.sendMessage("");
        player.sendMessage(MinigameMessages.GAME_BAR_TEAM.toString());
        player.sendMessage("");
        player.sendMessage(CenterChatText.centerChatMessage(ChatColor.GRAY + "Team: " +
                clickedTeam.getTeamColor() + clickedTeam.getTeamName() + sameTeamMessage));
        player.sendMessage("");

        for (String description : clickedTeam.getTeamDescription()) {
            String message = CenterChatText.centerChatMessage(ChatColor.YELLOW + description);
            player.sendMessage(message);
        }

        player.sendMessage("");
        player.sendMessage(MinigameMessages.GAME_BAR_BOTTOM.toString());
        player.sendMessage("");

        //Play a confirmation sound.
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, .5f, .6f);

        //Update the lobby scoreboard.
        gameManager.getGameLobby().getTarkanLobbyScoreboard().updatePlayerTeam(player, playerMinigameData);
    }

    /**
     * This will attempt to add players to a team and if it is not yet possible, we
     * will add them to a teams queue.
     *
     * @param player The player who wants to switch teams.
     * @param team   The team the player wants to join.
     */
    private void joinTeam(Player player, Team team) {
        // Update any current queues!
        updateTeamJoinQueues();

        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);
        Team lastTeam = playerMinigameData.getSelectedTeam();
        Team lastQueuedTeam = playerMinigameData.getQueuedTeam();

        if (team == lastTeam) return; // cancel joining same team
        if (lastQueuedTeam != null || team == lastQueuedTeam) return; // cancel joining queued team again

        if (team.getTeamPlayers().size() < team.getTeamSizes() || team.getTeamSizes() == -1) {
            // Remove from last team or team queue.
            if (lastQueuedTeam != null && lastQueuedTeam.getQueuedPlayers().contains(player)) {
                lastQueuedTeam.getQueuedPlayers().remove(player);
            }
            if (lastTeam != null) lastTeam.getTeamPlayers().remove(player);
            playerMinigameData.setQueuedTeam(null);

            // Add players to this team.
            team.getTeamPlayers().add(player);
            playerMinigameData.setSelectedTeam(team);

            // Team join message.
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "You have joined the " +
                    team.getTeamColor() + ChatColor.BOLD + team.getTeamName() + ChatColor.GREEN + " team.");

        } else {
            // Remove from last team queue.
            if (lastQueuedTeam.getQueuedPlayers().contains(player)) lastQueuedTeam.getQueuedPlayers().remove(player);

            // Setup new queue
            team.getQueuedPlayers().add(player);
            playerMinigameData.setQueuedTeam(team);

            // Team queue message.
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "You have joined the " +
                    team.getTeamColor() + ChatColor.BOLD + team.getTeamName() + ChatColor.YELLOW + " queue.");
        }

        updateHolograms();
    }

    public void playerQuit(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);

        // Remove player from team list.
        if (playerMinigameData.getSelectedTeam() != null) {
            if (playerMinigameData.getSelectedTeam().getTeamPlayers() != null) {
                playerMinigameData.getSelectedTeam().getTeamPlayers().remove(player);
            }
        }

        // Remove player from queue list.
        if (playerMinigameData.getQueuedTeam() != null) {
            if (playerMinigameData.getQueuedTeam().getTeamPlayers() != null) {
                playerMinigameData.getQueuedTeam().getTeamPlayers().remove(player);
            }
        }

        // Update queues and holograms.
        updateTeamJoinQueues();
        updateHolograms();
    }

    /**
     * This code is used to update and current team queues.  If players can move to a team,
     * we will make that happen.
     */
    private void updateTeamJoinQueues() {
        for (Team team : teamsList) {
            if (team.getQueuedPlayers().isEmpty()) return;

            if (team.getTeamPlayers().size() < team.getTeamSizes()) {

                Player player = team.getQueuedPlayers().remove(); // Grabs next queued player
                PlayerMinigameData playerMinigameData = gameManager.getPlayerManager().getPlayerProfileData(player);

                // Remove queue team
                playerMinigameData.setQueuedTeam(null);

                // Join the team
                team.getTeamPlayers().add(player);
                playerMinigameData.setSelectedTeam(team);

                // Team join message.
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "You have joined the " +
                        team.getTeamColor() + ChatColor.BOLD + team.getTeamName() + ChatColor.GREEN + " team.");
            }
        }
    }

    /**
     * Update holograms after a queue update or a team join.
     */
    private void updateHolograms() {
        // Run name change one tick later to fix strange bug.
        // The bug that happens is one player will see the updated
        // name but the others will not. Really strange.
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Team, Hologram> entry : teamHolograms.entrySet()) {
                    Team team = entry.getKey();
                    Hologram hologram = entry.getValue();
                    int queuedCount = team.getQueuedPlayers().size();
                    int currentTeamSize = team.getTeamPlayers().size();
                    int maxTeamSize;

                    if (team.getTeamSizes() == -1) {
                        maxTeamSize = gameManager.getMaxPlayersOnline();
                    } else {
                        maxTeamSize = team.getTeamSizes();
                    }

                    ArmorStand players = hologram.getArmorStands().get(0);
                    ArmorStand queue = hologram.getArmorStands().get(1);
                    players.setCustomName(ChatColor.BOLD + "Players: " + Integer.toString(currentTeamSize) + " / " + maxTeamSize);
                    queue.setCustomName(ChatColor.BOLD + "Queued Players: " + queuedCount);
                }
            }
        }.runTaskLater(plugin, 1);
    }

    /**
     * Holograms are spawned above entity heads
     *
     * @param team     The team we are setting up a hologram for.
     * @param location The location to spawn the holograms.
     */
    private void spawnHolograms(Team team, Location location) {
        List<String> hologramText = new ArrayList<>();
        int queuedCount = team.getQueuedPlayers().size();
        int currentTeamSize = team.getTeamPlayers().size();
        int maxTeamSize;

        if (team.getTeamSizes() == -1) {
            maxTeamSize = gameManager.getMaxPlayersOnline();
        } else {
            maxTeamSize = team.getTeamSizes();
        }

        hologramText.add(ChatColor.BOLD + "Players: " + Integer.toString(currentTeamSize) + " / " + maxTeamSize);
        hologramText.add(ChatColor.BOLD + "Queued Players: " + queuedCount);

        Hologram hologram = new Hologram();
        hologram.createHologram(hologramText, location.add(0, 2, 0));

        teamHolograms.put(team, hologram);
    }

    /**
     * This will spawn the entity that players click on
     * for team selection.
     *
     * @param team The team we are spawning an entity for.
     */
    private void spawnEntity(Team team, PedestalLocations pedLoc) {

        Location entityLocation = pedLoc.getLocation(lobbyWorld).add(.5, 1.5, .5);

        // Generate and spawn the entity.
        LivingEntity entity = (LivingEntity) lobbyWorld.spawnEntity(entityLocation, team.getTeamEntityType());

        entity.setCustomName(team.getTeamColor() + team.getTeamName());
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);
        entity.setCanPickupItems(false);
        entity.setCollidable(false);
        entity.setAI(false);

        // Fix to make mobs face the correct direction.
        entity.teleport(entityLocation);

        // Add the prefix to the team entity username.
        plugin.getSpigotCore().getScoreboardManager().getScoreboard().getTeam(UserGroup.MINIGAME_TEAM.getTeamName()).addEntry(entity.getUniqueId().toString());

        // Add the team selection entities UUID's to an array list.
        // This is used to keep track of which one is being clicked for team selection.
        teamEntities.put(entity, team);

        // Spawn holograms
        spawnHolograms(team, entityLocation);
    }

    @EventHandler
    public void onTeamRightClick(PlayerInteractAtEntityEvent event) {
        toggleTeamInteract(event.getPlayer(), event.getRightClicked());
    }

    @EventHandler
    public void onTeamLeftClick(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            event.setCancelled(true); // Cancel damage
            toggleTeamInteract((Player) event.getDamager(), event.getEntity());
        }
    }
}
