package com.kids.servent.message.reliability;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class HelpRequestMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 1234567890123456L;
    private final String suspiciousNodeIp;
    private final int suspiciousNodePort;
    private final String originatorIp;
    private final int originatorPort;

    public HelpRequestMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String suspiciousNodeIp, int suspiciousNodePort, String originatorIp, int originatorPort) {
        super(MessageType.HELP_REQUEST, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.suspiciousNodeIp = suspiciousNodeIp;
        this.suspiciousNodePort = suspiciousNodePort;
        this.originatorIp = originatorIp;
        this.originatorPort = originatorPort;
    }
}
