package com.kids.servent.handler.core;

import com.kids.app.AppConfig;
import com.kids.app.bootstrap.client.ServentBroadcastManager;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.file.FileData;
import com.kids.mutex.SuzukiKasamiMutex;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.core.UpdateMessage;
import com.kids.servent.message.util.MessageParser;
import com.kids.servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateHandler implements MessageHandler {

	private final Message clientMessage;

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.UPDATE) {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			return;
		}

		UpdateMessage updateMessage = (UpdateMessage) clientMessage;
		boolean isFromCurrentNode = updateMessage.getSenderIpAddress().equals(AppConfig.myServentInfo.getIpAddress()) && updateMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort();

		if (isFromCurrentNode) handleSelfUpdate(updateMessage);
		else forwardUpdate(updateMessage);
	}

	private void handleSelfUpdate(UpdateMessage message) {
		MessageParser messageData = MessageParser.fromString(message.getNodeInformation());

		List<ServentInfo> allNodes = convertToServentInfo(messageData.getNodes());
		AppConfig.chordState.addNodes(allNodes);

		ServentBroadcastManager.updateBroadcastNodes(messageData.getNodes());

		updateRequestNumbers(messageData.getRequestNumbers());
		saveMyData(message.getData());

		// Release mutex
		AppConfig.chordState.getMutex().unlock();
	}

	private void forwardUpdate(UpdateMessage message) {
		// Add new node to local state
		ServentInfo newNodeInfo = new ServentInfo(message.getSenderIpAddress(), message.getSenderPort());
		List<ServentInfo> newNodes = List.of(newNodeInfo);
		AppConfig.chordState.addNodes(newNodes);

		// Update broadcast nodes
		ServentIdentity newNodeIdentity = new ServentIdentity(newNodeInfo.getIpAddress(), newNodeInfo.getListenerPort());
		ServentBroadcastManager.updateBroadcastNodes(List.of(newNodeIdentity));

		MessageParser messageData = MessageParser.fromString(message.getNodeInformation());

		// Merge data
		Map<Integer, Map<String, FileData>> updatedData = mergeData(message.getData());

		// Create updated message with current node information
		SuzukiKasamiMutex mutex = (SuzukiKasamiMutex) AppConfig.chordState.getMutex();
		List<Integer> updatedRns = new ArrayList<>();

		// Calculate max RNs
		for (int i = 0; i < messageData.getRequestNumbers().size(); i++) {
			int localRN = mutex.getRN().get(i);
			int receivedRN = messageData.getRequestNumbers().get(i);
			updatedRns.add(Math.max(localRN, receivedRN));
		}

		// Forward updated message
		ServentIdentity currentNode = new ServentIdentity(
				AppConfig.myServentInfo.getIpAddress(),
				AppConfig.myServentInfo.getListenerPort()
		);

		String updatedMessageText = messageData.serialize(currentNode, updatedRns);

		Message nextUpdate = new UpdateMessage(
				message.getSenderIpAddress(),
				message.getSenderPort(),
				AppConfig.chordState.getNextNodeIp(),
				AppConfig.chordState.getNextNodePort(),
				updatedData,
				updatedMessageText
		);

		MessageUtil.sendMessage(nextUpdate);
	}

	private List<ServentInfo> convertToServentInfo(List<ServentIdentity> nodes) {
		List<ServentInfo> result = new ArrayList<>();
		for (ServentIdentity node : nodes) {
			result.add(new ServentInfo(node.ip(), node.port()));
		}
		return result;
	}

	private void updateRequestNumbers(List<Integer> receivedRNs) {
		SuzukiKasamiMutex mutex = (SuzukiKasamiMutex) AppConfig.chordState.getMutex();
		for (int i = 0; i < receivedRNs.size(); i++) {
			int localRN = mutex.getRN().get(i);
			int receivedRN = receivedRNs.get(i);
			mutex.getRN().set(i, Math.max(localRN, receivedRN));
		}
	}

	private void saveMyData(Map<Integer, Map<String, FileData>> data) {
		for (Map.Entry<Integer, Map<String, FileData>> entry : data.entrySet()) {
			if (AppConfig.chordState.isKeyMine(entry.getKey())) {
				AppConfig.chordState.getData().put(entry.getKey(), entry.getValue());
			}
		}
	}

	private Map<Integer, Map<String, FileData>> mergeData(Map<Integer, Map<String, FileData>> files) {
		Map<Integer, Map<String, FileData>> localData = AppConfig.chordState.getData();

		for (Map.Entry<Integer, Map<String, FileData>> entry : files.entrySet()) {
			if (localData.containsKey(entry.getKey())) localData.get(entry.getKey()).putAll(entry.getValue());
			else localData.put(entry.getKey(), entry.getValue());
		}

		return localData;
	}
}