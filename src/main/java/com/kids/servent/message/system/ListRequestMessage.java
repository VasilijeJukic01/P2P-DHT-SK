package com.kids.servent.message.system;

import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ListRequestMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8329502137496028371L;
    private final int key;

    public ListRequestMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int key) {
        super(MessageType.LIST_IMAGES_REQUEST, senderIpAddress, senderPort, receiverIpAddress, receiverPort, key + "");
        this.key = key;
    }

}
