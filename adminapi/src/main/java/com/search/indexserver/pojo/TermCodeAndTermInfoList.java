package com.search.indexserver.pojo;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yjj on 15/12/4.
 *
 */
@Getter
@Setter
public class TermCodeAndTermInfoList {

    private Integer termCode;
    private List<TermInOneDoc> termInOneDocList;

    public TermCodeAndTermInfoList(Integer termCode, List<TermInOneDoc> termInOneDocList) {
        this.termCode = termCode;
        this.termInOneDocList = termInOneDocList;
    }

    @Override
    public String toString() {
        return "TermCodeAndTermInfoList{" +
                "termCode=" + termCode +
                ", termInOneDocList=" + Joiner.on(" ").join(termInOneDocList) +
                '}';
    }
}
