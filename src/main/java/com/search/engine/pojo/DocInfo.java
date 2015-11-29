package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by yjj on 15/11/29.
 */
public class DocInfo {

    private Integer docId;
    private Integer worldCount;
    private Multimap<String, Integer> worldPosition = ArrayListMultimap.create();

    public DocInfo(Integer docId, Integer worldCount, Multimap<String, Integer> worldPosition) {
        this.docId = docId;
        this.worldCount = worldCount;
        this.worldPosition = worldPosition;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Integer getWorldCount() {
        return worldCount;
    }

    public void setWorldCount(Integer worldCount) {
        this.worldCount = worldCount;
    }

    public Multimap<String, Integer> getWorldPosition() {
        return worldPosition;
    }

    public void setWorldPosition(Multimap<String, Integer> worldPosition) {
        this.worldPosition = worldPosition;
    }

    @Override
    public String toString() {


        return "DocInfo{" +
                "docId=" + docId +
                ", worldCount=" + worldCount +
                ", worldPosition=" + Joiner.on(",").withKeyValueSeparator("=>").join(worldPosition.entries()) +
                '}';
    }
}
