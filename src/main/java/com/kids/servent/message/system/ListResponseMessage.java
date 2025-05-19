package com.kids.servent.message.system;

import com.kids.file.FileData;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Getter
public class ListResponseMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3439102837491428374L;
    private final List<FileData> fileData;

    public ListResponseMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int key, List<FileData> fileData) {
        super(MessageType.LIST_IMAGES_RESPONSE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, key + ":" + fileData);
        this.fileData = fileData;
    }

}
