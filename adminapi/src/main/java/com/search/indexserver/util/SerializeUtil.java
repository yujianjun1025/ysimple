package com.search.indexserver.util;

import com.google.common.collect.Maps;
import com.search.indexserver.pojo.TermInOneDoc;
import com.search.indexserver.pojo.TermInfo;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * Created by yjj on 15/12/14.
 */
public class SerializeUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    public static byte[] serializeMapByJava(Object object) {

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {


            ObjectOutputStream oo = new ObjectOutputStream(byteOutput);
            oo.writeObject(object);
            bytes = byteOutput.toByteArray();

            byteOutput.close();
            oo.close();

        } catch (Exception e) {
            logger.error("序列化时出现异常", e);
        }

        return bytes;

    }

    public static Object deserializeByJava(byte[] bytes) {

        Object obj = null;
        try {
            ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(byteInput);
            obj = oi.readObject();
            byteInput.close();
            oi.close();
        } catch (Exception e) {
            logger.error("反序列化时出现异常", e);
        }
        return obj;
    }


    public static byte[] serializeBySelf(List<TermInOneDoc> termInOneDocList) {

        TermInfo termInfo = new TermInfo(termInOneDocList);
        return termInfo.toBytes();
    }

    public static List<TermInOneDoc> deserializeBySelf(byte[] bytes) {
        return TermInfo.byte2Object(bytes);
    }


    public static void main(String[] args) {
        Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("b", 1);
        map.put("c", 1);
        map.put("d", 1);

        byte[] bytes = serializeMapByJava(map);

        Map<String, Integer> map1 = (Map<String, Integer>) deserializeByJava(bytes);


        System.out.println(map1.toString());



    }

}
