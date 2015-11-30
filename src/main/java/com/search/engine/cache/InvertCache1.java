package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.MergeNode;
import com.search.engine.pojo.WordInfo;
import com.search.engine.util.SortUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yjj on 15/11/22.
 */

public class InvertCache1 {


    private Multimap<String, Integer> invertCache = ArrayListMultimap.create();
    private Map<Integer, Map<String, WordInfo>> worldInfoCache = Maps.newHashMap();


    public InvertCache1() {

        ForwardCache forwardCache = new ForwardCache();
        buildInvert(forwardCache.getForwardCache());

    }

    public Multimap<String, Integer> getInvertCache() {


        return invertCache;
    }

    public Map<Integer, Map<String, WordInfo>> getWorldInfoCache() {


        return worldInfoCache;
    }

    public void buildInvert(List<DocInfo> forwardCache) {


        for (DocInfo docInfo : forwardCache) {
            for (String world : docInfo.getWorldPosition().keys()) {

                List<Integer> docIds = (List<Integer>) invertCache.get(world);
                Integer index = Collections.binarySearch(docIds, docInfo.getDocId());
                if (index < 0) {
                    invertCache.put(world, docInfo.getDocId());
                }
            }
        }

        Integer docCount = forwardCache.size();
        for (DocInfo docInfo : forwardCache) {

            Integer worldCount = docInfo.getWorldCount();
            for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {

                String world = entry.getKey();
                double tf = (entry.getValue().size() * 1.0) / worldCount;
                double idf = Math.log(docCount / invertCache.get(world).size());
                double rank = tf * idf;

                Map<String, WordInfo> wordInfoMap = worldInfoCache.get(docInfo.getDocId());
                if (wordInfoMap == null) {
                    wordInfoMap = Maps.newHashMap();
                    worldInfoCache.put(docInfo.getDocId(), wordInfoMap);
                }

                wordInfoMap.put(world, new WordInfo(docInfo.getWorldPosition().get(world), tf, idf, rank));
            }

        }


    }

    @Override
    public String toString() {

        StringBuilder invert = new StringBuilder("\n");
        for (Map.Entry<String, Collection<Integer>> entry : invertCache.asMap().entrySet()) {
            invert.append(entry.getKey()).append(":").append(Joiner.on(",").join(entry.getValue())).append("\n");
        }

        StringBuilder worldInfo = new StringBuilder("\n");
        for (Map.Entry<Integer, Map<String, WordInfo>> entry : worldInfoCache.entrySet()) {
            worldInfo.append(entry.getKey()).append(":").append(Joiner.on(",")
                    .withKeyValueSeparator("=>").join(entry.getValue())).append("\n");
        }

        return "InvertCache1{"
                + invert
                + "worldInfoCache=" + worldInfo +
                '}';
    }


}
