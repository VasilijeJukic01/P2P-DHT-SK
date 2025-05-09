package com.kids.servent.handler.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.UpdateMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateHandler implements MessageHandler {

	private final Message clientMessage;
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
				ServentInfo newNodInfo = new ServentInfo("localhost", clientMessage.getSenderPort());
				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodInfo);
				
				AppConfig.chordState.addNodes(newNodes);
				String newMessageText;

				if (clientMessage.getMessageText().isEmpty()) {
					newMessageText = String.valueOf(AppConfig.myServentInfo.getListenerPort());
				} else {
					newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getListenerPort();
				}

				Message nextUpdate = new UpdateMessage(
						clientMessage.getSenderIpAddress(),
						clientMessage.getSenderPort(),
						AppConfig.chordState.getNextNodeIp(),
						AppConfig.chordState.getNextNodePort(),
						newMessageText
				);

				MessageUtil.sendMessage(nextUpdate);
			} else {
				String messageText = clientMessage.getMessageText();
				String[] ports = messageText.split(",");
				
				List<ServentInfo> allNodes = new ArrayList<>();

				Arrays.stream(ports)
						.forEach(port->{
							allNodes.add(new ServentInfo(port, AppConfig.chordState.getNextNodePort()));
						});

				for (String port : ports) {
					allNodes.add(new ServentInfo("localhost", Integer.parseInt(port)));
				}
				AppConfig.chordState.addNodes(allNodes);
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

}
