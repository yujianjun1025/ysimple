package com.search.engine.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.search.engine.protobuf.InvertPro;

import java.util.List;

/**
 * Created by yjj on 15/12/14.
 */
public class SerializeUtil {

    public static byte[] serialize(List<InvertPro.TermInOneDoc> termInOneDocList) {

        InvertPro.TermInfo.Builder builder = InvertPro.TermInfo.newBuilder();
        builder.addAllTermInDocList(termInOneDocList);
        InvertPro.TermInfo termInfo = builder.build();
        return termInfo.toByteArray();

    }

    public static List<InvertPro.TermInOneDoc> deserialize(byte[] bytes) throws InvalidProtocolBufferException {

        InvertPro.TermInfo.Builder builder = InvertPro.TermInfo.newBuilder();
        InvertPro.TermInfo termInfo = builder.mergeFrom(bytes).build();
        return termInfo.getTermInDocListList();
    }
}
