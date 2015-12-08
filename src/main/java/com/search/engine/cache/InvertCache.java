package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.FieldAndDocId;
import com.search.engine.pojo.FieldInfo;
import com.search.engine.pojo.TermInfo;
import com.search.engine.util.SegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/29.
 * 倒排缓存类
 */
public class InvertCache implements KryoSerializable {

    private static final long serialVersionUID = -2385906232920579818L;
    private static final Logger logger = LoggerFactory.getLogger(InvertCache.class);

    private int WORD_COUNT = 0;
    private int DOC_COUNT = 0;
    private Map<Integer, List<TermInfo>> invertCache = Maps.newHashMap();
    private Map<String, Integer> str2int = Maps.newHashMap();

    public static InvertCache getInstance() {
        return InvertCache2Holder.instance;
    }

    public List<TermInfo> getTermInfo(Integer stringCode, int field) {
        List<TermInfo> res = invertCache.get(stringCode);

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

                List<TermInfo> termInfoList = invertCache.get(stringCode);
                if (termInfoList == null) {
                    termInfoList = Lists.newArrayList();
                    invertCache.put(stringCode, termInfoList);
                }

                int index = Collections.binarySearch(termInfoList, docId);
                if (index < 0) {

                    int tf = (entry.getValue().size() * 1000) / fieldInfo.getWorldCount();
                    TermInfo termInfo = new TermInfo(docId, fieldId, entry.getValue(), tf);

                    termInfoList.add(Math.abs(index + 1), termInfo);
                }
            }


        }


    }

    public void calculateRank() {

        long begin = System.currentTimeMillis();
        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {

            double idf = Math.log(DOC_COUNT / entry.getValue().size());
            for (TermInfo termInfo : entry.getValue()) {
                double rank = idf * termInfo.getTf();
                termInfo.setRank(rank);
            }
        }
        logger.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        logger.info("str2int size:{},  invertCache size:{}", new Object[]{str2int.size(), invertCache.size()});

    }

    public void write(Kryo kryo, Output output) {

        output.writeInt(WORD_COUNT);
        output.writeInt(DOC_COUNT);
        output.writeInt(str2int.size());
        for (Map.Entry<String, Integer> entry : str2int.entrySet()) {
            output.writeString(entry.getKey());
            output.writeInt(entry.getValue());
        }

        output.writeInt(invertCache.size());
        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {
            output.writeInt(entry.getKey());
            output.writeInt(entry.getValue().size());
            for (TermInfo termInfo : entry.getValue()) {
                termInfo.write(kryo, output);
            }
        }
    }

    public void read(Kryo kryo, Input input) {

        WORD_COUNT = input.readInt();
        DOC_COUNT = input.readInt();
        int str2intSize = input.readInt();
        for (int i = 0; i < str2intSize; i++) {
            str2int.put(input.readString(), input.readInt());
        }

        int cacheSize = input.readInt();
        for(int i = 0; i < cacheSize; i++){

            int termCode = input.readInt();
            int termSize = input.readInt();
            List<TermInfo> termInfoList = Lists.newArrayList();
            for(int j =0; j < termSize; j++){
                TermInfo termInfo = new TermInfo();
                termInfo.read(kryo, input);
                termInfoList.add(termInfo);
            }
            invertCache.put(termCode, termInfoList);
        }

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(Joiner.on(" ").join(entry.getValue())).append("\n");
        }

        return "InvertCache{\n" + stringBuilder.toString() + '}';
    }

    public static final class InvertCache2Holder {
        private static final InvertCache instance = new InvertCache();
    }

}
