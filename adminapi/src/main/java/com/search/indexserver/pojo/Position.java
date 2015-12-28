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

    public Position(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
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
