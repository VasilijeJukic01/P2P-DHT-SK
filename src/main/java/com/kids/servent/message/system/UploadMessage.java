package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class UploadMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8439102837491028374L;
    private final int key;
    private final String requesterIpAddress;
    private final int requesterPort;
    private final String path;

    public UploadMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String requesterIpAddress, int requesterPort, int key, String path) {
        super(MessageType.UPLOAD, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.key = key;
        this.path = path;
    }

}
