package com.kids.servent.message.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.kids.app.AppConfig;
import lombok.AllArgsConstructor;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main.
 * 
 * @author bmilojkovic
 *
 */
@AllArgsConstructor
public class DelayedMessageSender implements Runnable {

	private final byte serializationType;
	private final byte[] payload;
	private final String receiverIpAddress;
	private final int receiverPort;
	private final String originalMessageToString;

	public void run() {
		try {
			Thread.sleep((long)(Math.random() * 1000) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			AppConfig.timestampedStandardPrint("Sending message " + originalMessageToString);
		}

		try (Socket sendSocket = new Socket(receiverIpAddress, receiverPort); OutputStream os = sendSocket.getOutputStream()) {
			os.write(serializationType);
			os.write(payload);
			os.flush();
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't send message: " + originalMessageToString + " to " + receiverIpAddress + ":" + receiverPort);
		}
	}
}
