package com.kids.servent.message.reliability;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class HaveTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 1234511190123111789L;
    private final boolean hasToken;
    private final int queryId;

    public HaveTokenMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, boolean hasToken, int queryId) {
        super(MessageType.HAVE_TOKEN, senderIp, senderPort, receiverIp, receiverPort, String.valueOf(hasToken));
        this.hasToken = hasToken;
        this.queryId = queryId;
    }
}
