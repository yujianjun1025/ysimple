package com.search.indexserver.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * Created by yjj on 15/12/7.
 */
@Getter
@Setter
@ToString
public class FieldAndDocId {


    private int docId;
    private int fieldId;

    public FieldAndDocId(int fieldId, int docId) {
        this.fieldId = fieldId;
        this.docId = docId;
    }

}
