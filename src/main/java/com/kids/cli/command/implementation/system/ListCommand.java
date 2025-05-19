package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.app.ChordState;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.cli.command.CLICommand;
import com.kids.servent.message.system.ListRequestMessage;
import com.kids.servent.message.util.MessageUtil;

import java.util.List;

public class ListCommand implements CLICommand {

    @Override
    public String commandName() {
        return "list_files";
    }

    @Override
    public void execute(String args) {
        try {
            if (args == null || args.isEmpty()) {
                AppConfig.timestampedErrorPrint("Invalid argument for ListCommand. Should be address:port");
                return;
            }

            String[] parts = args.split(":");
            if (parts.length != 2) {
                AppConfig.timestampedErrorPrint("Invalid argument format for ListCommand: " + args + ". Should be address:port");
                return;
            }

            String address = parts[0];
            int port = Integer.parseInt(parts[1]);

            int key = ChordState.chordHash(address + ":" + port);

            List<ServentIdentity> serventIdentities = AppConfig.chordState.getAllNodeInfo().stream()
                    .map(n -> new ServentIdentity(n.getIpAddress(), n.getListenerPort()))
                    .toList();

            AppConfig.chordState.getMutex().lock(serventIdentities, false);
            
            AppConfig.timestampedStandardPrint("Requesting files from node: " + address + ":" + port);

            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);

            ListRequestMessage listRequestMessage = new ListRequestMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    nextNode.getIpAddress(),
                    nextNode.getListenerPort(),
                    key
            );
            
            MessageUtil.sendMessage(listRequestMessage);
            
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing ListCommand: " + e.getMessage());
        }
    }
}
