package com.kids.servent.message.serializer;

import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.util.MessageUtil;

public class SerializerFactory {

    public static MessageSerializer getSerializer(Message message) {
        if (message.getMessageType() == MessageType.SUZUKI_TOKEN_REQUEST || message.getMessageType() == MessageType.SUZUKI_TOKEN) {
            return new AvroSerializer();
        }
        return new JavaSerializer();
    }

    public static MessageDeserializer getDeserializer(byte serializationType) {
        if (serializationType == MessageUtil.AVRO_SERIALIZED) {
            return new AvroSerializer();
        }
        return new JavaSerializer();
    }

}
