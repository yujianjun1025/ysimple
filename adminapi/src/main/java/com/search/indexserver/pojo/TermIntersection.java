package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by yjj on 15/12/4.
 * 求交中间过程类
 */
@Getter
@Setter
public class TermIntersection {


    private int docId;
    private Map<Integer, TermInOneDoc> termInfoMap = Maps.newHashMap();

    public TermIntersection(int docId, Map<Integer, TermInOneDoc> termInfoMap) {
        this.docId = docId;
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
