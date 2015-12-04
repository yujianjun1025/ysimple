package com.search.engine.pojo;

import java.util.List;

/**
 * Created by yjj on 15/12/4.
 */
public class TermCodeAndTermInfoList {

    private Integer termCode ;
    private List<TermInfo> termInfoList;

    public TermCodeAndTermInfoList(Integer termCode, List<TermInfo> termInfoList) {
        this.termCode = termCode;
        this.termInfoList = termInfoList;
    }

    public Integer getTermCode() {
        return termCode;
    }

    public void setTermCode(Integer termCode) {
        this.termCode = termCode;
    }

    public List<TermInfo> getTermInfoList() {
        return termInfoList;
    }

    public void setTermInfoList(List<TermInfo> termInfoList) {
        this.termInfoList = termInfoList;
    }
}
