package com.kids.servent.message.serializer;

import com.kids.servent.message.Message;
import com.kids.servent.message.util.MessageUtil;

import java.io.*;

public class JavaSerializer implements MessageSerializer, MessageDeserializer {

    @Override
    public byte getSerializationType() {
        return MessageUtil.JAVA_SERIALIZED;
    }

    @Override
    public byte[] serialize(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        byte[] payload = baos.toByteArray();
        oos.close();

        return payload;
    }

    @Override
    public Message deserialize(InputStream inputStream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            return (Message) ois.readObject();
        }
    }

}
