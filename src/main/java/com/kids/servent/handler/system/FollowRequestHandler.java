package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.FollowRequestMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FollowRequestHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.FOLLOW_REQUEST) {
                FollowRequestMessage frm = (FollowRequestMessage) clientMessage;

                AppConfig.chordState.getSystemManager().addPendingFollow(frm.getSenderIpAddress(), frm.getSenderPort());
            }
            else {
                AppConfig.timestampedErrorPrint("FollowRequestHandler got a message that is not FOLLOW_REQUEST");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}