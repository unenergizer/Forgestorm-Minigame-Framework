package com.forgestorm.mgf.commands;

import com.forgestorm.mgf.MinigameFramework;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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

@AllArgsConstructor
public class Admin implements CommandExecutor {

	private final MinigameFramework plugin;

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

		// set min players

		// set max players

		return false;
	}

}
