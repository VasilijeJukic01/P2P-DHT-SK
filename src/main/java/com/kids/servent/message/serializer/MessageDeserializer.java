package com.kids.servent.message.serializer;

import com.kids.servent.message.Message;

import java.io.IOException;
import java.io.InputStream;

public interface MessageDeserializer {
    Message deserialize(InputStream inputStream) throws IOException, ClassNotFoundException;
}
