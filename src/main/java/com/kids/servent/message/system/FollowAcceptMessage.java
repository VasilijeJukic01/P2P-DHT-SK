package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class FollowAcceptMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 4135102818991428376L;

    public FollowAcceptMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.FOLLOW_ACCEPT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }
}