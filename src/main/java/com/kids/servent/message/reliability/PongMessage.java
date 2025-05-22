package com.kids.servent.message.reliability;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;

import java.io.Serial;

public class PongMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 2899833286642127636L;

    public PongMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.PONG, senderIpAddress, senderPort, receiverIpAddress, receiverPort, "pong");
    }

}
