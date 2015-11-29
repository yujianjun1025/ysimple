package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Created by yjj on 15/11/29.
 */
public class WordInfo {

    private List<Integer> posList;
    private double tf;
    private double idf;
    private double rank;


    public WordInfo(Collection<Integer> posList, double tf, double idf, double rank) {
        this.posList = Lists.newArrayList(posList);
        this.tf = tf;
        this.idf = idf;
        this.rank = rank;
    }

    public List<Integer> getPosList() {

        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }

    public double getTf() {
        return tf;
    }

    public void setTf(double tf) {
        this.tf = tf;
    }

    public double getIdf() {
        return idf;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "WordInfo{" +
                "posList=" + Joiner.on(" ").join(posList) +
                ", tf=" + tf +
                ", idf=" + idf +
                ", rank=" + rank +
                '}';
    }
}
