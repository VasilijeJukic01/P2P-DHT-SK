package com.kids.servent.message.system;

import com.kids.file.FileData;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.BasicMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ReplicateMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 3439102837491028374L;
    private final FileData fileData;

    public ReplicateMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileData fileData) {
        super(MessageType.REPLICATE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, fileData.path());

        this.fileData = fileData;
    }

}
