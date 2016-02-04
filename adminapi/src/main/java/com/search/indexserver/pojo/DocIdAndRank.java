package com.search.indexserver.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yjj on 15/12/5.
 */

@Getter
@Setter
@ToString
public class DocIdAndRank implements Comparable<Double> {

    private int docId;
    private double rank;

    public DocIdAndRank(int docId, double rank) {
        this.docId = docId;
        this.rank = rank;
    }
    public int compareTo(Double o) {
        return Double.compare(rank, o);
    }
}
