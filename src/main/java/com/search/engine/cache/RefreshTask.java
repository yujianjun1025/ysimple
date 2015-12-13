package com.search.engine.cache;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.search.engine.pojo.Doc;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.Node;
import com.search.engine.util.SegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/22.
 * 定时更新索引任务、虚拟化倒排文件，反虚拟化倒排文件任务
 */
@Service
public class RefreshTask {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTask.class);
    private static final Object object = new Object();
    private static String SOURCE_FILE = RefreshTask.class.getResource("/").getPath().concat("search_data.txt");
    private static String FORWARD_FILE = RefreshTask.class.getResource("/").getPath().concat("forward.dat");
    private static long lastModified = 0;
    private static InvertCache invertCache = InvertCache.getInstance();
    private static ForwardCache forwardCache = ForwardCache.getInstance();
    private static int flag = 1;
    @PostConstruct
    private void init() {

        synchronized (RefreshTask.class) {

            if (flag == 1) {
                flag = 0;
                logger.info("启动初始化倒排索引任务");


                try {
                    File file = new File(SOURCE_FILE);
                    buildIndex(SOURCE_FILE);
                    //考虑内存还不理想，暂时不支持文件更新自动加载
                } catch (Exception e) {

                    logger.error("生成索引过程中出现异常", e);
                }


              /*  new Thread(new Runnable() {

                    public void run() {

                        try {
                            do {
                                File file = new File(SOURCE_FILE);
                                long tmpVersion = file.lastModified();
                                if (tmpVersion > lastModified) {
                                    buildIndex(SOURCE_FILE);
                                    lastModified = tmpVersion;

                                    //考虑内存还不理想，暂时不支持文件更新自动加载
                                    break;
                                }
                                Thread.sleep(1000);
                            } while (true);

                        } catch (Exception e) {

                            logger.error("生成索引过程中出现异常", e);
                        }


                    }
                }).start();*/
            } else {
                logger.info("启动初始化倒排索引任务失败， 已经初始化执行");
            }
        }



    }


    public void buildIndex(String fileName) throws IOException {

        logger.info("开始准备生成正排");
        forwardCache.mem2diskBefore(FORWARD_FILE);

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
        logger.info("开始映射文件");
        forwardCache.mem2diskEnd(FORWARD_FILE);
        end = System.currentTimeMillis();
        logger.info("映射文件完成耗时{}毫秒", end - begin);

    }

    private void buildInvert(String fileName) {
        BufferedReader bufferedReader = null;
        try {

            Integer docId = 0;
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
                Multimap<Integer, Integer> worldPosition = ArrayListMultimap.create();
                for (String world : splitWorld) {
                    worldPosition.put(invertCache.putIfAbsent(world), pos++);
                }


                //默认fieldId = 1
                DocInfo docInfo = new DocInfo(doc.getDocId(), 1, splitWorld.size(), worldPosition);
                invertCache.addDocInfo(docInfo);

                List<Node> nodeList = Lists.newArrayList();
                for (Map.Entry<Integer, Collection<Integer>> entry : worldPosition.asMap().entrySet()) {
                    Node node = new Node(entry.getKey(), Lists.newArrayList(entry.getValue()), 0.0);
                    nodeList.add(node);
                }
                forwardCache.addDocument(docInfo.getDocId(), nodeList);
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
