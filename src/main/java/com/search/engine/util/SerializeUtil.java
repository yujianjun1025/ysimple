package com.search.engine.util;

import java.io.*;

/**
 * Created by yjj on 15/12/13.
 */
public class SerializeUtil {

    public static byte[] serialize(Object object) throws IOException {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            // 序列化
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } finally {
            if (oos != null) {
                oos.close();
            }

            if (baos != null) {
                baos.close();
            }
        }
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream ois = null;
        try {
            // 反序列化
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(byteArrayInputStream);
            return ois.readObject();
        } finally {
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
    }


}
