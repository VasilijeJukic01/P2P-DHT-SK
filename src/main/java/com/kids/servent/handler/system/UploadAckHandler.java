package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UploadAckHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.UPLOAD_ACK)
            AppConfig.chordState.getMutex().unlock();
        else AppConfig.timestampedErrorPrint("UploadAck handler got a message that is not UPLOAD_ACK");
    }

}
