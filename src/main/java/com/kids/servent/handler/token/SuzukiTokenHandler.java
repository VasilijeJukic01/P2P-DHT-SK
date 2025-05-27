package com.kids.servent.handler.token;

import com.kids.app.AppConfig;
import com.kids.avro.SuzukiKasamiTokenPayloadAvro;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.avro.AvroSuzukiTokenWrapperMessage;
import com.kids.servent.message.token.SuzukiTokenMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SuzukiTokenHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SUZUKI_TOKEN) {
            try {
                AppConfig.timestampedStandardPrint("Received token from " + clientMessage.getSenderIpAddress() + ":" + clientMessage.getSenderPort());

                SuzukiKasamiToken tokenToSet;

                if (clientMessage instanceof AvroSuzukiTokenWrapperMessage wrapper) {
                    SuzukiKasamiTokenPayloadAvro avroPayload = wrapper.getTokenPayload();
                    if (avroPayload == null) {
                        AppConfig.timestampedErrorPrint("Received Avro token message with null token payload");
                        return;
                    }
                    // Avro -> Java
                    tokenToSet = new SuzukiKasamiToken(avroPayload);
                }
                else if (clientMessage instanceof SuzukiTokenMessage legacyMsg) {
                    // This case should ideally not happen
                    if (legacyMsg.getToken() == null) return;
                    tokenToSet = legacyMsg.getToken();
                }
                else {
                    AppConfig.timestampedErrorPrint("Unexpected message type in SuzukiTokenHandler: " + clientMessage.getClass().getName());
                    return;
                }
                AppConfig.chordState.getMutex().setToken(tokenToSet);
            } catch (Exception e) {
                AppConfig.timestampedErrorPrint("Error processing token message: " + e.getMessage());
            }
        } else {
            AppConfig.timestampedErrorPrint("Got to token handler but message is not SUZUKI_TOKEN");
        }
    }

}
