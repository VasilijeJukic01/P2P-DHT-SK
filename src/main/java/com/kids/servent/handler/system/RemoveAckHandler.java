package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.app.system.Status;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.RemoveAckMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveAckHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.REMOVE_ACK) {
                AppConfig.chordState.getMutex().unlock();

                RemoveAckMessage ram = (RemoveAckMessage) clientMessage;
                Status status = ram.getStatus();
                String path = ram.getPath();

                if(status == Status.SUCCESS) AppConfig.timestampedStandardPrint(path + " deleted successfully.");
                else if (status == Status.FAILURE) AppConfig.timestampedStandardPrint(path + " can not be deleted, we are not the owner.");
                else AppConfig.timestampedStandardPrint(path + " not found.");
            }
            else {
                AppConfig.timestampedErrorPrint("RemoveAckHandler got a message that is not REMOVE_ACK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
