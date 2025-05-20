package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class RemoveReplicaMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3153103819491123371L;
    private final String path;
    private final int key;

    public RemoveReplicaMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int key, String path) {
        super(MessageType.REMOVE_REPLICA, senderIpAddress, senderPort, receiverIpAddress, receiverPort, key + ":" + path);
        this.key = key;
        this.path = path;
    }

}
