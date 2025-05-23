package com.kids.cli.command.implementation.reliability;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;

public class SimulateCrashCommand implements CLICommand {

    @Override
    public String commandName() {
        return "simulate_crash";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Simulating node crash...");
        System.exit(1);
    }

}
