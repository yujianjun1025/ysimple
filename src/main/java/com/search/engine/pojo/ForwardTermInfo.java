package com.search.engine.pojo;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;

/**
 * Created by yjj on 15/12/11.
 */
public class ForwardTermInfo implements Comparable<Integer> {

    private int termCode;
    private List<Integer> posList = Lists.newArrayList();
    private double rank;

    public ForwardTermInfo(int termCode) {

        this.termCode = termCode;
    }

    public int getTermCode() {
        return termCode;
    }

    public void setTermCode(int termCode) {
        this.termCode = termCode;
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

    public int compareTo(Integer o) {
        return Ints.compare(termCode, o);
    }
}
