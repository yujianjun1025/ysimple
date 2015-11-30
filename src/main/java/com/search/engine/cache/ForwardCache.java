package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.search.engine.pojo.Doc;
import com.search.engine.pojo.DocInfo;
import com.search.engine.util.SortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */

public class ForwardCache {

    private static final Logger logger = LoggerFactory.getLogger(ForwardCache.class);


    //docId, List<WordInfo>
    private List<DocInfo> forwardCache = Lists.newArrayList();

    private InvertCache1 invertCache1;

    public ForwardCache() {


        try {

            produceForward("/Users/yjj/m/search_engine/src/main/resources/search_data.txt");

        } catch (Exception e) {

            logger.error("加载文件失败", e);
            e.printStackTrace();
        }


    }


    public List<DocInfo> getForwardCache() {
        return forwardCache;
    }

    public void produceForward(String fileName) {

        List<Doc> docList = SortUtil.fileToDoc(fileName);

        for (Doc doc : docList) {
            List<String> splitWorld = doc.split();
            Multimap<String, Integer> worldPosition = ArrayListMultimap.create();
            DocInfo docInfo = new DocInfo(doc.getDocId(), splitWorld.size(), worldPosition);
            forwardCache.add(docInfo);

            Integer pos = 0;
            for (String world : splitWorld) {
                worldPosition.put(world, pos);
                pos++;
            }
        }

    }


    @Override
    public String toString() {
        return "ForwardCache{\n" + Joiner.on("\n").join(forwardCache) +
                "\n}";
    }


}
