package com.search.engine.pojo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.List;

/**
 * Created by yjj on 15/12/4.
 * term信息
 */
public class TermInfo implements Comparable<Integer>, KryoSerializable {

    private int docId;
    private List<Integer> posList = Lists.newArrayList();
    private double rank;

    //低8位为fieldId,高24为tf
    private int fieldAndTf;

    public TermInfo() {
    }

    public TermInfo(Integer docId, int fieldId, Collection<Integer> posList, int tf) {
        this.docId = docId;
        setFieldAndTf(fieldId, tf);
        this.posList = Lists.newArrayList(posList);
    }

    public int getFieldId() {
        return (fieldAndTf & 0x000000ff);
    }

    public int getTf() {
        return (fieldAndTf & 0xffffff00) >>> 8;
    }

    public void setFieldAndTf(int fieldId, int tf) {
        fieldAndTf |= tf & 0xffffff00;
        fieldAndTf |= fieldId & 0x000000ff;
    }

    public int compareTo(Integer o) {
        return Ints.compare(docId, o);
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }


    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }


    public void write(Kryo kryo, Output output) {

     /*   output.writeInt(docId);
        output.writeInt(posList.size());
        for (Integer pos : posList) {
            output.writeInt(pos);
        }
        output.writeInt(tf);
        output.writeDouble(rank);*/
    }

    public void read(Kryo kryo, Input input) {

       /* docId = input.readInt();
        int posSize = input.readInt();
        posList = Lists.newArrayList();
        for (int i = 0; i < posSize; i++) {
            posList.add(input.readInt());
        }
        tf = input.readInt();
        rank = input.readDouble();*/

    }

    @Override
    public String toString() {
        return "InvertDocInfo{" +
                "docId=" + docId +
                ", posList=" + Joiner.on(" ").join(posList) +
                // ", tf=" + tf +
                ", rank=" + rank +
                '}';
    }


}
