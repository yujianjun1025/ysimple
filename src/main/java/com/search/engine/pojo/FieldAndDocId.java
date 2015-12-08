package com.search.engine.pojo;

/**
 * Created by yjj on 15/12/7.
 */
public class FieldAndDocId {


    private int docId;
    private int fieldId;

    public FieldAndDocId(int fieldId, int docId) {
        this.fieldId = fieldId;
        this.docId = docId;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }
}
