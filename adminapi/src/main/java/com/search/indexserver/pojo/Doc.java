package com.search.indexserver.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yjj on 15/11/22.
 *
 */

@Getter
@Setter
public class Doc {

    private int docId;
    private String value;
    public Doc(Integer docId, String value) {
        this.docId = docId;
        this.value = value.intern();
    }
}
