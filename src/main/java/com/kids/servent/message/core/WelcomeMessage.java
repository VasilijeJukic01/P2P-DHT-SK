package com.kids.servent.message.core;

import com.kids.file.FileData;
import com.kids.servent.message.MessageType;
import lombok.Getter;

import java.io.Serial;
import java.util.Map;

@Getter
public class WelcomeMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8981406250652693908L;
	private final Map<Integer, Map<String, FileData>> data;

	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<Integer, Map<String, FileData>> data) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.data = data;
	}
}
