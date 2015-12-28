package com.search.indexserver.timetask;

import com.search.indexserver.cache.InvertCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by yjj on 15/12/28.
 */

@Repository
public class RefreshTask {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTask.class);
    private static final String TERM_FILE = RefreshTask.class.getResource("/").getPath().concat("termInfo.dat");
    private static final String STR2INT_FILE = RefreshTask.class.getResource("/").getPath().concat("str2int.txt");
    private static final String POSITION_FILE = RefreshTask.class.getResource("/").getPath().concat("position.txt");
    private static final String VERSION_FILE = RefreshTask.class.getResource("/").getPath().concat("version.txt");


    private InvertCache invertCache = new InvertCache();
    private long lastVersion = 0;

    public void refresh() {

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

            logger.error("反序列化索引文件出现异常", e);

        }

    }

    public InvertCache getInvertCache() {
        return invertCache;
    }
}
