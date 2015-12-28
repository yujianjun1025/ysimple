package com.search.indexserver.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.search.indexserver.protobuf.InvertPro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by yjj on 15/12/14.
 */
public class SerializeUtil {

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


    public static void main(String[] args) {

        Kryo kryo = new Kryo();
        Registration registration = kryo.register(InvertPro.TermInfo.class);

        InvertPro.TermInOneDoc.Builder builder = InvertPro.TermInOneDoc.newBuilder();
        builder.setDocId(1).setField(2).addPositions(3).setRank(4).setTf(5);

        InvertPro.TermInfo.Builder termInfoBuild = InvertPro.TermInfo.newBuilder();
        termInfoBuild.addAllTermInDocList(Lists.newArrayList(builder.build()));

        //序列化
        Output output = new Output(1, 4096);
        kryo.writeObject(output, termInfoBuild.build());
        byte[] bb = output.toBytes();

        System.out.println("bb size : " + bb.length);
        output.flush();

        //反序列化
        Input input = new Input(bb);
        InvertPro.TermInfo termInfo = (InvertPro.TermInfo) kryo.readObject(input, registration.getType());
        input.close();

        System.out.println(termInfo);

    }

}
