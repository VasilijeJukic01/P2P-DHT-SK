package com.kids.servent.handler.core;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.UpdateMessage;
import com.kids.servent.message.core.WelcomeMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WelcomeHandler implements MessageHandler {

	private final Message clientMessage;
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.WELCOME) {
			WelcomeMessage welcomeMsg = (WelcomeMessage)clientMessage;
			
			AppConfig.chordState.init(welcomeMsg);

			UpdateMessage um = new UpdateMessage(
					AppConfig.myServentInfo.getIpAddress(),
					AppConfig.myServentInfo.getListenerPort(),
					AppConfig.chordState.getNextNodeIp(),
					AppConfig.chordState.getNextNodePort(),
					""
			);
			MessageUtil.sendMessage(um);
		} else {
			AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
		}
	}

}
