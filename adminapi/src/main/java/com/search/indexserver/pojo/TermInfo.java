package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.search.indexserver.util.NumberBytes;
import com.sun.tools.javac.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yjj on 16/2/3.
 */
public class TermInfo {

    private static final Logger logger = LoggerFactory.getLogger(TermInfo.class);


    List<TermInOneDoc> inOneDocList = Lists.newArrayList();

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

    public static void main(String[] args) {

        TermInOneDoc inOneDoc = new TermInOneDoc();

        inOneDoc.setDocId(12);
        inOneDoc.setField(2);
        inOneDoc.setTf(34);
        inOneDoc.setRank(3.14);
        inOneDoc.setPositions(Lists.newArrayList(3, 5, 7, 13, 14, 6, 7, 9, 0));


        Pair<TermInOneDoc, Integer> tmp = TermInOneDoc.byte2Object(inOneDoc.toBytes(), 0);
        System.out.println(tmp.fst);


        TermInOneDoc inOneDoc2 = new TermInOneDoc();

        inOneDoc2.setDocId(124);
        inOneDoc2.setField(112);
        inOneDoc2.setTf(340);
        inOneDoc2.setRank(3.564);
        inOneDoc2.setPositions(Lists.newArrayList(3, 5, 7, 13, 14, 6, 7, 9, 0, 11111, 111));

        TermInfo termInfo = new TermInfo(Lists.newArrayList(inOneDoc, inOneDoc2));


        byte[] bytes = termInfo.toBytes();


        System.out.println(Joiner.on("\n").join(TermInfo.byte2Object(bytes)));


    }

    public List<TermInOneDoc> getInOneDocList() {
        return inOneDocList;
    }

    public void setInOneDocList(List<TermInOneDoc> inOneDocList) {
        this.inOneDocList = inOneDocList;
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
















































