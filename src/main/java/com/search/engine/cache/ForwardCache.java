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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yjj on 15/11/22.
 */

@Component
public class ForwardCache {

    private static final Logger logger = LoggerFactory.getLogger(ForwardCache.class);

    private static String FILE_NAME = "/Users/yjj/m/search_engine/src/main/resources/search_data.txt";

    private static int ARRAY_SIZE = 1000;
    //docId, List<WordInfo>
    private List<DocInfo>[] forwardCache = new List[ARRAY_SIZE];

    private long lastModified = 0;

    @Resource
    private InvertCache1 invertCache1;

    AtomicInteger atomicInteger = new AtomicInteger(0);

    @PostConstruct
    private void init() {

        for (int i = 0; i < ARRAY_SIZE; i++) {

            forwardCache[i] = Lists.newArrayList();
        }


        new Thread(new Runnable() {
            public void run() {

                while (true) {

                    File file = new File(FILE_NAME);
                    long tmpVersion = file.lastModified();
                    if (tmpVersion > lastModified) {

                        long begin = System.currentTimeMillis();
                        logger.info("准备加载新文件");
                        produceForward(FILE_NAME);
                        long end = System.currentTimeMillis();
                        logger.info("生成正排完成, 耗时{}", end  - begin);
                        begin = end;
                        invertCache1.buildInvert(forwardCache);
                        lastModified = tmpVersion;
                        logger.info("正排生成倒排耗时:{}", new Object[]{System.currentTimeMillis() - begin,atomicInteger});
                    }

                   // invertCache1.print();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("线程休眠出现异常", e);
                    }
                }

            }
        }).start();


    }


    public void produceForward(String fileName) {

        List<Doc> docList = SortUtil.fileToDoc(fileName);

        for (Doc doc : docList) {
            List<String> splitWorld = doc.split();
            Multimap<String, Integer> worldPosition = ArrayListMultimap.create();
            DocInfo docInfo = new DocInfo(doc.getDocId(), splitWorld.size(), worldPosition);
            forwardCache[doc.getDocId() % ARRAY_SIZE].add(docInfo);

            Integer pos = 0;
            for (String world : splitWorld) {
                worldPosition.put(world.intern(), pos);
                pos++;
            }
        }

    }

    private void print() {

        for (int i = 0; i < ARRAY_SIZE; i++) {
            logger.error(Joiner.on("\n").join(forwardCache));
        }

    }

    @Override
    public String toString() {


        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            stringBuilder.append(Joiner.on("\n").join(forwardCache)).append("\n");
        }

        return "ForwardCache{\n" + stringBuilder +
                "\n}";
    }


}
