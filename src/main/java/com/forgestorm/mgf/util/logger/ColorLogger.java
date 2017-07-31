package com.forgestorm.mgf.util.logger;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/*********************************************************************************
 *
 * OWNER: Robert Andrew Brown & Joseph Rugh
 * PROGRAMMER: Robert Andrew Brown & Joseph Rugh
 * PROJECT: forgestorm-minigame-framework
 * DATE: 7/31/2017
 * _______________________________________________________________________________
 *
 * Copyright © 2017 ForgeStorm.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code and/or source may be 
 * reproduced, distributed, or transmitted in any form or by any means, 
 * including photocopying, recording, or other electronic or mechanical methods, 
 * without the prior written permission of the owner.
 */

/**
 * Logger Info from Wikipedia.
 * Link: https://en.wikipedia.org/wiki/Java_logging_framework
 */
public enum ColorLogger {
    //Severe errors that cause premature termination. Expect these to be immediately visible on a status console.
    FATAL(ChatColor.DARK_RED),

    //Other runtime errors or unexpected conditions. Expect these to be immediately visible on a status console.
    ERROR(ChatColor.RED),

    WARNING(ChatColor.YELLOW),

    //Interesting runtime events (startup/shutdown). Expect these to be immediately visible on a console, so be conservative and keep to a minimum.
    INFO(ChatColor.AQUA),

    //Detailed information on the flow through the system. Expect these to be written to logs only.
    DEBUG(ChatColor.GREEN);

    private ChatColor chatColor;

    ColorLogger(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public void printLog(String logMessage) {
        Bukkit.getServer().getConsoleSender().sendMessage(getLogLevel() + chatColor + logMessage);
    }

    public void printLog(boolean printLog, String logMessage) {
        if (printLog) Bukkit.getServer().getConsoleSender().sendMessage(getLogLevel() + chatColor + logMessage);
    }

    private String getLogLevel() {
        return "[CL " + toString() + "] ";
    }
}
