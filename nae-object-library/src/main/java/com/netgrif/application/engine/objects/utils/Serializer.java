package com.netgrif.application.engine.objects.utils;

import java.io.*;

public final class Serializer {

    /**
     * Method deserializes bytes into object
     *
     * @param bytes Bytes to be deserialized
     *
     * @return Deserialized object
     * */
    public static Object deserialize(byte[] bytes)  {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
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
