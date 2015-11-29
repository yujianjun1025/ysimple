package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.search.engine.pojo.DocInfo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/29.
 */
@Component
public class InvertCache2 {


    private Map<String, List<InvertDocInfo>> invertCache = Maps.newHashMap();


    public void buildInvert(List<DocInfo> forwardCache) {

        for (DocInfo docInfo : forwardCache) {

            for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {

                InvertDocInfo invertDocInfo = new InvertDocInfo(docInfo.getDocId(), (List<Integer>) entry.getValue());

                List<InvertDocInfo> docIds = invertCache.get(entry.getKey());
                if (CollectionUtils.isEmpty(docIds)) {
                    invertCache.put(entry.getKey(), Lists.newArrayList(invertDocInfo));
                    continue;
                }

                Integer index = Collections.binarySearch(docIds, docInfo.getDocId());
                if (index < 0) {
                    docIds.add(Math.abs(index + 1), invertDocInfo);
                }

            }
        }

        Integer docCount = forwardCache.size();
        for (DocInfo docInfo : forwardCache) {

            Integer worldCount = docInfo.getWorldCount();
            for (Map.Entry<String, List<InvertDocInfo>> entry : invertCache.entrySet()) {

                String world = entry.getKey();
                for (InvertDocInfo invertDocInfo : entry.getValue()) {
                    double tf = (entry.getValue().size() * 1.0) / worldCount;
                    double idf = Math.log(docCount / invertCache.get(world).size());
                    double rank = tf * idf;

                    invertDocInfo.setTf(tf);
                    invertDocInfo.setIdf(idf);
                    invertDocInfo.setRank(rank);

                }


            }

        }

    }


    public static class InvertDocInfo implements Comparable<Integer> {

        private int docId;
        private List<Integer> posList;
        private double tf;
        private double idf;
        private double rank;

        public InvertDocInfo(Integer docId, List<Integer> posList) {
            this.docId = docId;
            this.posList = posList;
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

        public double getTf() {
            return tf;
        }

        public void setTf(double tf) {
            this.tf = tf;
        }

        public double getIdf() {
            return idf;
        }

        public void setIdf(double idf) {
            this.idf = idf;
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
                    ", idf=" + idf +
                    ", rank=" + rank +
                    '}';
        }

    }


    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, List<InvertDocInfo>> entry : invertCache.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(Joiner.on(" ").join(entry.getValue())).append("\n");
        }

        return "InvertCache2{\n" + stringBuilder.toString() + '}';
    }
}
