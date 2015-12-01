package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import sun.security.util.BitArray;

import java.util.Collection;
import java.util.List;

/**
 * Created by yjj on 15/11/29.
 */
public class WordInfo implements Comparable<Integer> {

    private int wordNum;
    private BitArray posList = new BitArray(128);
    private int tf;
    private int rank;

    public WordInfo(int wordNum) {

        this.wordNum = wordNum;

    }

    public void setWordNum(int wordNum) {
        this.wordNum = wordNum;
    }

    public BitArray getPosList() {
        return posList;
    }

    public void setPosList(BitArray posList) {
        this.posList = posList;
    }

    public double getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }


    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "WordInfo{" +
                "posList=" + posList +
                ", tf=" + tf +
                ", rank=" + rank +
                '}';
    }

    public int compareTo(Integer o) {
        return Ints.compare(wordNum, o);
    }

    public int getWordNum() {
        return wordNum;
    }
}
