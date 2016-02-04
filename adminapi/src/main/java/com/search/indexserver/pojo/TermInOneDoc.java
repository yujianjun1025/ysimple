package com.search.indexserver.pojo;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.search.indexserver.util.NumberBytes;
import com.sun.tools.javac.util.Pair;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 *
 * Created by yjj on 16/2/3.
 */
@Setter
@ToString
public class TermInOneDoc implements Comparable<Object> {
    private int docId;
    private int field;
    private int tf;
    private double rank = 0.0;
    private List<Integer> positions = Lists.newArrayList();
    private byte[] bytes = null;

    //lazy加载
    public static Pair<TermInOneDoc, Integer> byte2Object(byte[] bytes, int begin) {
        TermInOneDoc termInOneDoc = new TermInOneDoc();
        int bytesLength = NumberBytes.bytesToInt(bytes, begin);
        termInOneDoc.docId = NumberBytes.bytesToInt(bytes, begin + 4);
        termInOneDoc.bytes = new byte[bytesLength];
        System.arraycopy(bytes, begin, termInOneDoc.bytes, 0, bytesLength);
        return new Pair<TermInOneDoc, Integer>(termInOneDoc, bytesLength);
    }

    public int getDocId() {
        return docId;
    }

    public int getField() {
        if (field == 0 && bytes != null) {
            field = NumberBytes.bytesToInt(bytes, 8);
        }
        return field;
    }

    public int getTf() {
        if (tf == 0 && bytes != null) {
            tf = NumberBytes.bytesToInt(bytes, 12);
        }
        return tf;
    }

    public double getRank() {
        if (Doubles.compare(rank, 0.0) == 0 && bytes != null) {
            rank = NumberBytes.bytesToDouble(bytes, 16);
        }
        return rank;
    }

    public List<Integer> getPositions() {
        if (CollectionUtils.isEmpty(positions) && bytes != null) {
            positions = NumberBytes.bytesToInts(bytes, 24);
        }
        return positions;
    }

    public int compareTo(Object o) {
        if (o instanceof Integer) {
            return Integer.compare(getDocId(), (Integer) o);
        } else if (o instanceof FieldAndDocId) {
            int res = Ints.compare(getField(), ((FieldAndDocId) o).getFieldId());
            return res != 0 ? res : Ints.compare(getDocId(), ((FieldAndDocId) o).getDocId());
        }
        return 0;
    }

    public byte[] toBytes() {

        //总长度4 + 内容长度 4+3 + 8 + position长度4 + position长度
        int bytesLength = 4 + 4 * 3 + 8 + 4 + 4 * positions.size();
        int begin = 0;
        byte[] bytes = new byte[bytesLength];

        //转化为byte的总长度
        for (byte intByte : NumberBytes.intToBytes(bytesLength)) {
            bytes[begin++] = intByte;
        }

        for (byte intByte : NumberBytes.intToBytes(docId)) {
            bytes[begin++] = intByte;
        }

        for (byte intByte : NumberBytes.intToBytes(field)) {
            bytes[begin++] = intByte;
        }

        for (byte intByte : NumberBytes.intToBytes(tf)) {
            bytes[begin++] = intByte;
        }

        for (byte doubleByte : NumberBytes.doubleToBytes(rank)) {
            bytes[begin++] = doubleByte;
        }

        //position长度及position内容
        for (byte listByte : NumberBytes.intsToBytes(positions)) {
            bytes[begin++] = listByte;
        }

        return bytes;
    }

}
