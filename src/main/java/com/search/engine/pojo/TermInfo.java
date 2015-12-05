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
 */
public class TermInfo implements Comparable<Integer>, KryoSerializable {

    private int docId;
    private List<Integer> posList;
    private int tf;
    private double rank;

    public void write(Kryo kryo, Output output) {

        output.writeInt(docId);
        output.writeInt(posList.size());
        for (Integer pos : posList) {
            output.writeInt(pos);
        }
        output.writeInt(tf);
        output.writeDouble(rank);
    }

    public void read(Kryo kryo, Input input) {


        docId = input.readInt();
        int posSize = input.readInt();
        posList = Lists.newArrayList();
        for (int i = 0; i < posSize; i++) {
            posList.add(input.readInt());
        }
        tf = input.readInt();
        rank = input.readDouble();

    }

    public void read(Kryo kryo, Input input) {


        docId = input.readInt();
        int posSize = input.readInt();
        posList = Lists.newArrayList();
        for (int i = 0; i < posSize; i++) {
            posList.add(input.readInt());
        }
        tf = input.readInt();
        rank = input.readDouble();

    }

    public TermInfo(Integer docId, Collection<Integer> posList, int tf) {
        this.docId = docId;
        this.posList = Lists.newArrayList(posList);
        this.tf = tf;
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

    @Override
    public String toString() {
        return "InvertDocInfo{" +
                "docId=" + docId +
                ", posList=" + Joiner.on(" ").join(posList) +
                ", tf=" + tf +
                ", rank=" + rank +
                '}';
    }


}
