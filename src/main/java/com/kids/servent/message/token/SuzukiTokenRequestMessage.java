package com.kids.servent.message.token;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class SuzukiTokenRequestMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8981406250652693911L;
    private final int senderRN;

    public SuzukiTokenRequestMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int senderRN) {
        super(MessageType.SUZUKI_TOKEN_REQUEST, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.senderRN = senderRN;
    }

}
