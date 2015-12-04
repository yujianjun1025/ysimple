package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.search.engine.pojo.Doc;
import com.search.engine.pojo.DocInfo;
import com.search.engine.util.SegUtil;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */


@Service
public class RefreshTask {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTask.class);

    private static String FILE_NAME = RefreshTask.class.getResource("/").getPath().concat("search_data.txt");
    private static String serialize_file = "./".concat(String.valueOf(System.currentTimeMillis()).concat(".ivt"));

    private static int flag = 0;
    private static long lastModified = 0;
    private static InvertCache invertCache1 = InvertCache.getInstance();

    @PostConstruct
    private void init() {

        synchronized (RefreshTask.class) {

            logger.info("进入同步块,线程ID {}", Thread.currentThread().getId());
            if (flag == 0) {
                logger.info("flag == 0 执行初始化任务,线程ID {}", Thread.currentThread().getId());
                flag = 1;
                new Thread(new Runnable() {
                    public void run() {

                        while (true) {

                            File file = new File(FILE_NAME);
                            long tmpVersion = file.lastModified();
                            if (tmpVersion > lastModified) {
                                buildIndex(FILE_NAME);
                                lastModified = tmpVersion;

                                //考虑内存还不理想，暂时不支持文件更新自动加载
                                break;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                logger.error("线程休眠出现异常", e);
                            }
                        }

                    }
                }).start();
            } else {
                logger.info("flag == 1 不执行初始化任务,线程ID {}", Thread.currentThread().getId());
            }

            logger.info("退出同步块,线程ID {}", Thread.currentThread().getId());

        }

    }


    public void buildIndex(String fileName) {


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
                List<String> splitWorld = SegUtil.split(doc.getValue());
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

    public void serialize(String fileName) {

        try {

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            Output output = new Output(oos);
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.setRegistrationRequired(false);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.register(InvertCache.class);
            invertCache1.write(kryo, output);

        } catch (Exception e) {
            logger.error("倒排序列化出现异常", e);
        }


    }

}
