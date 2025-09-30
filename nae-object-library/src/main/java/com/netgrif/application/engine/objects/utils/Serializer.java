package com.netgrif.application.engine.objects.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public final class Serializer {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    /**
     * Method deserializes bytes into object
     *
     * @param bytes Bytes to be deserialized
     *
     * @return Deserialized object
     * */
    public static Object deserialize(byte[] bytes)  {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to deserialize object", e);
            return null;
        }
    }

    /**
     * Method serializes object into byte array
     *
     * @param obj Object to be serialized
     *
     * @return Serialized byte array
     * */
    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
