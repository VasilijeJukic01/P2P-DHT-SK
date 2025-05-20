package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;
import com.kids.servent.message.system.FollowRequestMessage;
import com.kids.servent.message.util.MessageUtil;

public class FollowCommand implements CLICommand {

    @Override
    public String commandName() {
        return "follow";
    }

    @Override
    public void execute(String args) {
        try {
            if (args == null || args.isEmpty()) {
                AppConfig.timestampedErrorPrint("Invalid argument for FollowCommand. Should be address:port");
                return;
            }

            String[] parts = args.split(":");
            if (parts.length != 2) {
                AppConfig.timestampedErrorPrint("Invalid argument format for FollowCommand: " + args + ". Should be address:port");
                return;
            }

            String address = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Cant follow yourself
            if (address.equals(AppConfig.myServentInfo.getIpAddress()) && port == AppConfig.myServentInfo.getListenerPort()) {
                AppConfig.timestampedErrorPrint("Cannot follow yourself");
                return;
            }

            AppConfig.timestampedStandardPrint("Sending follow request to " + address + ":" + port);

            FollowRequestMessage frm = new FollowRequestMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    address,
                    port
            );

            MessageUtil.sendMessage(frm);
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error executing FollowCommand: " + e.getMessage());
        }
    }
}