package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.app.reliability.ServentPulseManager;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.HaveTokenMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HaveTokenHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HAVE_TOKEN) {
            HaveTokenMessage responseMsg = (HaveTokenMessage) clientMessage;

            if (responseMsg.isHasToken()) {
                ServentPulseManager.globalTokenFoundElsewhere = true;
                AppConfig.timestampedStandardPrint("Node " + responseMsg.getSenderIpAddress() + ":" + responseMsg.getSenderPort() + " reported it HAS the token.");
            }
            else {
                AppConfig.timestampedStandardPrint("Node " + responseMsg.getSenderIpAddress() + ":" + responseMsg.getSenderPort() + " reported it DOES NOT have the token.");
            }
            ServentPulseManager.tokenQueryResponsesCount.incrementAndGet();
        } else {
            AppConfig.timestampedErrorPrint("HaveTokenResponseHandler received non-response message: " + clientMessage.getMessageType());
        }
    }
}
