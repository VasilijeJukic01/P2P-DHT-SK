package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;

import java.util.List;

public class PendingCommand implements CLICommand {

    @Override
    public String commandName() {
        return "pending";
    }

    @Override
    public void execute(String args) {
        try {
            List<String> pendingFollows = AppConfig.chordState.getSystemManager().getPendingFollows();

            if (pendingFollows.isEmpty()) {
                AppConfig.timestampedStandardPrint("No pending follow requests");
            }
            else {
                AppConfig.timestampedStandardPrint("Pending follow requests:");
                for (int i = 0; i < pendingFollows.size(); i++) {
                    AppConfig.timestampedStandardPrint((i+1) + ". " + pendingFollows.get(i));
                }
                AppConfig.timestampedStandardPrint("To accept a request, use 'accept [address:port]' command");
            }
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing PendingCommand: " + e.getMessage());
        }
    }
}