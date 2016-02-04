package com.search.indexserver.timetask;

import com.search.indexserver.cache.InvertCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yjj on 15/12/28.
 */

@Repository
@Slf4j
public class RefreshTask {


    private static final String TERM_FILE = "/tmp/search/invert/termInfo.dat";
    private static final String STR2INT_FILE = "/tmp/search/invert/str2int.txt";
    private static final String POSITION_FILE = "/tmp/search/invert/position.txt";
    private static final String VERSION_FILE = "/tmp/search/invert/version.txt";
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private InvertCache invertCache = new InvertCache();
    private long lastVersion = 0;

    @PostConstruct
    public void refresh() {


        if (!readWriteLock.writeLock().tryLock()) {
            log.info("重建索引任务还在运行, 稍后重试");
            return;
        }

        log.info("开始加载倒排数据");


        InvertCache tmpCache = null;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(new File(VERSION_FILE)));
            long tmpVersion = Long.parseLong(reader.readLine());
            if (lastVersion >= tmpVersion) {
                return;
            }

            tmpCache = InvertCache.disk2mem(STR2INT_FILE, POSITION_FILE, TERM_FILE);
            lastVersion = tmpVersion;
            invertCache = tmpCache;

        } catch (Exception e) {

            log.error("反序列化索引文件出现异常", e);

        }

        log.info("加载倒排数据完成");

    }

    public InvertCache getInvertCache() {
        return invertCache;
    }
}
