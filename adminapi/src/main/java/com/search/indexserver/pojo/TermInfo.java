package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.search.indexserver.util.NumberBytes;
import com.sun.tools.javac.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yjj on 16/2/3.
 */
@Getter
@Setter
public class TermInfo {

    private List<TermInOneDoc> inOneDocList = Lists.newArrayList();

    public TermInfo(List<TermInOneDoc> inOneDocList) {
        this.inOneDocList = inOneDocList;
    }

    public static List<TermInOneDoc> byte2Object(byte[] bytes) {

        List<TermInOneDoc> inOneDocs = Lists.newArrayList();

        if (bytes.length == 0) {
            return inOneDocs;
        }

        int begin = 0;
        Pair<TermInOneDoc, Integer> pair = null;
        do {

            pair = TermInOneDoc.byte2Object(bytes, begin);
            inOneDocs.add(pair.fst);
            begin += pair.snd;

        } while (pair.snd < bytes.length && begin < bytes.length);


        return inOneDocs;
    }


    @Override
    public String toString() {
        return "TermInfo{\n" +
                "inOneDocList=\n" + Joiner.on("\n").join(inOneDocList) +
                "\n}";
    }

    public byte[] toBytes() {

        byte[] bytes = new byte[1];
        int sumLen = 0;
        for (TermInOneDoc inOneDoc : inOneDocList) {
            byte[] oneDocByte = inOneDoc.toBytes();
            bytes = NumberBytes.add2Tail(bytes, sumLen, oneDocByte, 0, oneDocByte.length);
            sumLen += oneDocByte.length;
        }

        byte[] ret = new byte[sumLen];
        System.arraycopy(bytes, 0, ret, 0, sumLen);
        return ret;
    }

}
















































