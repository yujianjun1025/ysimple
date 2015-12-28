package com.search.indexserver.pojo;

/**
 * Created by yjj on 15/11/22.
 *
 */
public class Doc {

    private int docId;
    private String value;
    public Doc(Integer docId, String value) {
        this.docId = docId;
        this.value = value.intern();
    }

    public Integer getDocId() {

        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
