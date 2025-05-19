package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;

import java.io.Serial;

public class UploadAckMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8429502837491028371L;

    public UploadAckMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.UPLOAD_ACK, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }
}
