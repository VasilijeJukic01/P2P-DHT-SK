package com.kids.cli.command.implementation;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.cli.command.CLICommand;

public class SuccessorInfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "successor_info";
	}

	@Override
	public void execute(String args) {
		ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();
		
		int num = 0;

		for (ServentInfo serventInfo : successorTable) {
			System.out.println(num + ": " + serventInfo);
			num++;
		}
	}

}
