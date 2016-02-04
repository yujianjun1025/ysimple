package com.search.indexserver.util;

import com.search.indexserver.pojo.TermInOneDoc;
import com.search.indexserver.pojo.TermInfo;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
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
        return GzipUtil.compress(termInfo.toBytes());
    }

    public static List<TermInOneDoc> deserializeBySelf(byte[] bytes) {
        return TermInfo.byte2Object(GzipUtil.uncompress(bytes));
    }


}
