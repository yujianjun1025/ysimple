package com.search.indexserver.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.search.indexserver.pojo.DocInfo;
import com.search.indexserver.pojo.FieldAndDocId;
import com.search.indexserver.pojo.FieldInfo;
import com.search.indexserver.pojo.Position;
import com.search.indexserver.protobuf.InvertPro;
import com.search.indexserver.util.SegUtil;
import com.search.indexserver.util.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yjj on 15/12/11.
 * 倒排索引类
 */
public class InvertCache {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache.class);
    private int OFFSET = 0;
    private FileChannel fc = null;
    private int WORD_COUNT = 0;
    private int DOC_COUNT = 0;
    private Map<String, Integer> str2int = Maps.newHashMap();
    private Map<Integer, Position> termCodeAndPosition = Maps.newHashMap();
    private List<List<InvertPro.TermInOneDoc>> cache = Lists.newArrayList();


    public List<InvertPro.TermInOneDoc> getTermInfo(Integer termCode, int field) {

        long begin = System.nanoTime();
        List<InvertPro.TermInOneDoc> res = getTermInfoListByTermCode(termCode);
        long end = System.nanoTime();
        logger.info("getTermInfoListByTermCode(termCode)耗时{}毫秒", (1.0 * (end - begin)) / 1000000);

        if (res == null) {
            return Lists.newArrayList();
        }

        FieldAndDocId min = new FieldAndDocId(field, Integer.MIN_VALUE);
        int minIndex = Collections.binarySearch(res, min);
        FieldAndDocId max = new FieldAndDocId(field, Integer.MAX_VALUE);
        int maxIndex = Collections.binarySearch(res, max);


        minIndex = Math.abs(minIndex + 1);
        maxIndex = Math.abs(maxIndex + 1);

        if (minIndex >= res.size()) {
            return Lists.newArrayList();
        }

        if (maxIndex > res.size()) {
            maxIndex = res.size();
        }

        if (Integer.compare(minIndex, maxIndex) == 0) {
            return Lists.newArrayList();
        }


        return res.subList(minIndex, maxIndex);
    }

    private Integer str2Int(String string) {
        return str2int.get(String.valueOf(string));
    }

    public List<Integer> getTermCodeListByQuery(String query) {

        List<Integer> res = Lists.newArrayList();
        for (String string : SegUtil.split(query)) {

            Integer string2code = str2Int(string);
            if (string2code == null) {
                return Lists.newArrayList();
            }
            res.add(string2code);
        }
        return res;
    }

    public void addDocInfo(DocInfo docInfo) {

        DOC_COUNT++;

        int docId = docInfo.getDocId();
        for (FieldInfo fieldInfo : docInfo.getField()) {
            int fieldId = fieldInfo.getField();
            for (Map.Entry<String, Collection<Integer>> entry : fieldInfo.getWorldPosition().asMap().entrySet()) {

                String world = entry.getKey();
                if (!str2int.containsKey(world.intern())) {
                    str2int.put(world.intern(), WORD_COUNT++);
                }
                Integer stringCode = str2int.get(world);

                List<InvertPro.TermInOneDoc> termInOneDocList;
                int length = cache.size();

                if (stringCode >= length) {
                    termInOneDocList = Lists.newArrayList();
                    cache.add(stringCode, termInOneDocList);
                }

                termInOneDocList = cache.get(stringCode);
                if (termInOneDocList == null) {
                    termInOneDocList = Lists.newArrayList();
                    cache.add(stringCode, termInOneDocList);
                }

                int index = Collections.binarySearch(termInOneDocList, docId);
                if (index < 0) {

                    int tf = (entry.getValue().size() * 1000) / fieldInfo.getWorldCount();
                    InvertPro.TermInOneDoc termInOneDoc = InvertPro.TermInOneDoc.newBuilder().
                            setTf(tf).
                            setDocId(docId).
                            setField(fieldId).
                            addAllPositions(entry.getValue()).
                            build();
                    termInOneDocList.add(Math.abs(index + 1), termInOneDoc);
                }
            }

        }


    }

    public void calculateRank() {

        long begin = System.currentTimeMillis();
        for (List<InvertPro.TermInOneDoc> entry : cache) {

            double idf = Math.log(DOC_COUNT / entry.size());
            for (InvertPro.TermInOneDoc termInOneDoc : entry) {
                double rank = idf * termInOneDoc.getTf();
                termInOneDoc.toBuilder().setRank(rank);
            }
        }
        logger.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        logger.info("str2int size:{},  cache size:{}", new Object[]{str2int.size(), cache.size()});

    }

    private List<InvertPro.TermInOneDoc> getTermInfoListByTermCode(Integer termCode) {

        try {

            Position position = termCodeAndPosition.get(termCode);
            logger.info("开始位置:{}K, 大小:{}K", (1.0 * position.getOffset()) / 1024, (1.0 * position.getTotalSize()) / 1024);

            long begin = System.nanoTime();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, position.getOffset(), position.getTotalSize());
            long end = System.nanoTime();

            logger.info("fc.map()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);

            begin = end;
            byte[] bytes = new byte[position.getTotalSize()];
            byteBuffer.get(bytes, 0, position.getTotalSize());
            end = System.nanoTime();
            logger.info("byteBuffer.get()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);

            begin = end;

            List<InvertPro.TermInOneDoc> res = parallelDeserialize(position, bytes);
            end = System.nanoTime();
            logger.info("deserializeByProto()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);
            return res;

        } catch (Exception e) {
            logger.error("序列化出现异常{}", e);
        }

        return Lists.newArrayList();
    }

    private static final ExecutorService DESERIALIZE_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private List<InvertPro.TermInOneDoc> parallelDeserialize(Position position, byte[] bytes) throws InvalidProtocolBufferException, InterruptedException {

        final List<InvertPro.TermInOneDoc> res = Lists.newArrayList();
        final Object object = new Object();
        final Map<Integer, List<InvertPro.TermInOneDoc>> map = Maps.newTreeMap();
        int lastByteSize = 0;
        final CountDownLatch countDownLatch = new CountDownLatch(position.getSize().size());
        for (Integer byteSize : position.getSize()) {
            int byteLength = byteSize - lastByteSize;
            final byte[] tmpBytes = new byte[byteLength];
            final int key = lastByteSize;
            System.arraycopy(bytes, lastByteSize, tmpBytes, 0, byteLength);
            DESERIALIZE_THREAD_POOL.submit(new Runnable() {
                public void run() {

                    try {
                        List<InvertPro.TermInOneDoc> tmpList = SerializeUtil.deserializeByProto(tmpBytes);
                        synchronized (object) {
                            map.put(key, tmpList);
                        }
                    } catch (Exception e) {
                        logger.error("反序列化出现异常", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });

            lastByteSize = byteSize;
            //todo
        }

        countDownLatch.await();
        for (Map.Entry<Integer, List<InvertPro.TermInOneDoc>> entry : map.entrySet()) {
            res.addAll(entry.getValue());
        }

        return res;
    }

    public static void mem2disk(InvertCache invertCache, String str2IntFileName, String positionFileName, String termFileName) throws Exception {

        BufferedWriter str2IntWrite = new BufferedWriter(new FileWriter(new File(str2IntFileName)));
        BufferedWriter positionWrite = new BufferedWriter(new FileWriter(new File(positionFileName)));
        FileOutputStream fos = new FileOutputStream(termFileName);

        try {

            String str2IntJson = JSON.toJSONString(invertCache.str2int);
            str2IntWrite.write(str2IntJson);

            for (int i = 0; i < invertCache.cache.size(); i++) {

                int size = 0;
                Position position = new Position(invertCache.OFFSET);
                invertCache.termCodeAndPosition.put(i, position);
                //2000只是意淫的一个值
                for (List<InvertPro.TermInOneDoc> termInOneDocList : Lists.partition(invertCache.cache.get(i), 2000)) {
                    byte[] bytes = SerializeUtil.serializeByProto(termInOneDocList);
                    size += bytes.length;
                    invertCache.OFFSET += bytes.length;
                    position.addSize(size);
                    fos.write(bytes);
                }
            }

            String positionJson = JSON.toJSONString(invertCache.termCodeAndPosition);
            positionWrite.write(positionJson);


        } finally {
            str2IntWrite.close();
            positionWrite.close();
            fos.close();
        }
    }

    public static InvertCache disk2mem(String str2IntFileName, String positionFileName, String termFileName) throws Exception {

        InvertCache ret = new InvertCache();
        BufferedReader str2IntReader = new BufferedReader(new FileReader(new File(str2IntFileName)));
        BufferedReader positionReader = new BufferedReader(new FileReader(new File(positionFileName)));

        try {

            String oneLine = str2IntReader.readLine();
            ret.str2int = JSON.parseObject(oneLine, new TypeReference<Map<String, Integer>>() {
            });

            oneLine = positionReader.readLine();
            ret.termCodeAndPosition = JSON.parseObject(oneLine, new TypeReference<Map<Integer, Position>>() {
            });

            //磁盘映射到内存
            ret.fc = new FileInputStream(termFileName).getChannel();

        } finally {
            str2IntReader.close();
            positionReader.close();
        }

        return ret;
    }

}
