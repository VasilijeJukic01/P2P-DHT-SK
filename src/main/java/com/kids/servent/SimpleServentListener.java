package com.kids.servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.kids.app.AppConfig;
import com.kids.app.Cancellable;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.handler.core.NewNodeHandler;
import com.kids.servent.handler.core.NullHandler;
import com.kids.servent.handler.core.SorryHandler;
import com.kids.servent.handler.core.UpdateHandler;
import com.kids.servent.handler.core.WelcomeHandler;
import com.kids.servent.handler.system.*;
import com.kids.servent.handler.token.SuzukiTokenHandler;
import com.kids.servent.handler.token.SuzukiTokenRequestHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.util.MessageUtil;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;

	/*
	 * Thread pool for executing the handlers. Each client will get its own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			System.exit(0);
		}

		while (working) {
			try {
				Message clientMessage;
				Socket clientSocket = listenerSocket.accept();

				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);

				switch (clientMessage.getMessageType()) {
				case NEW_NODE:
					messageHandler = new NewNodeHandler(clientMessage);
					break;
				case WELCOME:
					messageHandler = new WelcomeHandler(clientMessage);
					break;
				case SORRY:
					messageHandler = new SorryHandler(clientMessage);
					break;
				case UPDATE:
					messageHandler = new UpdateHandler(clientMessage);
					break;
				case SUZUKI_TOKEN_REQUEST:
					messageHandler = new SuzukiTokenRequestHandler(clientMessage);
					break;
				case SUZUKI_TOKEN:
					messageHandler = new SuzukiTokenHandler(clientMessage);
					break;
				case UPLOAD:
					messageHandler = new UploadHandler(clientMessage);
					break;
				case UPLOAD_ACK:
					messageHandler = new UploadAckHandler(clientMessage);
					break;
				case REPLICATE:
					messageHandler = new ReplicateHandler(clientMessage);
					break;
				case LIST_IMAGES_REQUEST:
					messageHandler = new ListRequestHandler(clientMessage);
					break;
					case LIST_IMAGES_RESPONSE:
					messageHandler = new ListResponseHandler(clientMessage);
					break;
				case POISON:
					break;
				}
				
				threadPool.submit(messageHandler);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
