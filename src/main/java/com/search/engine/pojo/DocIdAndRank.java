package com.search.engine.pojo;

/**
 * Created by yjj on 15/12/5.
 */
public class DocIdAndRank implements Comparable<Double> {

    private int docId;
    private double rank;

    public DocIdAndRank(int docId, double rank) {
        this.docId = docId;
        this.rank = rank;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public int compareTo(Double o) {
        return Double.compare(rank, o);
    }
}
