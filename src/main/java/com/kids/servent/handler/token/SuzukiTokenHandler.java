package com.kids.servent.handler.token;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.token.SuzukiTokenMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SuzukiTokenHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SUZUKI_TOKEN) {
            try {
                SuzukiTokenMessage suzukiTokenMessage = (SuzukiTokenMessage) clientMessage;
                
                if (suzukiTokenMessage.getToken() == null) {
                    AppConfig.timestampedErrorPrint("Received token message with null token");
                    return;
                }

                AppConfig.timestampedStandardPrint("Received token from " + suzukiTokenMessage.getSenderIpAddress() + ":" + suzukiTokenMessage.getSenderPort());
                        
                AppConfig.chordState.getMutex().setToken(suzukiTokenMessage.getToken());
            } catch (Exception e) {
                AppConfig.timestampedErrorPrint("Error processing token message: " + e.getMessage());
            }
        } else {
            AppConfig.timestampedErrorPrint("Got to token handler but message is not SUZUKI_TOKEN");
        }
    }

}
