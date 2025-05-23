package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.RecoveryMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RecoveryHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        try {
            if(clientMessage.getMessageType() == MessageType.RECOVERY) {
                RecoveryMessage rm = (RecoveryMessage) clientMessage;
                AppConfig.chordState.removeNode(rm.getRemoving());
            }
            else {
                System.out.println("RecoveryHandler got a message that is not RECOVERY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
