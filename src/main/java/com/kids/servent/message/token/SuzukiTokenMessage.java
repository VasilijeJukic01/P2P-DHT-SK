package com.kids.servent.message.token;

import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class SuzukiTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8981406250652693901L;
    private final SuzukiKasamiToken token;

    public SuzukiTokenMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, SuzukiKasamiToken token) {
        super(MessageType.SUZUKI_TOKEN, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.token = token;
    }

}
