package com.kids.servent.handler.token;

import com.kids.app.AppConfig;
import com.kids.app.ChordState;
import com.kids.mutex.SuzukiKasamiMutex;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.avro.AvroSKRequestWrapperMessage;
import com.kids.servent.message.token.SuzukiTokenRequestMessage;
import com.kids.app.servent.ServentIdentity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SuzukiTokenRequestHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.SUZUKI_TOKEN_REQUEST) {
                SuzukiKasamiMutex suzukiKasamiMutex = (SuzukiKasamiMutex) AppConfig.chordState.getMutex();

                int senderChordId = ChordState.chordHash(clientMessage.getSenderIpAddress() + ":" + clientMessage.getSenderPort());
                int senderRN;

                if (clientMessage instanceof AvroSKRequestWrapperMessage wrapper) {
                    senderRN = wrapper.getSenderRN();
                }
                else if (clientMessage instanceof SuzukiTokenRequestMessage legacyMsg) {
                    senderRN = legacyMsg.getSenderRN();
                }
                else {
                    AppConfig.timestampedErrorPrint("Unexpected message type in SuzukiTokenRequestHandler: " + clientMessage.getClass().getName());
                    return;
                }

                if (suzukiKasamiMutex.getRN().get(senderChordId) < senderRN) {
                    suzukiKasamiMutex.getRN().set(senderChordId, senderRN);

                    // Have token and not in CS -> Send token
                    if (suzukiKasamiMutex.hasToken() && suzukiKasamiMutex.getToken().getLN().get(senderChordId) + 1 == senderRN) {
                        ServentIdentity senderServentIdentity = new ServentIdentity(clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
                        suzukiKasamiMutex.getToken().getQueue().add(senderServentIdentity);

                        // Send token if not in CS
                        if (!suzukiKasamiMutex.usesToken()) suzukiKasamiMutex.tryToSendToken();
                    }
                }
            } else {
                AppConfig.timestampedErrorPrint("Got to token request handler but message is not SUZUKI_TOKEN_REQUEST");
            }
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error in token request handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}