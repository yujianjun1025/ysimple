package com.search.engine.pojo;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */
public class Doc {

    private int docId;
    private String value;


    public Doc(Integer docId, String value) {
        this.docId = docId;
        this.value = value.intern();
    }


    public List<String> split() {

        List<String> result = Lists.newArrayList();
        for (Character character : value.toCharArray()) {
            result.add(character.toString());
        }
        return result;

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
