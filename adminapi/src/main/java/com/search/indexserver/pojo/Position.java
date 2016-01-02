package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yjj on 15/12/28.
 */
public class Position {

    private int offset;
    private List<Integer> segmentLength = Lists.newArrayList();

    public Position(){

    }

    public Position(int offset) {
        this.offset = offset;
    }

    public List<Integer> getSegmentLength() {
        return segmentLength;
    }

    public void setSegmentLength(List<Integer> segmentLength) {
        this.segmentLength = segmentLength;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<Integer> getSize() {
        return segmentLength;
    }

    public void addSize(Integer offset) {
        segmentLength.add(offset);
    }

    public Integer getTotalSize() {
        return segmentLength.get(segmentLength.size() - 1);
    }

    @Override
    public String toString() {
        return "Position{" +
                "OFFSET=" + offset +
                ", segmentLength=" + Joiner.on(" ").join(segmentLength) +
                '}';
    }
}
