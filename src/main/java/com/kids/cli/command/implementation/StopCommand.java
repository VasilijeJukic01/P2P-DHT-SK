package com.kids.cli.command.implementation;

import com.kids.app.AppConfig;
import com.kids.cli.CLIParser;
import com.kids.cli.command.CLICommand;
import com.kids.servent.SimpleServentListener;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StopCommand implements CLICommand {

	private final CLIParser parser;
	private final SimpleServentListener listener;
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
	}

}
