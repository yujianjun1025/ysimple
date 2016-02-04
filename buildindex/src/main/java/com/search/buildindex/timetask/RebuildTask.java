package com.search.buildindex.timetask;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.search.indexserver.cache.InvertCache;
import com.search.indexserver.pojo.Doc;
import com.search.indexserver.pojo.DocInfo;
import com.search.indexserver.util.SegUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yjj on 15/11/22
 * 定时更新索引任务、虚拟化倒排文件，反虚拟化倒排文件任务
 */

@Service
@Slf4j
public class RebuildTask {

    private static final String TXT_FILE = "/tmp/search/data/search_data.txt";
    private static final String TERM_FILE = "/tmp/search/invert/termInfo.dat";
    private static final String STR2INT_FILE = "/tmp/search/invert/str2int.txt";
    private static final String POSITION_FILE = "/tmp/search/invert/position.txt";
    private static final String VERSION_FILE = "/tmp/search/invert/version.txt";
    private static volatile long LAST_MODIFY = 0;
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public RebuildTask() {

    }

    public boolean rebuild() {

        if (!readWriteLock.writeLock().tryLock()) {
            log.info("重建索引任务还在运行， 稍后重试");
            return false;
        }

        InvertCache invertCache = new InvertCache();
        try {
            File file = new File(TXT_FILE);
            long tmpLastModify = file.lastModified();
            if (LAST_MODIFY >= tmpLastModify) {
                return true;
            }

            buildIndex(invertCache, TXT_FILE);
            LAST_MODIFY = tmpLastModify;
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(VERSION_FILE)));
            writer.write(String.valueOf(LAST_MODIFY));
            writer.close();

        } catch (Exception e) {
            log.error("序列化文件时出现异常", e);
            return false;
        } finally {
            log.info("索引任务重建完成");
            readWriteLock.writeLock().unlock();
        }
        return true;
    }


    public void buildIndex(InvertCache invertCache, String fileName) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("开始生成倒排");
        buildInvert(invertCache, fileName);
        log.info("生成倒排耗时{}毫秒", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        stopwatch.reset().start();
        log.info("开始计算rank值");
        invertCache.calculateRank();
        log.info("计算rerank值耗时{}毫秒", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        stopwatch.reset().start();
        log.info("开始序列化");
        try {
            InvertCache.mem2disk(invertCache, STR2INT_FILE, POSITION_FILE, TERM_FILE);
        } catch (Exception e) {
            log.error("内存序列化到磁盘出现异常", e);
        }
        log.info("序列化完成耗时{}毫秒", stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

    private void buildInvert(InvertCache invertCache, String fileName) {
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
            log.error("读取文件出现异常{}", e);
        } finally {

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                log.error("关闭bufferedReader时出现异常", e);
            }

        }
    }

}
