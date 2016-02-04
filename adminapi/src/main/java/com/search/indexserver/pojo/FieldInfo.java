package com.search.indexserver.pojo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * Created by yjj on 15/12/6.
 */

@Getter
@Setter
public class FieldInfo {
    private int field;
    private int worldCount;
    private Multimap<String, Integer> worldPosition = ArrayListMultimap.create();

    public FieldInfo(int field, int worldCount, Multimap<String, Integer> worldPosition) {
        this.field = field;
        this.worldCount = worldCount;
        this.worldPosition = worldPosition;
    }

}
