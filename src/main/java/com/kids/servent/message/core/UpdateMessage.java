package com.kids.servent.message.core;

import com.kids.file.FileData;
import com.kids.servent.message.MessageType;
import lombok.Getter;

import java.io.Serial;
import java.util.Map;

@Getter
public class UpdateMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = 3586102505319194978L;
	private final Map<Integer, Map<String, FileData>> data;
	private final String nodeInformation;

	public UpdateMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<Integer, Map<String, FileData>> data, String nodeInformation) {
		super(MessageType.UPDATE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, "");

		this.nodeInformation = nodeInformation;
		this.data = data;
	}
}
