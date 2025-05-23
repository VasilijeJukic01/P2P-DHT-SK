package com.kids.servent.message.reliability;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class DoYouHaveTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID  = 1234567222012456789L;
    private final String replyToIp;
    private final int replyToPort;
    private final int queryId;

    public DoYouHaveTokenMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String replyToIp, int replyToPort, int queryId) {
        super(MessageType.DO_YOU_HAVE_TOKEN, senderIp, senderPort, receiverIp, receiverPort);
        this.replyToIp = replyToIp;
        this.replyToPort = replyToPort;
        this.queryId = queryId;
    }
}
