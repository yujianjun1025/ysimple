package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.MergeNode;
import com.search.engine.pojo.WordInfo;
import com.search.engine.util.SortUtil;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by yjj on 15/11/22.
 */

@Component
public class InvertCache1 {


    private Multimap<String, Integer> invertCache = ArrayListMultimap.create();
    private Map<Integer, Map<String, WordInfo>> worldInfoCache = Maps.newHashMap();


    public Multimap<String, Integer> getInvertCache() {
        return invertCache;
    }

    public Map<Integer, Map<String, WordInfo>> getWorldInfoCache() {
        return worldInfoCache;
    }

    public void buildInvert(List<DocInfo> forwardCache) {

        for (DocInfo docInfo : forwardCache) {
            for (String world : docInfo.getWorldPosition().keys()) {

                List<Integer> docIds = (List<Integer>) invertCache.get(world);
                Integer index = Collections.binarySearch(docIds, docInfo.getDocId());
                if (index < 0) {
                    invertCache.put(world, docInfo.getDocId());
                }
            }
        }

        Integer docCount = forwardCache.size();
        for (DocInfo docInfo : forwardCache) {

            Integer worldCount = docInfo.getWorldCount();
            for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {

                String world = entry.getKey();
                double tf = (entry.getValue().size() * 1.0) / worldCount;
                double idf = Math.log(docCount / invertCache.get(world).size());
                double rank = tf * idf;

                Map<String, WordInfo> wordInfoMap = worldInfoCache.get(docInfo.getDocId());
                if (wordInfoMap == null) {
                    wordInfoMap = Maps.newHashMap();
                    worldInfoCache.put(docInfo.getDocId(), wordInfoMap);
                }

                wordInfoMap.put(world, new WordInfo(docInfo.getWorldPosition().get(world), tf, idf, rank));
            }

        }

    }

    @Override
    public String toString() {

        StringBuilder invert = new StringBuilder("\n");
        for (Map.Entry<String, Collection<Integer>> entry : invertCache.asMap().entrySet()) {
            invert.append(entry.getKey()).append(":").append(Joiner.on(",").join(entry.getValue())).append("\n");
        }

        StringBuilder worldInfo = new StringBuilder("\n");
        for (Map.Entry<Integer, Map<String, WordInfo>> entry : worldInfoCache.entrySet()) {
            worldInfo.append(entry.getKey()).append(":").append(Joiner.on(",")
                    .withKeyValueSeparator("=>").join(entry.getValue())).append("\n");
        }

        return "InvertCache1{"
                + invert
                + "worldInfoCache=" + worldInfo +
                '}';
    }


    public List<Integer> getDocIds(String string) {

        if (string == null || string.length() == 0) {
            return Lists.newArrayList();
        }

        List<List<Integer>> allDocId = Lists.newArrayList();
        for (int i = 0; i < string.length(); i++) {
            allDocId.add((List<Integer>) invertCache.get(String.valueOf(string.charAt(i))));
        }

        return SortUtil.merge(allDocId);
    }


    public List<Integer> filterDocId(List<Integer> docIds, String string) {

        List<Integer> res = Lists.newArrayList();

        for (Integer docId : docIds) {

            Integer pos = 0;
            List<MergeNode> mergeNodes = Lists.newArrayList();

            for (Character character : string.toCharArray()) {
                WordInfo wordInfo = worldInfoCache.get(docId).get(String.valueOf(character));
                mergeNodes.add(new MergeNode(pos++, wordInfo.getPosList()));
            }

            Collections.sort(mergeNodes, new Comparator<MergeNode>() {
                public int compare(MergeNode o1, MergeNode o2) {
                    return o1.getPosList().size() - o2.getPosList().size();
                }
            });


            int lastOrder = mergeNodes.get(0).getOrder();
            for (MergeNode mergeNode : mergeNodes) {
                int offset = mergeNode.getOrder() - lastOrder;
                mergeNode.setOffset(offset);
                lastOrder = mergeNode.getOrder();

            }
            for (int k = 0; k < mergeNodes.get(0).getPosList().size(); k++) {

                boolean flag = true;
                int i = mergeNodes.get(0).getPosList().get(k);
                for (int j = 1; j < mergeNodes.size(); j++) {
                    int index = Collections.binarySearch(mergeNodes.get(j).getPosList(), i + j);
                    if (index < 0) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    res.add(docId);
                    break;
                }
            }
        }

        return res;
    }

    public List<Integer> getTopN(List<Integer> docIds, String string, int topN) {

        Ordering<List<WordInfo>> ordering = new Ordering<List<WordInfo>>() {
            @Override
            public int compare(List<WordInfo> left, List<WordInfo> right) {

                for (int i = 0; i < left.size(); i++) {
                    int res = Double.compare(left.get(i).getRank(), right.get(i).getRank());
                    if (res != 0) {
                        return res;
                    }
                }

                return 0;
            }
        };

        List<Integer> integers = Lists.newArrayList();
        List<List<WordInfo>> res = Lists.newArrayList();

        //List<WordInfo>[] res = new ArrayList<WordInfo>[100];
        for (Integer docId : docIds) {

            List<WordInfo> wordInfoList = Lists.newArrayList();
            for (Character world : string.toCharArray()) {
                wordInfoList.add(worldInfoCache.get(docId).get(String.valueOf(world)));
            }

            if (ordering.compare(wordInfoList, res.get(0)) <= 0) {
                continue;
            }

            int index = ordering.binarySearch(res, wordInfoList);
            res.set(index, wordInfoList);
            res.remove(0);
            integers.set(index, docId);
            integers.remove(0);
        }


        return integers;
    }

    public void search(String string) {

        List<Integer> docIds = getDocIds(string);
        List<Integer> res = filterDocId(docIds, string);
        System.out.println("contain " + string + " docIds:" + Joiner.on(" ").join(res));
    }

}
