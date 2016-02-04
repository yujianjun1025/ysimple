package com.search.indexserver.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yjj on 15/11/29.
 *
 */
@Getter
@Setter
public class DocInfo {

    List<FieldInfo> field = Lists.newArrayList();
    private int docId;

    public DocInfo(Integer docId, int field, Integer worldCount, Multimap<String, Integer> worldPosition) {
        this.docId = docId;
        this.field = Lists.newArrayList(new FieldInfo(field, worldCount, worldPosition));
    }
}
