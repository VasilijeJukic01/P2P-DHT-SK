package com.kids.servent.message.core;

import com.kids.servent.message.MessageType;

import java.io.Serial;

public class SorryMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = 8866336621366084210L;

	public SorryMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
		super(MessageType.SORRY, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
	}

}
