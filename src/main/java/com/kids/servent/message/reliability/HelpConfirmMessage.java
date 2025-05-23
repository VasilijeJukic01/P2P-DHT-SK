package com.kids.servent.message.reliability;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class HelpConfirmMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 5432109876543210L;
    private final String suspiciousNodeIp;
    private final int suspiciousNodePort;
    private final boolean isAlive;

    public HelpConfirmMessage(String senderIp, int senderPort, String receiverIp, int receiverPort, String suspiciousNodeIp, int suspiciousNodePort, boolean isAlive) {
        super(MessageType.HELP_CONFIRM, senderIp, senderPort, receiverIp, receiverPort, suspiciousNodeIp + ":" + suspiciousNodePort + ":" + isAlive);
        this.suspiciousNodeIp = suspiciousNodeIp;
        this.suspiciousNodePort = suspiciousNodePort;
        this.isAlive = isAlive;
    }
}
