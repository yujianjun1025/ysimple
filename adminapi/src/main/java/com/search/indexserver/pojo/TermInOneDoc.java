package com.search.indexserver.pojo;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.search.indexserver.util.NumberBytes;
import com.sun.tools.javac.util.Pair;

import java.util.List;

/**
 * Created by yjj on 16/2/3.
 */
public class TermInOneDoc implements Comparable<Object> {
    private int docId;
    private int field;
    private int tf;
    private double rank;
    private List<Integer> positions = Lists.newArrayList();

    public static Pair<TermInOneDoc, Integer> byte2Object(byte[] bytes, int begin) {
        TermInOneDoc termInOneDoc = new TermInOneDoc();
        int bytesLength = NumberBytes.bytesToInt(bytes, begin);
        termInOneDoc.docId = NumberBytes.bytesToInt(bytes, begin + 4);
        termInOneDoc.field = NumberBytes.bytesToInt(bytes, begin + 8);
        termInOneDoc.tf = NumberBytes.bytesToInt(bytes, begin + 12);
        termInOneDoc.rank = NumberBytes.bytesToDouble(bytes, begin + 16);
        termInOneDoc.positions = NumberBytes.bytesToInts(bytes, begin + 24);
        return new Pair<TermInOneDoc, Integer>(termInOneDoc, bytesLength);
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

    @Override
    public String toString() {
        return "TermInOneDoc{" +
                "docId=" + docId +
                ", field=" + field +
                ", tf=" + tf +
                ", rank=" + rank +
                ", positions=" + positions +
                '}';
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
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
