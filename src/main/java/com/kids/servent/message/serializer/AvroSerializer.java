package com.kids.servent.message.serializer;

import com.kids.avro.SuzukiKasamiTokenPayloadAvro;
import com.kids.avro.SuzukiTokenMessageAvro;
import com.kids.avro.SuzukiTokenRequestMessageAvro;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.avro.AvroSKRequestWrapperMessage;
import com.kids.servent.message.avro.AvroSuzukiTokenWrapperMessage;
import com.kids.servent.message.token.SuzukiTokenMessage;
import com.kids.servent.message.token.SuzukiTokenRequestMessage;
import com.kids.servent.message.util.MessageUtil;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AvroSerializer implements MessageSerializer, MessageDeserializer {

    @Override
    public byte getSerializationType() {
        return MessageUtil.AVRO_SERIALIZED;
    }

    @Override
    public byte[] serialize(Message message) throws IOException {
        if (message.getMessageType() == MessageType.SUZUKI_TOKEN_REQUEST) {
            return serializeSuzukiTokenRequest((SuzukiTokenRequestMessage) message);
        }
        else if (message.getMessageType() == MessageType.SUZUKI_TOKEN) {
            return serializeSuzukiToken((SuzukiTokenMessage) message);
        }
        throw new IllegalArgumentException("Unsupported message type for Avro: " + message.getMessageType());
    }

    private byte[] serializeSuzukiTokenRequest(SuzukiTokenRequestMessage message) throws IOException {
        SuzukiTokenRequestMessageAvro avroMsg = SuzukiTokenRequestMessageAvro.newBuilder()
                .setMessageTypeOrdinal(MessageType.SUZUKI_TOKEN_REQUEST.ordinal())
                .setSenderIp(message.getSenderIpAddress())
                .setSenderPort(message.getSenderPort())
                .setReceiverIp(message.getReceiverIpAddress())
                .setReceiverPort(message.getReceiverPort())
                .setMessageId(message.getMessageId())
                .setSenderRN(message.getSenderRN())
                .build();

        return serializeAvro(avroMsg, MessageType.SUZUKI_TOKEN_REQUEST.ordinal());
    }

    private byte[] serializeSuzukiToken(SuzukiTokenMessage message) throws IOException {
        SuzukiKasamiTokenPayloadAvro tokenPayloadAvro = message.getToken().toAvroPayload();
        SuzukiTokenMessageAvro avroMsg = SuzukiTokenMessageAvro.newBuilder()
                .setMessageTypeOrdinal(MessageType.SUZUKI_TOKEN.ordinal())
                .setSenderIp(message.getSenderIpAddress())
                .setSenderPort(message.getSenderPort())
                .setReceiverIp(message.getReceiverIpAddress())
                .setReceiverPort(message.getReceiverPort())
                .setMessageId(message.getMessageId())
                .setToken(tokenPayloadAvro)
                .build();

        return serializeAvro(avroMsg, MessageType.SUZUKI_TOKEN.ordinal());
    }

    private byte[] serializeAvro(Object avroObject, int messageTypeOrdinal) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(messageTypeOrdinal);

        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        DatumWriter<Object> writer = (DatumWriter<Object>) new SpecificDatumWriter<>(avroObject.getClass());
        writer.write(avroObject, encoder);
        encoder.flush();

        return baos.toByteArray();
    }

    @Override
    public Message deserialize(InputStream inputStream) throws IOException {
        int messageTypeOrdinal = inputStream.read();
        MessageType messageType = MessageType.values()[messageTypeOrdinal];

        byte[] avroPayloadBytes = inputStream.readAllBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(avroPayloadBytes);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bais, null);

        if (messageType == MessageType.SUZUKI_TOKEN_REQUEST) {
            DatumReader<SuzukiTokenRequestMessageAvro> reader = new SpecificDatumReader<>(SuzukiTokenRequestMessageAvro.class);
            SuzukiTokenRequestMessageAvro avroMsg = reader.read(null, decoder);
            return new AvroSKRequestWrapperMessage(avroMsg);
        }
        else if (messageType == MessageType.SUZUKI_TOKEN) {
            DatumReader<SuzukiTokenMessageAvro> reader = new SpecificDatumReader<>(SuzukiTokenMessageAvro.class);
            SuzukiTokenMessageAvro avroMsg = reader.read(null, decoder);
            return new AvroSuzukiTokenWrapperMessage(avroMsg);
        }

        throw new IOException("Unhandled Avro message type: " + messageTypeOrdinal);
    }
}
