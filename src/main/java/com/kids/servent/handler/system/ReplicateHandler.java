package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.file.FileData;
import com.kids.file.FileOperations;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.ReplicateMessage;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class ReplicateHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.REPLICATE) {
            ReplicateMessage replicateMessage = (ReplicateMessage) clientMessage;
            FileData fileDataFromMessage = replicateMessage.getFileData();
            int key = FileOperations.hashFilePath(fileDataFromMessage.path());

            // Check if we already have this exact file (path + original uploader)
            Map<String, FileData> filesAtKey = AppConfig.chordState.getData().get(key);
            boolean alreadyExists = false;
            if (filesAtKey != null) {
                FileData existingFileData = filesAtKey.get(fileDataFromMessage.path());
                if (existingFileData != null && existingFileData.serventIdentity().equals(fileDataFromMessage.serventIdentity())) {
                    alreadyExists = true;
                    AppConfig.timestampedStandardPrint("Received ReplicateMessage for " + fileDataFromMessage.path() +
                            " from " + replicateMessage.getSenderIpAddress() + ":" + replicateMessage.getSenderPort() +
                            ", but I already have this exact replica.");
                }
            }

            if (!alreadyExists) {
                AppConfig.chordState.getSystemManager().putIntoData(
                        key,
                        fileDataFromMessage.path(),
                        fileDataFromMessage.serventIdentity().ip(),
                        fileDataFromMessage.serventIdentity().port()
                );
                AppConfig.timestampedStandardPrint("Stored replica of image " + fileDataFromMessage.path() +
                        " (originally uploaded by: " + fileDataFromMessage.serventIdentity() +
                        ", replicated by: " + replicateMessage.getSenderIpAddress() + ":" + replicateMessage.getSenderPort() + ")");
            }
        } else {
            AppConfig.timestampedErrorPrint("Replicate handler got a message that is not REPLICATE");
        }
    }

}
