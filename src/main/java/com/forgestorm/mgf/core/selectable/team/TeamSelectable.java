package com.forgestorm.mgf.core.selectable.team;

import com.forgestorm.mgf.constants.MinigameMessages;
import com.forgestorm.mgf.constants.PedestalLocations;
import com.forgestorm.mgf.core.games.Minigame;
import com.forgestorm.mgf.core.selectable.LobbySelectable;
import com.forgestorm.mgf.player.PlayerMinigameData;
import com.forgestorm.mgf.util.world.PedestalMapping;
import com.forgestorm.spigotcore.constants.UserGroup;
import com.forgestorm.spigotcore.database.PlayerProfileData;
import com.forgestorm.spigotcore.util.display.Hologram;
import com.forgestorm.spigotcore.util.scoreboard.ScoreboardManager;
import com.forgestorm.spigotcore.util.text.CenterChatText;
import com.forgestorm.spigotcore.util.text.ColorMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 8/17/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

public class TeamSelectable extends LobbySelectable {

    private final Map<LivingEntity, Team> teamEntities = new HashMap<>();
    private final Map<Team, Hologram> teamHolograms = new HashMap<>();

    public TeamSelectable(Minigame minigame) {
        super(minigame);
    }

    @Override
    public void setup() {
        List<Team> teamsList = minigame.getTeamList();

        // Determine the number of teams and get the center pedestal locations appropriately.
        PedestalMapping pedestalMapping = new PedestalMapping();
        int shiftOver = pedestalMapping.getShiftAmount(teamsList.size());

        int teamsSpawned = 0;

        // Spawn the teams
        for (Team team : teamsList) {

            // Get platform location
            PedestalLocations pedLoc = PedestalLocations.values()[9 + shiftOver + teamsSpawned];
            pedestalLocations.add(pedLoc);

            // Spawn platform
            platformBuilder.setPlatform(minigame.getLobbyWorld(), pedLoc, team.getTeamPlatformMaterials());

            // Spawn entities
            LivingEntity entity = spawnSelectableEntity(
                    team.getTeamColor() + team.getTeamName(),
                    team.getTeamEntityType(),
                    pedLoc,
                    UserGroup.MINIGAME_TEAM);

            // Add the team selection entities UUID's to an array list.
            // This is used to keep track of which one is being clicked for team selection.
            teamEntities.put(entity, team);

            // Spawn holograms
            spawnHolograms(team, pedLoc.getLocation(minigame.getLobbyWorld()));

            teamsSpawned++;
        }
    }

    @Override
    public void destroy() {

        // Remove entities
        for (LivingEntity entity : teamEntities.keySet()) {
            plugin.getSpigotCore().getScoreboardManager().removeEntityFromTeam(entity, UserGroup.MINIGAME_TEAM.getTeamName());
            entity.remove();
        }

        // Remove platforms
        platformBuilder.clearPlatform(minigame.getLobbyWorld(), pedestalLocations);

        // Remove Holograms
        for (Hologram hologram : teamHolograms.values()) hologram.removeHolograms();

        // Unregister listeners
        PlayerKickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);

        // Clear list and maps
        teamEntities.clear();
        teamHolograms.clear();
    }

    @Override
    public void toggleInteract(Player player, Entity entity) {
        if (!teamEntities.containsKey(entity)) return;

        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
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
        hologram.createHologram(hologramText, location.add(.5, 3.1, .5));

        teamHolograms.put(team, hologram);
    }

    /**
     * Update holograms after a queue update or a team enter.
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
     * This will select the players default team.
     *
     * @param player The player to setup.
     */
    public void initPlayer(Player player) {
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
        Team smallestTeam = null;
        Team teamToJoin = null;

        for (Team team : minigame.getTeamList()) {
            // Try to enter empty team first.
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

        assert teamToJoin != null;

        teamToJoin.getTeamPlayers().add(player);
        playerMinigameData.setSelectedTeam(teamToJoin);

        // Team enter message.
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "You have joined the " +
                teamToJoin.getTeamColor() + ChatColor.BOLD + teamToJoin.getTeamName() + ChatColor.GREEN + " team.");

        // Update the holograms
        updateHolograms();
    }

    /**
     * This will attempt to add players to a team and if it is not yet possible, we
     * will add them to a teams queue.
     *
     * @param player The player who wants to switch teams.
     * @param team   The team the player wants to enter.
     */
    private void joinTeam(Player player, Team team) {
        // Update any current queues!
        updateTeamJoinQueues();

        PlayerProfileData playerProfileData = gameManager.getPlugin().getSpigotCore().getProfileManager().getProfile(player);
        PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);
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

            // Team enter message.
            player.sendMessage("");
            player.sendMessage(ColorMessage.color("&aYou have joined the " +
                    team.getTeamColor() + "&l" + team.getTeamName() + "&a team."));

            // Change scoreboard prefix/usergroup
            ScoreboardManager scoreboardManager = plugin.getSpigotCore().getScoreboardManager();
            scoreboardManager.removePlayer(player);

            String teamName = ChatColor.stripColor((Integer.toString(playerProfileData.getUserGroup()) + team.getTeamName()).replace(" ", "_"));
            String teamPrefix = playerProfileData.getPlayerUsergroup().getUserGroupPrefix() + team.getTeamColor();

            player.sendMessage("teamName: " + teamName + ChatColor.RESET + "teamPrefix: " + teamPrefix + " " + player.getName());

            scoreboardManager.addPlayer(player, teamName, teamPrefix, "");

        } else {
            // Remove from last team queue.
            if (lastQueuedTeam.getQueuedPlayers().contains(player)) lastQueuedTeam.getQueuedPlayers().remove(player);

            // Setup new queue
            team.getQueuedPlayers().add(player);
            playerMinigameData.setQueuedTeam(team);

            // Team queue message.
            player.sendMessage("");
            player.sendMessage(ColorMessage.color("&eYou have joined the " +
                    team.getTeamColor() + "&l" + team.getTeamName() + "&e queue."));
        }

        updateHolograms();
    }

    /**
     * This code is used to update and current team queues.  If players can move to a team,
     * we will make that happen.
     */
    private void updateTeamJoinQueues() {
        for (Team team : minigame.getTeamList()) {
            if (team.getQueuedPlayers().isEmpty()) return;

            if (team.getTeamPlayers().size() < team.getTeamSizes()) {

                Player player = team.getQueuedPlayers().remove(); // Grabs next queued player
                PlayerMinigameData playerMinigameData = gameManager.getPlayerMinigameManager().getPlayerProfileData(player);

                // Remove queue team
                playerMinigameData.setQueuedTeam(null);

                // Join the team
                team.getTeamPlayers().add(player);
                playerMinigameData.setSelectedTeam(team);

                // Team enter message.
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "You have joined the " +
                        team.getTeamColor() + ChatColor.BOLD + team.getTeamName() + ChatColor.GREEN + " team.");
            }
        }
    }

    /**
     * Code to remove the player from queues and team lists.
     *
     * @param player The player who left the server.
     */
    private void playerQuit(Player player) {
        // Remove the player from team lists.
        for (Team team : minigame.getTeamList()) {
            if (team.getTeamPlayers().contains(player)) team.getTeamPlayers().remove(player);
            if (team.getDeadPlayers().contains(player)) team.getDeadPlayers().remove(player);
            if (team.getQueuedPlayers().contains(player)) team.getQueuedPlayers().remove(player);
        }

        // Update queues and holograms.
        updateTeamJoinQueues();
        updateHolograms();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        playerQuit(event.getPlayer());
    }
}
