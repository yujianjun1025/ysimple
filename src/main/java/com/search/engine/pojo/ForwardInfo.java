package com.search.engine.pojo;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;

/**
 * Created by yjj on 15/12/11.
 */
public class ForwardInfo implements Comparable<Integer> {

    private int docId;
    private List<ForwardTermInfo> forwardTermInfoList = Lists.newArrayList();

    public ForwardInfo(Integer docId) {
        this.docId = docId;
    }


    public void addForwardTerm(ForwardTermInfo termInfo) {
        forwardTermInfoList.add(termInfo);
    }

    public List<ForwardTermInfo> getForwardTermInfoList() {
        return forwardTermInfoList;
    }

    public void setForwardTermInfoList(List<ForwardTermInfo> forwardTermInfoList) {
        this.forwardTermInfoList = forwardTermInfoList;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }


    public int compareTo(Integer o) {
        return Ints.compare(docId, o);
    }
}

