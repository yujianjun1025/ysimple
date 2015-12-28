package com.search.indexserver.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.search.indexserver.protobuf.InvertPro;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/12/14.
 */
public class SerializeUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    public static byte[] serializeByProto(List<InvertPro.TermInOneDoc> termInOneDocList) {

        InvertPro.TermInfo.Builder builder = InvertPro.TermInfo.newBuilder();
        builder.addAllTermInDocList(termInOneDocList);
        InvertPro.TermInfo termInfo = builder.build();
        return termInfo.toByteArray();

    }

    public static List<InvertPro.TermInOneDoc> deserializeByProto(byte[] bytes) throws InvalidProtocolBufferException {

        InvertPro.TermInfo.Builder builder = InvertPro.TermInfo.newBuilder();
        //InvertPro.TermInfo termInfo = builder.mergeFrom(bytes).build();
        InvertPro.TermInfo termInfo = InvertPro.TermInfo.parseFrom(bytes);

        return termInfo.getTermInDocListList();
    }


    public static byte[] serializeByHessian(List<InvertPro.TermInOneDoc> termInOneDocList) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HessianOutput ho = new HessianOutput(os);
        InvertPro.TermInfo.Builder builder = InvertPro.TermInfo.newBuilder();
        builder.addAllTermInDocList(termInOneDocList);
        ho.writeObject(builder.build());
        return os.toByteArray();


    }

    public static List<InvertPro.TermInOneDoc> deserializeByHessian(byte[] bytes) throws IOException {

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(is);
        return (List<InvertPro.TermInOneDoc>) hi.readObject();
    }


    public static byte[] serializeByKryo(List<InvertPro.TermInOneDoc> termInOneDocList) throws IOException {

        InvertPro.TermInfo.Builder termInfoBuild = InvertPro.TermInfo.newBuilder();
        termInfoBuild.addAllTermInDocList(termInOneDocList);

        Kryo kryo = new Kryo();
        //序列化
        Output output = new Output(1, 409600000);
        kryo.writeObject(output, termInfoBuild.build());
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;

    }

    public static List<InvertPro.TermInOneDoc> deserializeByKryo(byte[] bytes) throws IOException {

        Kryo kryo = new Kryo();
        Registration registration = kryo.register(InvertPro.TermInfo.class);

        //反序列化
        Input input = new Input(bytes);
        InvertPro.TermInfo termInfo = (InvertPro.TermInfo) kryo.readObject(input, registration.getType());
        input.close();
        return termInfo.getTermInDocListList();
    }


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
