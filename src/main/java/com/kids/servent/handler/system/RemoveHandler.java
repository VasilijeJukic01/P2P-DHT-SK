package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.RemoveMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.REMOVE) {
                RemoveMessage rm = (RemoveMessage) clientMessage;

                int key = rm.getKey();
                String value = rm.getPath();
                String originalAddress = rm.getOriginalSenderAddress();
                int originalPort = rm.getOriginalSenderPort();

                AppConfig.chordState.getSystemManager().remove(key, value, originalAddress, originalPort);
            }
            else {
                AppConfig.timestampedErrorPrint("RemoveHandler got a message that is not REMOVE");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
