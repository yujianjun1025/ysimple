package com.search.engine.pojo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by yjj on 15/12/6.
 */
public class FieldInfo {
    private int field;
    private int worldCount;
    private Multimap<Integer, Integer> worldPosition = ArrayListMultimap.create();

    public FieldInfo(int field, int worldCount, Multimap<Integer, Integer> worldPosition) {
        this.field = field;
        this.worldCount = worldCount;
        this.worldPosition = worldPosition;
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int getWorldCount() {
        return worldCount;
    }

    public void setWorldCount(int worldCount) {
        this.worldCount = worldCount;
    }

    public Multimap<Integer, Integer> getWorldPosition() {
        return worldPosition;
    }

    public void setWorldPosition(Multimap<Integer, Integer> worldPosition) {
        this.worldPosition = worldPosition;
    }
}
