package com.kids.servent.message.avro;

import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public abstract class AvroWrapperMessage implements Message {

    private final MessageType type;
    private final String senderIpAddress;
    private final int senderPort;
    private final String receiverIpAddress;
    private final int receiverPort;
    private final int messageId;

    @Override
    public MessageType getMessageType() { return type; }

    @Override
    public String getSenderIpAddress() { return senderIpAddress; }

    @Override
    public int getSenderPort() { return senderPort; }

    @Override
    public String getReceiverIpAddress() { return receiverIpAddress; }

    @Override
    public int getReceiverPort() { return receiverPort; }

    @Override
    public int getMessageId() { return messageId; }

    @Override
    public String getMessageText() { return "[AVRO_PAYLOAD_TYPE:" + type + "]"; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Message other)) return false;
        return getMessageId() == other.getMessageId() && getSenderPort() == other.getSenderPort();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessageId(), getSenderPort());
    }

    @Override
    public String toString() {
        return "[AVRO] " + getSenderIpAddress() + ":" + getSenderPort() + " -> " +
                getReceiverIpAddress() + ":" + getReceiverPort() +
                " ID: " + getMessageId() + " Type: " + getMessageType();
    }
}
