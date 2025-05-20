package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.FollowAcceptMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FollowAcceptHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.FOLLOW_ACCEPT) {
                FollowAcceptMessage fam = (FollowAcceptMessage) clientMessage;

                AppConfig.timestampedStandardPrint("Follow request has been accepted " + fam.getSenderIpAddress() + ":" + fam.getSenderPort());
            }
            else {
                AppConfig.timestampedErrorPrint("FollowAcceptHandler got a message that is not FOLLOW_ACCEPT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}