package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.file.Visibility;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.UploadMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UploadHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.UPLOAD) {
            UploadMessage uploadMessage = (UploadMessage) clientMessage;

            int key = uploadMessage.getKey();
            String path = uploadMessage.getPath();
            String address = uploadMessage.getRequesterIpAddress();
            int port = uploadMessage.getRequesterPort();
            Visibility visibility = uploadMessage.getVisibility();

            AppConfig.chordState.getSystemManager().upload(key, path, address, port, visibility);
        }
        else {
            AppConfig.timestampedErrorPrint("Upload handler got a message that is not UPLOAD");
        }
    }

}
