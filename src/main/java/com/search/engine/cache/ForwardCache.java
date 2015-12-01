package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Closer;
import com.search.engine.pojo.Doc;
import com.search.engine.pojo.DocInfo;
import com.search.engine.util.SortUtil;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yjj on 15/11/22.
 */

@Component
public class ForwardCache {

    private static final Logger logger = LoggerFactory.getLogger(ForwardCache.class);

    private static String FILE_NAME = "/Users/yjj/m/search_engine/src/main/resources/search_data.txt";

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
                        produceForward(FILE_NAME);
                        lastModified = tmpVersion;
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


    public void produceForward(String fileName) {


        BufferedReader bufferedReader = null;
        try {

            Integer docId = 1;
            bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
            String tmpStr = null;
            while ((tmpStr = bufferedReader.readLine()) != null) {

                tmpStr = tmpStr.trim();
                if (tmpStr.length() == 0) {
                    continue;
                }

                Doc doc = new Doc(docId++, tmpStr);
                List<String> splitWorld = doc.split();
                Integer pos = 0;
                Multimap<String, Integer> worldPosition = ArrayListMultimap.create();
                for (String world : splitWorld) {
                    worldPosition.put(world.intern(), pos);
                    pos++;
                }

                DocInfo docInfo = new DocInfo(doc.getDocId(), splitWorld.size(), worldPosition);
                invertCache1.addDocInfo(docInfo);
            }

        } catch (Exception e) {
            logger.error("读取文件出现异常{}", e);
        } finally {

            try {
                bufferedReader.close();
            } catch (Exception e) {
                logger.error("关闭bufferedReader时出现异常", e);
            }

        }

        logger.info("开始计算rank值");
        long begin = System.currentTimeMillis();
        invertCache1.calculateRank();

        logger.info("计算rerank值耗时{}", System.currentTimeMillis() - begin);

    }


}
