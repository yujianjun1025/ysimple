package com.search.engine.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Created by yjj on 15/11/29.
 *
 */
public class DocInfo {

    private int docId;
    List<FieldInfo> field = Lists.newArrayList();

    public DocInfo(Integer docId, int field, Integer worldCount, Multimap<String, Integer> worldPosition) {
        this.docId = docId;
        this.field = Lists.newArrayList(new FieldInfo(field, worldCount, worldPosition));
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public List<FieldInfo> getField() {
        return field;
    }

    public void setField(List<FieldInfo> field) {
        this.field = field;
    }

}
