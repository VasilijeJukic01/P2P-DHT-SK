package com.kids.servent.message.system;

import com.kids.app.system.Status;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class RemoveAckMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 3133102811491423371L;
    private final String path;
    private final Status status;

    public RemoveAckMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String path, Status status) {
        super(MessageType.REMOVE_ACK, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.path = path;
        this.status = status;
    }

}
