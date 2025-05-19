package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;

public class VisibilityCommand implements CLICommand {

    @Override
    public String commandName() {
        return "visibility";
    }

    @Override
    public void execute(String args) {
        try {
            if (args == null || args.isEmpty()) {
                AppConfig.timestampedErrorPrint("Invalid argument for VisibilityCommand. Should be public/private");
                return;
            }
            
            String mode = args.trim().toLowerCase();
            
            if (mode.equals("public")) {
                AppConfig.chordState.getSystemManager().getIsPublic().set(true);
                AppConfig.timestampedStandardPrint("Visibility set to PUBLIC - files are visible to nodes that don't follow this node");
            }
            else if (mode.equals("private")) {
                AppConfig.chordState.getSystemManager().getIsPublic().set(false);
                AppConfig.timestampedStandardPrint("Visibility set to PRIVATE - files are only visible to nodes that follow this node");
            }
            else {
                AppConfig.timestampedErrorPrint("Invalid argument for VisibilityCommand. Should be public/private");
            }
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing VisibilityCommand: " + e.getMessage());
        }
    }
}
