package com.kids.cli.command.implementation;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("My info: " + AppConfig.myServentInfo);
	}

}
