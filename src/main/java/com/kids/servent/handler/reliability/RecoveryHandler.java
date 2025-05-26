package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
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
                AppConfig.timestampedStandardPrint("Processing RECOVERY message for node: " + rm.getRemoving());

                ServentInfo nodeToRemove = rm.getRemoving();
                boolean nodeExisted = AppConfig.chordState.getAllNodeInfo().stream()
                        .anyMatch(n -> n.getChordId() == nodeToRemove.getChordId());

                if (nodeExisted) {
                    AppConfig.chordState.removeNode(nodeToRemove);
                    AppConfig.timestampedStandardPrint("Node " + nodeToRemove + " removed from local state due to RECOVERY message.");
                    // If node's replication targets might have changed, ensure its data is replicated
                    AppConfig.chordState.getSystemManager().ensureDataReplication();
                }
                else {
                    AppConfig.timestampedStandardPrint("Node " + nodeToRemove + " was already removed or not known. Ignoring RECOVERY actions.");
                }
            }
            else {
                System.out.println("RecoveryHandler got a message that is not RECOVERY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
