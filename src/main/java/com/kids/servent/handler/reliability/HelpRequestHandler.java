package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.HelpConfirmMessage;
import com.kids.servent.message.reliability.HelpRequestMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;

@AllArgsConstructor
public class HelpRequestHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HELP_REQUEST) {
            HelpRequestMessage hrm = (HelpRequestMessage) clientMessage;
            AppConfig.timestampedStandardPrint("Received help request from " + hrm.getSenderIpAddress() + ":" + hrm.getSenderPort() + " to check " + hrm.getSuspiciousNodeIp() + ":" + hrm.getSuspiciousNodePort());

            boolean nodeIsAlive = false;
            try (Socket socket = new Socket(hrm.getSuspiciousNodeIp(), hrm.getSuspiciousNodePort())) {
                nodeIsAlive = true;
                AppConfig.timestampedStandardPrint("Helper check: Node " + hrm.getSuspiciousNodeIp() + ":" + hrm.getSuspiciousNodePort() + " is ALIVE.");
            } catch (IOException e) {
                AppConfig.timestampedStandardPrint("Helper check: Failed to connect to " + hrm.getSuspiciousNodeIp() + ":" + hrm.getSuspiciousNodePort() + ". Node appears to be DEAD.");
            }

            HelpConfirmMessage hcm = new HelpConfirmMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    hrm.getOriginatorIp(), hrm.getOriginatorPort(),
                    hrm.getSuspiciousNodeIp(), hrm.getSuspiciousNodePort(),
                    nodeIsAlive
            );
            MessageUtil.sendMessage(hcm);
            AppConfig.timestampedStandardPrint("Sent confirmation [" + (nodeIsAlive ? "ALIVE" : "DEAD") + "] to " + hrm.getOriginatorIp() + ":" + hrm.getOriginatorPort() + " about " + hrm.getSuspiciousNodeIp() + ":" + hrm.getSuspiciousNodePort());
        }
        else {
            AppConfig.timestampedErrorPrint("HelpRequestHandler received a message that is not HELP_REQUEST: " + clientMessage.getMessageType());
        }
    }
}
