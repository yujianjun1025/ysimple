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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */

@Component
public class ForwardCache {

    private static final Logger logger = LoggerFactory.getLogger(ForwardCache.class);

    private static String FILE_NAME = "/Users/yjj/m/search_engine/src/main/resources/search_data.txt";


    //docId, List<WordInfo>
    private List<DocInfo> forwardCache = Lists.newArrayList();
    private long lastModified = 0;

    @Resource
    private InvertCache1 invertCache1;

    @PostConstruct
    private void init() {

        new Thread(new Runnable() {
            public void run() {

                while (true) {

                    File file = new File(FILE_NAME);
                    long tmpVersion = file.lastModified();
                    if (tmpVersion > lastModified) {

                        long begin = System.currentTimeMillis();
                        produceForward(FILE_NAME);
                        invertCache1.buildInvert(forwardCache);
                        lastModified = tmpVersion;
                        logger.info("加载文件:{}成功,版本号:{}, 耗时:{}", new Object[]{FILE_NAME, lastModified, System.currentTimeMillis() - begin});
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("线程休眠出现异常", e);
                    }
                }

            }
        }).start();
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
