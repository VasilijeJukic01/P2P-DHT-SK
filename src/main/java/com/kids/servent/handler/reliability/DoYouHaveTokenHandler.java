package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.DoYouHaveTokenMessage;
import com.kids.servent.message.reliability.HaveTokenMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DoYouHaveTokenHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.DO_YOU_HAVE_TOKEN) {
            DoYouHaveTokenMessage queryMsg = (DoYouHaveTokenMessage) clientMessage;
            boolean iHaveToken = AppConfig.chordState.getMutex().hasToken();

            HaveTokenMessage responseMsg = new HaveTokenMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    queryMsg.getReplyToIp(), queryMsg.getReplyToPort(),
                    iHaveToken, queryMsg.getQueryId()
            );
            MessageUtil.sendMessage(responseMsg);
        } else {
            AppConfig.timestampedErrorPrint("DoYouHaveTokenQueryHandler received non-query message: " + clientMessage.getMessageType());
        }
    }
}
