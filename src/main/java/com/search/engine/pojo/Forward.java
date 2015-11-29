package com.search.engine.pojo;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.search.engine.util.SortUtil;

import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */
public class Forward {


    //docId, List<WordInfo>
    private List<DocInfo> forwardCache = Lists.newArrayList();

    public List<DocInfo> getForwardCache() {
        return forwardCache;
    }

    public void produceForward(String fileName) {

        List<Doc> docList = SortUtil.fileToDoc(fileName);

        for (Doc doc : docList) {
            List<String> splitWorld = doc.split();
            Multimap<String, Integer> worldPosition = ArrayListMultimap.create();
            DocInfo docInfo = new DocInfo(doc.getDocId(), splitWorld.size(), worldPosition);
            forwardCache.add(docInfo);

            Integer pos = 0;
            for (String world : splitWorld) {
                worldPosition.put(world, pos);
                pos++;
            }
        }

    }


    @Override
    public String toString() {
        return "Forward{\n" + Joiner.on("\n").join(forwardCache) +
                "\n}";
    }


    public static class DocInfo {
        private Integer docId;
        private Integer worldCount;
        private Multimap<String, Integer> worldPosition = ArrayListMultimap.create();

        public DocInfo(Integer docId, Integer worldCount, Multimap<String, Integer> worldPosition) {
            this.docId = docId;
            this.worldCount = worldCount;
            this.worldPosition = worldPosition;
        }

        public Integer getDocId() {
            return docId;
        }

        public void setDocId(Integer docId) {
            this.docId = docId;
        }

        public Integer getWorldCount() {
            return worldCount;
        }

        public void setWorldCount(Integer worldCount) {
            this.worldCount = worldCount;
        }

        public Multimap<String, Integer> getWorldPosition() {
            return worldPosition;
        }

        public void setWorldPosition(Multimap<String, Integer> worldPosition) {
            this.worldPosition = worldPosition;
        }

        @Override
        public String toString() {


            return "DocInfo{" +
                    "docId=" + docId +
                    ", worldCount=" + worldCount +
                    ", worldPosition=" + Joiner.on(",").withKeyValueSeparator("=>").join(worldPosition.entries()) +
                    '}';
        }
    }


}
