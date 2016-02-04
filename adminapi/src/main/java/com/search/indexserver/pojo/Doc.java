package com.search.indexserver.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yjj on 15/11/22.
 *
 */

@Getter
@Setter
@ToString
public class Doc {

    private int docId;
    private String value;
    public Doc(Integer docId, String value) {
        this.docId = docId;
        this.value = value.intern();
    }
}
