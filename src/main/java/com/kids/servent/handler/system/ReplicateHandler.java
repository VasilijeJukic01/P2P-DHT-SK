package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.file.FileData;
import com.kids.file.FileOperations;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.ReplicateMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReplicateHandler implements MessageHandler {

    // TODO: Why are you duplicated???
    private Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.REPLICATE) {
            ReplicateMessage replicateMessage = (ReplicateMessage) clientMessage;
            FileData fileData = replicateMessage.getFileData();

            int key = FileOperations.hashFilePath(fileData.path());
            String path = fileData.path();
            int originalPort = fileData.serventIdentity().port();
            String originalAddress = fileData.serventIdentity().ip();

            AppConfig.chordState.getSystemManager().putIntoData(key, path, originalAddress, originalPort);
            AppConfig.timestampedStandardPrint("Stored replica of image " + path + " (originally uploaded by: " + originalAddress + ":" + originalPort + ")");
        }
        else {
            AppConfig.timestampedErrorPrint("Replicate handler got a message that is not REPLICATE");
        }
    }

}
