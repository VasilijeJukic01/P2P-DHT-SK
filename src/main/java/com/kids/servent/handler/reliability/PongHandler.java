package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.app.reliability.ServentActivityTracker;
import com.kids.app.reliability.ServentState;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PongHandler implements MessageHandler {

    private Message clientMessage;

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.PONG) {
                ServentActivityTracker tracker = AppConfig.chordState.getTracker();
                tracker.setNotify(false);
                tracker.resetTimestamp();
                tracker.setStatus(ServentState.ALIVE);
            }
            else {
                AppConfig.timestampedErrorPrint("Pong handler got a message that is not PONG");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
