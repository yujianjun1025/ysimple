package com.search.engine.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.FieldAndDocId;
import com.search.engine.pojo.FieldInfo;
import com.search.engine.protobuf.InvertPro;
import com.search.engine.util.SegUtil;
import com.search.engine.util.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/12/11.
 * 倒排索引类
 */
public class InvertCache {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache.class);

    private static String TERM_FILE = RefreshTask.class.getResource("/").getPath().concat("termInfo.dat");
    Map<Integer, Position> termCodeAndPosition = Maps.newHashMap();
    int offset = 0;
    FileChannel fc = null;
    private int WORD_COUNT = 0;
    private int DOC_COUNT = 0;
    private List<List<InvertPro.TermInOneDoc>> invertCache = Lists.newArrayList();
    private Map<String, Integer> str2int = Maps.newHashMap();

    public static InvertCache getInstance() {
        return InvertCacheHolder.instance;
    }

    public List<InvertPro.TermInOneDoc> getTermInfo(Integer termCode, int field) {
//        List<TermInOneDoc> res = invertCache.get(termCode);

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
                int length = invertCache.size();

                if (stringCode >= length) {
                    termInOneDocList = Lists.newArrayList();
                    invertCache.add(stringCode, termInOneDocList);
                }

                termInOneDocList = invertCache.get(stringCode);
                if (termInOneDocList == null) {
                    termInOneDocList = Lists.newArrayList();
                    invertCache.add(stringCode, termInOneDocList);
                }

                int index = Collections.binarySearch(termInOneDocList, docId);
                if (index < 0) {

                    int tf = (entry.getValue().size() * 1000) / fieldInfo.getWorldCount();
                    InvertPro.TermInOneDoc.Builder builder = InvertPro.TermInOneDoc.newBuilder();

                    builder.setDocId(docId);
                    builder.setField(fieldId);
                    builder.addAllPositions(entry.getValue());
                    builder.setTf(tf);
                    InvertPro.TermInOneDoc termInOneDoc = builder.build();
                    termInOneDocList.add(Math.abs(index + 1), termInOneDoc);
                }
            }

        }


    }

    public void calculateRank() {

        long begin = System.currentTimeMillis();
        for (List<InvertPro.TermInOneDoc> entry : invertCache) {

            double idf = Math.log(DOC_COUNT / entry.size());
            for (InvertPro.TermInOneDoc termInOneDoc : entry) {
                double rank = idf * termInOneDoc.getTf();

                //todo
                //termInOneDoc.setRank(rank);
            }
        }
        logger.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        logger.info("str2int size:{},  invertCache size:{}", new Object[]{str2int.size(), invertCache.size()});

    }

    private List<InvertPro.TermInOneDoc> getTermInfoListByTermCode(Integer termCode) {

        try {

            Position position = termCodeAndPosition.get(termCode);
            long begin = System.nanoTime();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, position.offset, position.size);
            long end = System.nanoTime();

            logger.info("fc.map()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);

            begin = end;
            byte[] bytes = new byte[position.size];
            byteBuffer.get(bytes, 0, position.size);
            end = System.nanoTime();
            logger.info("byteBuffer.get()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);

            begin = end;
            List<InvertPro.TermInOneDoc> res = SerializeUtil.deserialize(bytes);
            end = System.nanoTime();
            logger.info("deserialize()耗时{}毫秒", (1.0 * (end - begin)) / 1000000);
            return res;

        } catch (Exception e) {
            logger.error("序列化出现异常{}", e);
        }

        return Lists.newArrayList();


    }

    public void mem2disk() {

        try {
            FileOutputStream fos = new FileOutputStream(TERM_FILE);
            for (int i = 0; i < invertCache.size(); i++) {

                byte[] bytes = SerializeUtil.serialize(invertCache.get(i));
                int size = bytes.length;
                fos.write(bytes);
                Position position = new Position(offset, size);
                termCodeAndPosition.put(i, position);
                offset += size;
            }

            if (fos != null) {
                fos.close();
            }

            //磁盘映射到内存
            fc = new FileInputStream(TERM_FILE).getChannel();

            //清楚内存中的内容
            invertCache.clear();
            invertCache = null;

        } catch (Exception e) {

            logger.error("内存刷入磁盘出现异常", e);
        }


    }

    public static final class InvertCacheHolder {
        private static final InvertCache instance = new InvertCache();
    }

    class Position {
        private int offset;
        private int size;

        public Position(int offset, int size) {
            this.offset = offset;
            this.size = size;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

}
