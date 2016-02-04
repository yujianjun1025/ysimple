package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * Created by yjj on 15/12/28.
 */
@Getter
@Setter

public class Position {

    private int offset;
    private int docCount;
    private List<Integer> segmentLength = Lists.newArrayList();

    public Position() {
    }

    public Position(int offset) {
        this.offset = offset;
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
                "offset=" + offset +
                ", docCount=" + docCount +
                ", segmentLength=" + Joiner.on(" ").join(segmentLength) +
                '}';
    }
}
