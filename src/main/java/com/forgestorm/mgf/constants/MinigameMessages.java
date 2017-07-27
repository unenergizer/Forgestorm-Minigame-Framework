package com.forgestorm.mgf.constants;

import org.bukkit.ChatColor;

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

public enum MinigameMessages {
	
	//Debug messages
	BOSS_BAR_LOBBY_MESSAGE("&e&lFORGESTORM &7&l- &a&lMINIGAMES"),
	BOSS_BAR_SPECTATOR_MESSAGE("&e&lFORGESTORM &7&l- &a&lMINIGAMES"),
	
	//Commands
	ADMIN("&7[&cAdmin&7] "),
	COMMAND_ADMIN_NOT_OP("&cYou do not have permission to do this."),
	COMMAND_ADMIN_UNKNOWN("&cCommand unknown! Use &a/admin help &cfor more information!"),
	COMMAND_ADMIN_GAMES_PLAYED("&e&lTotal games played: &c%s"),
	COMMAND_ADMIN_END_GAME("&c&lThe minigame has just been shut down. Returning to minigame lobby."),
	COMMAND_ADMIN_END_ERROR("&c&lYou can not end a minigame if the minigame is not running."),
	COMMAND_ADMIN_FORCE_START("&c&lYou have just force started a minigame."),
	
	//Team MinigameMessages
	TEAM_QUEUE_PLACED("&eYou were placed in a queue to join this team."),
	TEAM_QUEUE_ALREADY_PLACED("&cYou are already queued for this team!"),
	TEAM_ALREADY_ON_TEAM("&7(you are on this team)"),
	TEAM_ALREADY_ON_QUEUE("&7(you are queued for this team)"),
	TEAM_DROPPED_FROM_QUEUE("&cYou have been removed from the %s &cqueue."),
	
	//Kit MinigameMessages
	KIT_ALREADY_HAVE_KIT("&7(you are using this kit)"),
	
	//Game display messages
	GAME_COUNTDOWN_NOT_ENOUGH_PLAYERS("&c&lCountdown canceled! Not enough players!"),
	GAME_COUNTDOWN_ALL_TEAMS_NEED_PLAYERS("&c&lCountdown canceled! All teams need players!"),
	GAME_BAR_KIT("&8&l&m----------------&r&l &l &l &6&lKit Select&l &l &l &8&l&m----------------"),
	GAME_BAR_TEAM("&8&l&m---------------&r&l &l &l &3&lTeam Select&l &l &l &8&l&m----------------"),
	GAME_BAR_RULES("&8&l&m---------------&r&l &l &l &a&lHow to Play&l &l &l &8&l&m----------------"),
	GAME_BAR_SCORES("&8&l&m---------------&r&l &l &l &e&lFinal Scores&l &l &l &8&l&m---------------"),
	GAME_BAR_BOTTOM("&8&l&m---------------------------------------------"),
	GAME_TIME_REMAINING_PLURAL("&e&lGame will start in &c&l%s &e&lseconds."),
	GAME_TIME_REMAINING_SINGULAR("&e&lGame will start in &c&l1 &e&lsecond."),
	GAME_ARENA_SPECTATOR_TITLE("&aHello, Spectator!"),
	GAME_ARENA_SPECTATOR_SUBTITLE("&7Relax, another minigame will start soon!"),
	
	//Join and Quit MinigameMessages
	PLAYER_JOIN_LOBBY("&a+ &8[&7%s&8/&7%f&8] &7%e"),
	PLAYER_QUIT_LOBBY("&c- &8[&7%s&8/&7%f&8] &7%e"),
	PLAYER_QUIT_GAME("&8[&cQuit&8] &7%s"),
	SPECTATOR_JOIN("&a+ &8[&7Spectator&8] &7%s"),
	SPECTATOR_QUIT("&c- &8[&7Spectator&8] &7%s"),
	
	//Scoreboard
	SB_GAME_STATUS_WAITING_1("Need players"),
	SB_GAME_STATUS_READY("Ready!"),
	TSB_GAME("&d&lGAME&7&l:&r "),
    TSB_STATUS("&d&lSTATUS&7&l:&r " ),
    TSB_PLAYERS("&d&lPLAYERS&7&l:&r " ),
    TSB_BLANK_LINE_3("&r&r&r"),
	TSB_TEAM("&lTEAM&7&l:&r "),
	TSB_KIT("&lKIT&7&l:&r "),
    TSB_BLANK_LINE_4("&r&r&r&r"),

	//Menu Items
	MENU_NAME_TRACKER("Player Tracker"),
	MENU_NAME_SPECTATOR("Spectator Menu"),
	MENU_ITEM_SPECTATOR_NO_SPEED("No Speed"),
	MENU_ITEM_SPECTATOR_SPEED_1("Speed 1"),
	MENU_ITEM_SPECTATOR_SPEED_2("Speed 2"),
	MENU_ITEM_SPECTATOR_SPEED_3("Speed 3"),
	MENU_ITEM_SPECTATOR_SPEED_4("Speed 4"),
	MENU_ITEM_SPECTATOR_TRACK_PLAYERS("Track Players");
	
	private String message;
	
	//Constructor
	MinigameMessages(String message) {
		this.message = color(message);
	}
	
	/**
	 * Sends a string representation of the enumerator item.
	 */
	public String toString() {
		return message;
	}
	
	/**
	 * Converts special characters in text into Minecraft client color codes.
	 * <p>
	 * This will give the messages color.
	 * @param msg The message that needs to have its color codes converted.
	 * @return Returns a colored message!
	 */
	public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}