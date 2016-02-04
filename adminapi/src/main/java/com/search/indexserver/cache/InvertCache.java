package com.search.indexserver.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.search.indexserver.pojo.*;
import com.search.indexserver.util.SegUtil;
import com.search.indexserver.util.SerializeUtil;
import lombok.extern.slf4j.Slf4j;

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
import java.util.concurrent.TimeUnit;

/**
 * Created by yjj on 15/12/11.
 * 倒排索引类
 */
@Slf4j
public class InvertCache {


    private static final ExecutorService DESERIALIZE_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private int OFFSET = 0;
    private FileChannel fc = null;
    private int WORD_COUNT = 0;
    private int DOC_COUNT = 0;
    private Map<String, Integer> str2int = Maps.newHashMap();
    private Map<Integer, Position> termCodeAndPosition = Maps.newHashMap();
    private List<List<TermInOneDoc>> cache = Lists.newArrayList();

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

                int docCount = 0;
                //2000只是意淫的一个值
                for (List<TermInOneDoc> termInOneDocList : Lists.partition(invertCache.cache.get(i), 2000)) {
                    byte[] bytes = SerializeUtil.serializeBySelf(termInOneDocList);
                    size += bytes.length;
                    invertCache.OFFSET += bytes.length;
                    position.addSize(size);
                    fos.write(bytes);
                    docCount += termInOneDocList.size();
                }

                position.setDocCount(docCount);
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

    public List<TermInOneDoc> getTermInfo(Integer termCode, int field) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<TermInOneDoc> res = getTermInfoListByTermCode(termCode);
        log.info("getTermInfoListByTermCode(termCode)耗时{}毫秒", (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS)) / 1000000);

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

                List<TermInOneDoc> termInOneDocList;
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
                    TermInOneDoc termInOneDoc = new TermInOneDoc();

                    termInOneDoc.setTf(tf);
                    termInOneDoc.setDocId(docId);
                    termInOneDoc.setField(fieldId);
                    termInOneDoc.setPositions(Lists.newArrayList(entry.getValue()));
                    termInOneDocList.add(Math.abs(index + 1), termInOneDoc);
                }
            }

        }


    }

    public void calculateRank() {

        long begin = System.currentTimeMillis();
        for (List<TermInOneDoc> entry : cache) {

            double idf = Math.log(DOC_COUNT / entry.size());
            for (TermInOneDoc termInOneDoc : entry) {
                double rank = idf * termInOneDoc.getTf();
                termInOneDoc.setRank(rank);
            }
        }
        log.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        log.info("str2int size:{},  cache size:{}", new Object[]{str2int.size(), cache.size()});

    }

    private List<TermInOneDoc> getTermInfoListByTermCode(Integer termCode) {

        try {

            Position position = termCodeAndPosition.get(termCode);
            log.info("开始位置:{}, 大小:{} byte ", position.getOffset(), position.getTotalSize());


            Stopwatch stopwatch = Stopwatch.createStarted();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, position.getOffset(), position.getTotalSize());
            long end = System.nanoTime();

            log.info("fc.map()耗时{}毫秒", (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS)) / 1000000);


            stopwatch.reset().start();
            byte[] bytes = new byte[position.getTotalSize()];
            byteBuffer.get(bytes, 0, position.getTotalSize());
            end = System.nanoTime();
            log.info("byteBuffer.get()耗时{}毫秒", (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS)) / 1000000);

            stopwatch.reset().start();
            List<TermInOneDoc> res = parallelDeserialize(position, bytes);
            end = System.nanoTime();
            log.info("deserializeBySelf()耗时{}毫秒", (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS)) / 1000000);
            return res;

        } catch (Exception e) {
            log.error("序列化出现异常{}", e);
        }

        return Lists.newArrayList();
    }

    private List<TermInOneDoc> parallelDeserialize(Position position, byte[] bytes) throws InvalidProtocolBufferException, InterruptedException {

        final List<TermInOneDoc> res = Lists.newArrayList();
        final Object object = new Object();
        final Map<Integer, List<TermInOneDoc>> map = Maps.newTreeMap();
        int lastByteSize = 0;
        final CountDownLatch countDownLatch = new CountDownLatch(position.getSegmentLength().size());
        for (Integer byteSize : position.getSegmentLength()) {
            int byteLength = byteSize - lastByteSize;
            if (byteLength <= 0) {
                countDownLatch.countDown();
                continue;
            }

            final byte[] tmpBytes = new byte[byteLength];
            final int key = lastByteSize;
            System.arraycopy(bytes, lastByteSize, tmpBytes, 0, byteLength);
            DESERIALIZE_THREAD_POOL.submit(new Runnable() {
                public void run() {

                    try {
                        List<TermInOneDoc> tmpList = SerializeUtil.deserializeBySelf(tmpBytes);
                        synchronized (object) {
                            map.put(key, tmpList);
                        }
                    } catch (Exception e) {
                        log.error("反序列化出现异常", e);

                        log.error("tmpBytes size ", tmpBytes.length);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });

            lastByteSize = byteSize;
            //todo
        }

        countDownLatch.await();
        for (Map.Entry<Integer, List<TermInOneDoc>> entry : map.entrySet()) {
            res.addAll(entry.getValue());
        }

        return res;
    }

}
