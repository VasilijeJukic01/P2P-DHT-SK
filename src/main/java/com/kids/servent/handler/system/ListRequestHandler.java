package com.kids.servent.handler.system;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.file.FileData;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.system.ListRequestMessage;
import com.kids.servent.message.system.ListResponseMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ListRequestHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LIST_IMAGES_REQUEST) {
            try {
                ListRequestMessage listRequestMessage = (ListRequestMessage) clientMessage;

                int key = listRequestMessage.getKey();

                if (AppConfig.chordState.isKeyMine(key)) {
                    List<FileData> files;
                    
                    // Check if this node is in public mode or if the requesting node follows this node
                    boolean isPublic = AppConfig.chordState.getSystemManager().getIsPublic().get();
                    boolean requestorFollows = isNodeFollower(clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
                    
                    if (isPublic || requestorFollows) files = AppConfig.chordState.getSystemManager().getAllData();
                    // If private and the requestor does not follow us -> return empty list
                    else {
                        files = new ArrayList<>();
                        AppConfig.timestampedStandardPrint("Request for files received, but node is in PRIVATE mode and requestor doesn't follow this node");
                    }

                    ListResponseMessage lrm = new ListResponseMessage(
                            AppConfig.myServentInfo.getIpAddress(),
                            AppConfig.myServentInfo.getListenerPort(),
                            clientMessage.getSenderIpAddress(),
                            clientMessage.getSenderPort(),
                            key,
                            files
                    );

                    MessageUtil.sendMessage(lrm);
                }
                else {
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                    ListRequestMessage lrm = new ListRequestMessage(
                            clientMessage.getSenderIpAddress(),
                            clientMessage.getSenderPort(),
                            nextNode.getIpAddress(),
                            nextNode.getListenerPort(),
                            key
                    );
                    MessageUtil.sendMessage(lrm);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            AppConfig.timestampedErrorPrint("ListRequestHandler got a message that is not LIST_IMAGES_REQUEST");
        }
    }

    private boolean isNodeFollower(String ipAddress, int port) {
        return AppConfig.chordState.getSystemManager().getFollowers().stream()
                .map(node -> node.split(":"))
                .anyMatch(parts -> parts[0].equals(ipAddress) && Integer.parseInt(parts[1]) == port);
    }
}
