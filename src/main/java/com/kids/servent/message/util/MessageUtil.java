package com.kids.servent.message.util;

import com.kids.app.AppConfig;
import com.kids.servent.message.Message;
import com.kids.servent.message.serializer.MessageDeserializer;
import com.kids.servent.message.serializer.MessageSerializer;
import com.kids.servent.message.serializer.SerializerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MessageUtil {

	public static final boolean MESSAGE_UTIL_PRINTING = true;
	public static final byte JAVA_SERIALIZED = 0x01;
	public static final byte AVRO_SERIALIZED = 0x02;

	public static Message readMessage(Socket socket) {
		Message clientMessage = null;
        try (socket; InputStream is = socket.getInputStream()) {
            int serializationType = is.read();

            MessageDeserializer deserializer = SerializerFactory.getDeserializer((byte) serializationType);
            clientMessage = deserializer.deserialize(is);

            if (MESSAGE_UTIL_PRINTING && clientMessage != null) {
                AppConfig.timestampedStandardPrint("Got message " + clientMessage);
            }
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Error processing received message: " + e.getMessage());
            e.printStackTrace();
        }
		return clientMessage;
	}

	public static void sendMessage(Message message) {
		try {
			MessageSerializer serializer = SerializerFactory.getSerializer(message);
			byte serializationType = serializer.getSerializationType();
			byte[] payload = serializer.serialize(message);
			String originalToString = message.toString();

			Thread delayedSender = new Thread(new DelayedMessageSender(
					serializationType, payload,
					message.getReceiverIpAddress(), message.getReceiverPort(),
					originalToString));
			delayedSender.start();

		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Error serializing message " + message + ": " + e.getMessage());
		}
	}
}
