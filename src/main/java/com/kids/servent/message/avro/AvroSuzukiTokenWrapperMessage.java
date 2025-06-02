package com.kids.servent.message.avro;

import com.kids.avro.SuzukiKasamiTokenPayloadAvro;
import com.kids.avro.SuzukiTokenMessageAvro;
import com.kids.servent.message.MessageType;
import lombok.Getter;

import java.io.Serial;

@Getter
public class AvroSuzukiTokenWrapperMessage extends AvroWrapperMessage {

    @Serial
    private static final long serialVersionUID = 3287642978546283094L;

    private final SuzukiTokenMessageAvro avroPayload;

    public AvroSuzukiTokenWrapperMessage(SuzukiTokenMessageAvro avroPayload) {
        super(MessageType.SUZUKI_TOKEN,
                avroPayload.getSenderIp(),
                avroPayload.getSenderPort(),
                avroPayload.getReceiverIp(),
                avroPayload.getReceiverPort(),
                avroPayload.getMessageId()
        );
        this.avroPayload = avroPayload;
    }

    public SuzukiKasamiTokenPayloadAvro getTokenPayload() {
        return avroPayload.getToken();
    }
}
