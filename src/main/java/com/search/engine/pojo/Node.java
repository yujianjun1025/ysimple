package com.search.engine.pojo;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * Created by yjj on 15/12/13.
 */
public class Node implements java.io.Serializable {

    private int termCode;
    private List<Integer> posList;
    private double rank;

    public Node(int termCode, List<Integer> posList, double rank) {
        this.termCode = termCode;
        this.posList = posList;
        this.rank = rank;
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


    @Override
    public String toString() {
        return "Node{" +
                "termCode=" + termCode +
                ", posList=" + Joiner.on(" ").join(posList) +
                ", rank=" + rank +
                '}';
    }
}
