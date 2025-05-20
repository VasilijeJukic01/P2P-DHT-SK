package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.RemoveReplicaMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveReplicaHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.REMOVE_REPLICA) {
                RemoveReplicaMessage rrm = (RemoveReplicaMessage) clientMessage;

                String path = rrm.getPath();
                AppConfig.chordState.getSystemManager().removeReplicasHandle(path);
            }
            else {
                AppConfig.timestampedErrorPrint("RemoveReplicaHandler got a message that is not REMOVE_REPLICA");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
