package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.PongMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PingHandler  implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.PING) {

                Message pongMessage = new PongMessage(
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getListenerPort(),
                        clientMessage.getSenderIpAddress(),
                        clientMessage.getSenderPort()
                );

                MessageUtil.sendMessage(pongMessage);
            }
            else {
                AppConfig.timestampedErrorPrint("PingHandler got a message that is not PING");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
