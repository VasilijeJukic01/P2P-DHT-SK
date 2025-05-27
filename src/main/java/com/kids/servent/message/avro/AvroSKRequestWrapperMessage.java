package com.kids.servent.message.avro;

import com.kids.avro.SuzukiTokenRequestMessageAvro;
import com.kids.servent.message.MessageType;

import java.io.Serial;

public class AvroSKRequestWrapperMessage extends AvroWrapperMessage {

    @Serial
    private static final long serialVersionUID = -8546328907641203754L;

    private final SuzukiTokenRequestMessageAvro avroPayload;

    public AvroSKRequestWrapperMessage(SuzukiTokenRequestMessageAvro avroPayload) {
        super(MessageType.SUZUKI_TOKEN_REQUEST,
                avroPayload.getSenderIp(),
                avroPayload.getSenderPort(),
                avroPayload.getReceiverIp(),
                avroPayload.getReceiverPort(),
                avroPayload.getMessageId()
        );
        this.avroPayload = avroPayload;
    }

    public int getSenderRN() {
        return avroPayload.getSenderRN();
    }
}

