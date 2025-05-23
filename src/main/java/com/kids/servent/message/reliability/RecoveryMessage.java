package com.kids.servent.message.reliability;

import com.kids.app.servent.ServentInfo;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class RecoveryMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 3119833286642327636L;
    private final ServentInfo removing;

    public RecoveryMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, ServentInfo removeInfo) {
        super(MessageType.RECOVERY, senderIpAddress, senderPort, receiverIpAddress, receiverPort, "");
        this.removing = removeInfo;
    }

}
