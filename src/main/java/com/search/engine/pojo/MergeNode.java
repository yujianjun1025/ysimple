package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yjj on 15/11/29.
 */
public class MergeNode {

    private int order;
    private int offset;
    private List<Integer> posList = Lists.newArrayList();

    public MergeNode(int order, List<Integer> posList) {
        this.order = order;
        this.posList = posList;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "MergeNode{" +
                "order=" + order +
                ", offset=" + offset +
                ", posList=" + Joiner.on(" ").join(posList) +
                '}';
    }
}
