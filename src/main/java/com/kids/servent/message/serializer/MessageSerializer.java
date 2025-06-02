package com.kids.servent.message.serializer;

import com.kids.servent.message.Message;
import java.io.IOException;

public interface MessageSerializer {
    byte getSerializationType();
    byte[] serialize(Message message) throws IOException;
}
