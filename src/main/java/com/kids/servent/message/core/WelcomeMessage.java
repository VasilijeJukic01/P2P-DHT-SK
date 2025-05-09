package com.kids.servent.message.core;

import com.kids.servent.message.MessageType;
import lombok.Getter;

import java.io.Serial;
import java.util.Map;

@Getter
public class WelcomeMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8981406250652693908L;
	private final Map<Integer, Integer> values;

	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<Integer, Integer> values) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		this.values = values;
	}
}
