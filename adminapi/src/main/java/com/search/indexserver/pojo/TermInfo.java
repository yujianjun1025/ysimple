package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.search.indexserver.util.NumberBytes;
import com.sun.tools.javac.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by yjj on 16/2/3.
 */
@Getter
@Setter
@Slf4j
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

        Stopwatch stopwatch = Stopwatch.createStarted();
        int begin = 0;
        Pair<TermInOneDoc, Integer> pair;
        do {
            pair = TermInOneDoc.byte2Object(bytes, begin);
            inOneDocs.add(pair.fst);
            begin += pair.snd;
        } while (pair.snd < bytes.length && begin < bytes.length);
        log.info("TermInOneDoc.byte2Object byte length {} 耗时 {} 毫秒", bytes.length, (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS)) / 1000000);
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
















































