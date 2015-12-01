package com.search.engine.pojo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yjj on 15/11/29.
 */
public class DocInfo implements KryoSerializable {

    private static final long serialVersionUID = -2385906232920579818L;

    private int docId;
    private int worldCount;
    private Multimap<String, Integer> worldPosition = ArrayListMultimap.create();

    public DocInfo() {

    }

    public DocInfo(Integer docId, Integer worldCount, Multimap<String, Integer> worldPosition) {
        this.docId = docId;
        this.worldCount = worldCount;
        this.worldPosition = worldPosition;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Integer getWorldCount() {
        return worldCount;
    }

    public void setWorldCount(Integer worldCount) {
        this.worldCount = worldCount;
    }

    public Multimap<String, Integer> getWorldPosition() {
        return worldPosition;
    }

    public void setWorldPosition(Multimap<String, Integer> worldPosition) {
        this.worldPosition = worldPosition;
    }

    public void write(Kryo kryo, Output output) {

        output.writeInt(docId);
        output.writeInt(worldCount);
        output.writeInt(worldPosition.size());
        for (Map.Entry<String, Collection<Integer>> entry : worldPosition.asMap().entrySet()) {
            output.writeString(entry.getKey());
            output.writeInt(entry.getValue().size());
            for (Integer integer : entry.getValue()) {
                output.writeInt(integer);
            }
        }
    }

    public void read(Kryo kryo, Input input) {
        docId = input.readInt();
        worldCount = input.readInt();

        int worldPositionSize = input.readInt();
        for (int i = 0; i < worldPositionSize; i++) {
            String world = input.readString();
            int posSize = input.readInt();
            for (int j = 0; j < posSize; j++) {
                worldPosition.put(world, input.readInt());
            }
        }

    }

    @Override
    public String toString() {


        return "DocInfo{" +
                "docId=" + docId +
                ", worldCount=" + worldCount +
                ", worldPosition=" + Joiner.on(",").withKeyValueSeparator("=>").join(worldPosition.entries()) +
                '}';

    }


}
