package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class RemoveMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 3433102811491428374L;
    private final String originalSenderAddress;
    private final int originalSenderPort;
    private final int key;
    private final String path;

    public RemoveMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int key, String value, String originalSenderAddress, int originalSenderPort) {
        super(MessageType.REMOVE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, key + ":" + value);
        this.originalSenderAddress = originalSenderAddress;
        this.originalSenderPort = originalSenderPort;
        this.key = key;
        this.path = value;
    }

}
