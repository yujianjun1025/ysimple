package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.search.engine.pojo.DocInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/29.
 */
public class InvertCache2 implements KryoSerializable {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache1.class);
    private static Integer DOC_COUNT = 0;
    private Map<Integer, List<TermInfo>> invertCache = Maps.newHashMap();
    private Map<String, Integer> str2int = Maps.newHashMap();
    private int WORD_COUNT = 0;

    public static InvertCache2 getInstance() {
        return InvertCache2Holder.instance;
    }

    public Integer getStringCode(Character character) {

        Integer res = str2int.get(String.valueOf(character));
        return res != null ? res : -1;

    }

    public void addDocInfo(DocInfo docInfo) {

        DOC_COUNT++;

        for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {


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

            TermInfo termInfo = null;
            int index = Collections.binarySearch(termInfoList, docInfo.getDocId());
            if (index < 0) {

                int tf = (entry.getValue().size() * 1000) / docInfo.getWorldCount();
                termInfo = new TermInfo(docInfo.getDocId(), entry.getValue(), tf);
                termInfoList.add(Math.abs(index + 1), termInfo);
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

    }

    public void read(Kryo kryo, Input input) {

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(Joiner.on(" ").join(entry.getValue())).append("\n");
        }

        return "InvertCache2{\n" + stringBuilder.toString() + '}';
    }

    public static class TermInfo implements Comparable<Integer> {

        private int docId;
        private List<Integer> posList;
        private int tf;
        private double rank;

        public TermInfo(Integer docId, Collection<Integer> posList, int tf) {
            this.docId = docId;
            this.posList = Lists.newArrayList(posList);
            this.tf = tf;
        }

        public int compareTo(Integer o) {
            return Ints.compare(0, docId);
        }

        public int getDocId() {
            return docId;
        }

        public void setDocId(int docId) {
            this.docId = docId;
        }

        public List<Integer> getPosList() {
            return posList;
        }

        public void setPosList(List<Integer> posList) {
            this.posList = posList;
        }


        public int getTf() {
            return tf;
        }

        public void setTf(int tf) {
            this.tf = tf;
        }

        public double getRank() {
            return rank;
        }

        public void setRank(double rank) {
            this.rank = rank;
        }

        @Override
        public String toString() {
            return "InvertDocInfo{" +
                    "docId=" + docId +
                    ", posList=" + Joiner.on(" ").join(posList) +
                    ", tf=" + tf +
                    ", rank=" + rank +
                    '}';
        }

    }

    public static final class InvertCache2Holder {
        private static final InvertCache2 instance = new InvertCache2();
    }
}
