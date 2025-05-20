package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.app.ChordState;
import com.kids.app.servent.ServentIdentity;
import com.kids.cli.command.CLICommand;
import com.kids.file.FileOperations;

import java.util.List;

public class RemoveCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        try {
            if (args == null || args.isEmpty()) {
                AppConfig.timestampedErrorPrint("Invalid argument for RemoveCommand. Should be file path");
                return;
            }

            ChordState chordState = AppConfig.chordState;
            int key = FileOperations.hashFilePath(args);

            List<ServentIdentity> serventIdentity = AppConfig.chordState.getAllNodeInfo().stream()
                    .map(n -> new ServentIdentity(n.getIpAddress(), n.getListenerPort()))
                    .toList();

            chordState.getMutex().lock(serventIdentity, false);
            chordState.getSystemManager().remove(key, args, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing RemoveCommand: " + e.getMessage());
        }
    }
}
