package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.app.ChordState;
import com.kids.file.FileData;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.ListResponseMessage;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ListResponseHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.LIST_IMAGES_RESPONSE) {
                ChordState chordState = AppConfig.chordState;
                chordState.getMutex().unlock();

                List<FileData> files = ((ListResponseMessage) clientMessage).getFileData();

                if(chordState.getSystemManager().getIsPublic().get()) AppConfig.timestampedStandardPrint("Listed images: " + files.toString());
                else AppConfig.timestampedStandardPrint("ListResponseHandler: " + files.size() + " images found, but not in public mode. ");
            }
            else {
                AppConfig.timestampedErrorPrint("ListResponseHandler got a message that is not LIST_IMAGES_RESPONSE");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
