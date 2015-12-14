package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.search.engine.protobuf.InvertPro;

import java.util.Map;

/**
 * Created by yjj on 15/12/4.
 * 求交中间过程类
 */
public class TermIntersection {


    private int docId;
    private Map<Integer, InvertPro.TermInOneDoc> termInfoMap = Maps.newHashMap();

    public TermIntersection(int docId, Map<Integer, InvertPro.TermInOneDoc> termInfoMap) {
        this.docId = docId;
        this.termInfoMap = termInfoMap;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public Map<Integer, InvertPro.TermInOneDoc> getTermInfoMap() {
        return termInfoMap;
    }

    public void setTermInfoMap(Map<Integer, InvertPro.TermInOneDoc> termInfoMap) {
        this.termInfoMap = termInfoMap;
    }


    @Override
    public String toString() {
        return "TermIntersection{" +
                "docId=" + docId +
                ", termInfoMap=" + Joiner.on(" ").withKeyValueSeparator("=>").join(termInfoMap) +
                '}';
    }
}
