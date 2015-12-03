package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.WordInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/22.
 */

public class InvertCache1 implements KryoSerializable {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache1.class);
    private static Integer docCount = 0;
    private Map<Integer, List<Integer>> invertCache = Maps.newHashMap();
    private Map<Integer, List<WordInfo>> worldInfoCache = Maps.newHashMap();
    private Map<Integer, List<Integer>> tmpInvertCache = Maps.newHashMap();
    private Map<Integer, List<WordInfo>> tmpWorldInfoCache = Maps.newHashMap();
    private int ALLWORLDCOUNT = 0;
    private Map<String, Integer> str2int = Maps.newHashMap();

    public static InvertCache1 getInstance() {
        return InvertCacheHolder.instance;
    }

    public Map<Integer, List<WordInfo>> getWorldInfoCache() {
        return worldInfoCache;
    }

    public void write(Kryo kryo, Output output) {

        int invertCacheSize = tmpInvertCache.size();
        output.writeInt(invertCacheSize);
        for (int i = 0; i < invertCacheSize; i++) {

            List<Integer> integers = tmpInvertCache.get(i);
            int docIdSize = integers.size();
            output.writeInt(docIdSize);

            for (Integer integer : integers) {
                output.writeInt(integer);
            }
        }
    }

    public void read(Kryo kryo, Input input) {

    }

    public Integer getStringCode(Character character) {

        Integer res = str2int.get(String.valueOf(character));
        return res != null ? res : -1;

    }

    public void addDocInfo(DocInfo docInfo) {

        docCount++;

        List<WordInfo> wordInfoList = Lists.newArrayList();
        tmpWorldInfoCache.put(docInfo.getDocId(), wordInfoList);

        for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {

            String world = entry.getKey();
            if (!str2int.containsKey(world.intern())) {
                str2int.put(world.intern(), ALLWORLDCOUNT++);
            }
            Integer worldNum = str2int.get(world);

            List<Integer> docIds = (List<Integer>) tmpInvertCache.get(worldNum);
            if (CollectionUtils.isEmpty(docIds)) {

                List<Integer> docIdList = tmpInvertCache.get(worldNum);
                if (docIdList == null) {
                    docIdList = Lists.newArrayList();
                    tmpInvertCache.put(worldNum, docIdList);
                }
                docIdList.add(docInfo.getDocId());
                continue;
            }

            Integer index = Collections.binarySearch(docIds, docInfo.getDocId());
            if (index < 0) {
                docIds.add(Math.abs(index + 1), docInfo.getDocId());
            }


            int worldPos = Collections.binarySearch(wordInfoList, worldNum);
            WordInfo wordInfo = null;
            if (worldPos < 0) {
                worldPos = Math.abs(worldPos + 1);
                wordInfo = new WordInfo(worldNum);
                wordInfoList.add(worldPos, wordInfo);
            } else {

                wordInfo = wordInfoList.get(worldPos);
            }

            for (Integer pos : entry.getValue()) {
                wordInfo.getPosList().set(pos, true);
            }

            wordInfo.setTf((entry.getValue().size() * 1000000) / docInfo.getWorldCount());
        }

    }

    public void calculateRank() {


        for (Map.Entry<Integer, List<WordInfo>> entry : tmpWorldInfoCache.entrySet()) {

            for (WordInfo wordInfo : entry.getValue()) {

                double idf = Math.log(docCount / tmpInvertCache.get(wordInfo.getWordNum()).size());
                int rank = (int) (wordInfo.getTf() * idf * 100000);
                wordInfo.setRank(rank);

            }

        }

        logger.info("tmpInvertCache.size ={} ", tmpInvertCache.size());
        logger.info("tmpWorldInfoCache.size ={} ", tmpWorldInfoCache.size());

        invertCache = tmpInvertCache;
        worldInfoCache = tmpWorldInfoCache;
        docCount = 0;

    }

    @Override
    public String toString() {

        StringBuilder invert = new StringBuilder("\n");
        for (Map.Entry<Integer, List<Integer>> entry : invertCache.entrySet()) {
            invert.append(entry.getKey()).append(":").append(Joiner.on(",").join(entry.getValue())).append("\n");
        }

        StringBuilder worldInfo = new StringBuilder("\n");
        for (Map.Entry<Integer, List<WordInfo>> entry : worldInfoCache.entrySet()) {
            worldInfo.append(entry.getKey()).append(":").append(Joiner.on(",").join(entry.getValue())).append("\n");
        }

        return "InvertCache1{"
                + invert
                + "worldInfoCache=" + worldInfo +
                '}';
    }

    public List<Integer> getDocIdByStringNum(Integer stringNum) {
        return invertCache.get(stringNum);
    }

    public Map<Integer, List<Integer>> getInvertCache() {
        return invertCache;
    }

    public static final class InvertCacheHolder {
        private static final InvertCache1 instance = new InvertCache1();
    }


}
