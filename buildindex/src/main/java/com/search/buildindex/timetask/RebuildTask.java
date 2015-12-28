package com.search.buildindex.timetask;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.search.indexserver.cache.InvertCache;
import com.search.indexserver.pojo.Doc;
import com.search.indexserver.pojo.DocInfo;
import com.search.indexserver.util.SegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yjj on 15/11/22.
 * 定时更新索引任务、虚拟化倒排文件，反虚拟化倒排文件任务
 */

@Service
public class RebuildTask {

    public RebuildTask() {

    }

    private static final Logger logger = LoggerFactory.getLogger(RebuildTask.class);
    private static final String TXT_FILE = RebuildTask.class.getResource("/").getPath().concat("search_data.txt");
    private static final String TERM_FILE = InvertCache.class.getResource("/").getPath().concat("termInfo.dat");
    private static volatile long LAST_MODIFY = 0;
    private static InvertCache invertCache = InvertCache.getInstance();
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public boolean rebuild() {

        if (!readWriteLock.writeLock().tryLock()) {
            logger.info("重建索引任务还在运行， 稍后重试");
            return false;
        }

        try {
            File file = new File(TXT_FILE);
            long tmpLastModify = file.lastModified();
            if (LAST_MODIFY < tmpLastModify) {
                buildIndex(TXT_FILE);
                LAST_MODIFY = tmpLastModify;
            }

        } finally {
            logger.info("索引任务重建完成");
            readWriteLock.writeLock().unlock();
        }

        return true;
    }


    public void buildIndex(String fileName) {


        long begin = System.currentTimeMillis();
        logger.info("开始生成倒排");
        buildInvert(fileName);
        long end = System.currentTimeMillis();
        logger.info("生成倒排耗时{}毫秒", end - begin);

        begin = end;
        logger.info("开始计算rank值");
        invertCache.calculateRank();
        end = System.currentTimeMillis();
        logger.info("计算rerank值耗时{}毫秒", end - begin);

        begin = end;
        logger.info("开始序列化");
        invertCache.mem2disk(TERM_FILE);
        end = System.currentTimeMillis();
        logger.info("序列化完成耗时{}毫秒", end - begin);

    }

    private void buildInvert(String fileName) {
        BufferedReader bufferedReader = null;
        try {

            Integer docId = 1;
            bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
            String tmpStr;
            while ((tmpStr = bufferedReader.readLine()) != null) {

                tmpStr = tmpStr.trim();
                if (tmpStr.length() == 0) {
                    continue;
                }

                Doc doc = new Doc(docId++, tmpStr);
                List<String> splitWorld = SegUtil.split(doc.getValue());
                Integer pos = 0;
                Multimap<String, Integer> worldPosition = ArrayListMultimap.create();
                for (String world : splitWorld) {
                    worldPosition.put(world.intern(), pos);
                    pos++;
                }

                //默认fieldId = 1
                DocInfo docInfo = new DocInfo(doc.getDocId(), 1, splitWorld.size(), worldPosition);
                invertCache.addDocInfo(docInfo);
            }

        } catch (Exception e) {
            logger.error("读取文件出现异常{}", e);
        } finally {

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                logger.error("关闭bufferedReader时出现异常", e);
            }

        }
    }

}
