package com.kids.servent.message.core;

import com.kids.servent.message.MessageType;

import java.io.Serial;

public class PoisonMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -5625132784318034900L;

	public PoisonMessage() {
		super(MessageType.POISON, "", 0, "", 0);
	}

}
