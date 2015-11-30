package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.search.engine.pojo.Doc;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.MergeNode;
import com.search.engine.pojo.WordInfo;
import com.search.engine.util.SortUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yjj on 15/11/22.
 */

@Component
public class InvertCache1 {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache1.class);

    private Multimap<String, Integer> invertCache = ArrayListMultimap.create();
    private Map<Integer, Map<String, WordInfo>> worldInfoCache = Maps.newHashMap();


    public Multimap<String, Integer> getInvertCache() {
        return invertCache;
    }

    public Map<Integer, Map<String, WordInfo>> getWorldInfoCache() {
        return worldInfoCache;
    }

    public void buildInvert(List<DocInfo>[] forwardCache) {


        Multimap<String, Integer> tmpInvertCache = ArrayListMultimap.create();
        Map<Integer, Map<String, WordInfo>> tmpWorldInfoCache = Maps.newHashMap();

        Integer docCount = 0;
        for (List<DocInfo> docInfoList : forwardCache) {
            for (DocInfo docInfo : docInfoList) {

                for (String world : docInfo.getWorldPosition().keys()) {

                    List<Integer> docIds = (List<Integer>) tmpInvertCache.get(world);
                    if(CollectionUtils.isEmpty(docIds)){
                        tmpInvertCache.put(world.intern(), docInfo.getDocId());
                        continue;
                    }

                    Integer index = Collections.binarySearch(docIds, docInfo.getDocId());
                    if (index < 0) {
                        docIds.add(Math.abs(index + 1), docInfo.getDocId());
                    }

                }
                docCount++;
            }

        }

        for (List<DocInfo> docInfoList : forwardCache) {
            for (DocInfo docInfo : docInfoList) {

                int worldCount = docInfo.getWorldCount();
                for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {

                    String world = entry.getKey();
                    double tf = (entry.getValue().size() * 1.0) / worldCount;
                    double idf = Math.log(docCount / tmpInvertCache.get(world).size());
                    double rank = tf * idf;

                    Map<String, WordInfo> wordInfoMap = tmpWorldInfoCache.get(docInfo.getDocId());
                    if (wordInfoMap == null) {
                        wordInfoMap = Maps.newHashMap();
                        tmpWorldInfoCache.put(docInfo.getDocId(), wordInfoMap);
                    }

                    wordInfoMap.put(world.intern(), new WordInfo(docInfo.getWorldPosition().get(world), tf, idf, rank));
                }

            }

        }

        invertCache = tmpInvertCache;
        worldInfoCache = tmpWorldInfoCache;


    }

    public void print(){

        for (Map.Entry<String, Collection<Integer>> entry : invertCache.asMap().entrySet()) {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append(entry.getKey()).append(":").append(Joiner.on(",").join(entry.getValue())).append("\n");
            logger.error(stringBuffer.toString());
        }

        logger.error("分割线2");
       /* for (Map.Entry<Integer, Map<String, WordInfo>> entry : worldInfoCache.entrySet()) {
            StringBuilder worldInfo = new StringBuilder("\n");
            worldInfo.append(entry.getKey()).append(":").append(Joiner.on(",")
                    .withKeyValueSeparator("=>").join(entry.getValue())).append("\n");
            logger.error(worldInfo.toString());
        }*/

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
