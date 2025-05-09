package com.kids.servent.handler.core;

import com.kids.app.AppConfig;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import lombok.AllArgsConstructor;

/**
 * This will be used if no proper handler is found for the message.
 * @author bmilojkovic
 *
 */
@AllArgsConstructor
public class NullHandler implements MessageHandler {

	private final Message clientMessage;
	
	@Override
	public void run() {
		AppConfig.timestampedErrorPrint("Couldn't handle message: " + clientMessage);
	}

}
