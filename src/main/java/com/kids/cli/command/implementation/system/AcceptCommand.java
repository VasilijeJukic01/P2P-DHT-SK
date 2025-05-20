package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;

public class AcceptCommand implements CLICommand {

    @Override
    public String commandName() {
        return "accept";
    }

    @Override
    public void execute(String args) {
        try {
            if (args == null || args.isEmpty()) {
                AppConfig.timestampedErrorPrint("Invalid argument for AcceptCommand. Should be address:port");
                return;
            }

            if (!AppConfig.chordState.getSystemManager().getPendingFollows().contains(args)) {
                AppConfig.timestampedErrorPrint("No pending follow request from " + args);
                return;
            }

            String[] parts = args.split(":");
            if (parts.length != 2) {
                AppConfig.timestampedErrorPrint("Invalid argument format for AcceptCommand: " + args + ". Should be address:port");
                return;
            }

            String address = parts[0];
            int port = Integer.parseInt(parts[1]);

            AppConfig.chordState.getSystemManager().acceptFollow(address, port);

        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing AcceptCommand: " + e.getMessage());
        }
    }
}