package com.kids.servent.message.core;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.kids.app.ChordState;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	@Serial
	private static final long serialVersionUID = -9075856313609777945L;

	private final String senderIpAddress;
	private final String receiverIpAddress;
	private final int senderPort;
	private final int receiverPort;
	private final MessageType type;
	private final String messageText;
	
	// This gives us a unique id - incremented in every natural constructor.
	private static final AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;

	public static int getNextMessageId() {
		return messageCounter.getAndIncrement();
	}

	public BasicMessage(MessageType type, String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
		this.type = type;
		this.senderIpAddress = senderIpAddress;
		this.senderPort = senderPort;
		this.receiverIpAddress = receiverIpAddress;
		this.receiverPort = receiverPort;
		this.messageText = "";

		this.messageId = getNextMessageId();
	}

	public BasicMessage(MessageType type, String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String messageText) {
		this.type = type;
		this.senderIpAddress = senderIpAddress;
		this.senderPort = senderPort;
		this.receiverIpAddress = receiverIpAddress;
		this.receiverPort = receiverPort;
		this.messageText = messageText;

		this.messageId = getNextMessageId();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}
	
	@Override
	public int getReceiverPort() {
		return receiverPort;
	}
	
	@Override
	public String getReceiverIpAddress() {
		return receiverIpAddress;
	}

	@Override
	public String getSenderIpAddress() {
		return senderIpAddress;
	}
	
	@Override
	public int getSenderPort() {
		return senderPort;
	}

	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}
	
	/**
	 * Comparing messages is based on their unique id and the original sender port.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage other) {
            return getMessageId() == other.getMessageId() && getSenderPort() == other.getSenderPort();
		}
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are going to keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getSenderPort());
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|sender_port|message_id|text|type|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		String senderIpAddressPort = getSenderIpAddress() + ":" + getSenderPort();
		String receiverIpAddressPort = getReceiverIpAddress() + ":" + getReceiverPort();

		return "[cHashSp:" + ChordState.chordHash(senderIpAddressPort) + "|sPort:" + senderIpAddressPort + "|mId:" +
				getMessageId() + "|mTxt:" + getMessageText() + "|mTyp:" + getMessageType() + "|rPort:" +
				receiverIpAddressPort + "|cHashRp:" + ChordState.chordHash(receiverIpAddressPort) + "]";

	}
}
