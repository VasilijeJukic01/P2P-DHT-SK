package com.kids.servent.handler.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.NewNodeMessage;
import com.kids.servent.message.core.SorryMessage;
import com.kids.servent.message.core.WelcomeMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NewNodeHandler implements MessageHandler {

	private final Message clientMessage;
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			String newNodeIp = clientMessage.getSenderIpAddress();
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo(newNodeIp, newNodePort);
			
			// Check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(
						AppConfig.myServentInfo.getIpAddress(),
						AppConfig.myServentInfo.getListenerPort(),
						clientMessage.getSenderIpAddress(),
						clientMessage.getSenderPort()
				);
				MessageUtil.sendMessage(sry);
				return;
			}
			
			// Check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { // if yes, prepare and send welcome message
				ServentInfo hisPred = AppConfig.chordState.getPredecessorInfo();
				if (hisPred == null) hisPred = AppConfig.myServentInfo;
				
				AppConfig.chordState.setPredecessorInfo(newNodeInfo);
				
				Map<Integer, Integer> myValues = AppConfig.chordState.getValueMap();
				Map<Integer, Integer> hisValues = new HashMap<>();
				
				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();
				
				for (Entry<Integer, Integer> valueEntry : myValues.entrySet()) {
					if (hisPredId == myId) { // I am first and he is second
						if (myId < newNodeId) {
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { // My old predecessor was before me
						if (valueEntry.getKey() <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { // My old predecessor was after me
						if (hisPredId > newNodeId) { // New node overflow
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { // No new node overflow
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
						
					}

				}
				for (Integer key : hisValues.keySet()) { // Remove his values from my map
					myValues.remove(key);
				}
				AppConfig.chordState.setValueMap(myValues);

				WelcomeMessage wm = new WelcomeMessage(
						AppConfig.myServentInfo.getIpAddress(),
						AppConfig.myServentInfo.getListenerPort(),
						newNodeIp,
						newNodePort,
						hisValues
				);

				MessageUtil.sendMessage(wm);
			}
			// If he is not my predecessor, let someone else take care of it
			else {
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(
						newNodeIp,
						newNodePort,
						nextNode.getIpAddress(),
						nextNode.getListenerPort()
				);
				MessageUtil.sendMessage(nnm);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}
	}

}
