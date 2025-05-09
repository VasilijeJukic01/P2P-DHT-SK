package com.kids.servent.handler.core;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SorryHandler implements MessageHandler {

	private Message clientMessage;
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.SORRY) {
			AppConfig.timestampedStandardPrint("Couldn't enter Chord system because of collision. Change my listener port, please.");
			System.exit(0);
		} else {
			AppConfig.timestampedErrorPrint("Sorry handler got a message that is not SORRY");
		}

	}

}
