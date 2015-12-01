package com.search.engine.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.search.engine.cache.InvertCache1;
import com.search.engine.pojo.MergeNode;
import com.search.engine.pojo.WordInfo;
import com.search.engine.util.SortUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.security.util.BitArray;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yjj on 15/11/29.
 */

@Component
public class Search {

    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);


    @Resource
    private InvertCache1 invertCache1;

    public List<Integer> getDocIds(String string) {

        if (string == null || string.length() == 0) {
            return Lists.newArrayList();
        }

        List<List<Integer>> allDocId = Lists.newArrayList();

        long begin = System.nanoTime();
        for (int i = 0; i < string.length(); i++) {

            List<Integer> tmp = (List<Integer>) invertCache1.getInvertCache().get(invertCache1.getStringCode(String.valueOf(string.charAt(i))));

            if (CollectionUtils.isEmpty(tmp)) {
                continue;
            }

            allDocId.add(tmp);
        }

        long end = System.nanoTime();
        logger.info("得到所有的docId耗时:{}毫秒", new Object[]{(end - begin) * 1.0 / 1000000});

        return SortUtil.merge(allDocId);
    }


    public List<Integer> filterDocId(List<Integer> docIds, String string) {

        List<Integer> res = Lists.newArrayList();

        for (Integer docId : docIds) {

            Integer pos = 0;
            List<MergeNode> mergeNodes = Lists.newArrayList();

            for (Character character : string.toCharArray()) {

                List<WordInfo> wordInfoList = invertCache1.getWorldInfoCache().get(docId);
                int index = Collections.binarySearch(wordInfoList, invertCache1.getStringCode(String.valueOf(character)));
                if (index > 0) {
                    WordInfo wordInfo = wordInfoList.get(index);
                    mergeNodes.add(new MergeNode(pos++, SortUtil.bitArrayToList(wordInfo.getPosList())));
                }

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

            if (mergeNodes.size() <= 1) {
                continue;
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


        for (Integer docId : docIds) {

            List<WordInfo> wordInfoList = Lists.newArrayList();
            for (Character world : string.toCharArray()) {
                // wordInfoList.add(invertCache1.getWorldInfoCache().get(docId).get(String.valueOf(world)));
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

    public List<Integer> doSearch(final String string) {

        long begin = System.nanoTime();
        List<Integer> docIds = getDocIds(string);
        long end = System.nanoTime();
        logger.info("查询词:{}, 得到所有docIds耗时:{}毫秒, 结果数{}", new Object[]{string, (end - begin) * 1.0 / 1000000, docIds.size()});
        begin = end;


        final Object object = new Object();
        List<List<Integer>> partions = Lists.partition(docIds, 100);
        final List<Integer> res = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(partions.size());
        for (final List<Integer> integers : partions) {

            threadPool.submit(new Runnable() {
                public void run() {
                    List<Integer> tmp = filterDocId(integers, string);
                    synchronized (object) {
                        res.addAll(tmp);
                    }
                    countDownLatch.countDown();
                }
            });

        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("countDownLatch 发生线程中断异常", e);
        }

        end = System.nanoTime();
        logger.info("查询词:{}, filterDocId耗时:{}毫秒, 结果数{}", new Object[]{string, (end - begin) * 1.0 / 1000000, res.size()});
        return res;

    }
}
