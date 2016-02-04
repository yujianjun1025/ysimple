package com.search.indexserver.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.search.indexserver.cache.InvertCache;
import com.search.indexserver.pojo.DocIdAndRank;
import com.search.indexserver.pojo.TermCodeAndTermInfoList;
import com.search.indexserver.pojo.TermInOneDoc;
import com.search.indexserver.pojo.TermIntersection;
import com.search.indexserver.timetask.RefreshTask;
import com.search.indexserver.util.GatherUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yjj on 15/11/29.
 * 紧密度搜索类
 */

@Repository
@Slf4j
public class TightnessSearch {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final ExecutorService GET_TERM_INFO_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static Ordering<AssembleNode> ordering = new Ordering<AssembleNode>() {
        @Override
        public int compare(AssembleNode left, AssembleNode right) {
            return Ints.compare(left.getTermInOneDoc().getPositions().size(), right.getTermInOneDoc().getPositions().size());
        }
    };
    @Resource
    private RefreshTask refreshTask;

    public List<TermIntersection> getDocIdIntersection(final InvertCache invertCache, List<Integer> termCodeList, final int field) {

        if (CollectionUtils.isEmpty(termCodeList)) {
            return Lists.newArrayList();
        }


        final List<TermCodeAndTermInfoList> termCodeAndTermInfoLists = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(termCodeList.size());
        final Object object = new Object();
        for (Integer termCode : termCodeList) {

            final Integer integer = termCode;

            GET_TERM_INFO_THREAD_POOL.submit(new Runnable() {
                public void run() {

                    try {
                        synchronized (object) {
                            termCodeAndTermInfoLists.add(new TermCodeAndTermInfoList(integer, invertCache.getTermInfo(integer, field)));
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("countDownLatch.await()发生中断异常", e);
        }

        for (TermCodeAndTermInfoList termCodeAndTermInfoList : termCodeAndTermInfoLists) {
            log.info("需要求交的集合termCode:{} termInfoList size:{}", termCodeAndTermInfoList.getTermCode(), termCodeAndTermInfoList.getTermInOneDocList().size());
        }

        return GatherUtil.intersectionByBinSearch(termCodeAndTermInfoLists);
    }

    public List<DocIdAndRank> assembleDoc(List<TermIntersection> termIntersectionList, List<Integer> termCodeList, int topN) {

        List<DocIdAndRank> res = Lists.newArrayList();

        for (TermIntersection termIntersection : termIntersectionList) {


            int order = 1;
            List<AssembleNode> assembleNodeListOrigin = Lists.newArrayList();
            for (Integer aTermCodeList : termCodeList) {
                int termCode = aTermCodeList;
                AssembleNode assembleNode = new AssembleNode(termCode, order++, termIntersection.getTermInfoMap().get(termCode));
                assembleNodeListOrigin.add(assembleNode);
            }
            List<AssembleNode> orderBySizeAssembleNode = ordering.sortedCopy(assembleNodeListOrigin);
            int lastOrder = orderBySizeAssembleNode.get(0).getOrder();
            for (AssembleNode assembleNode : orderBySizeAssembleNode) {
                int offset = assembleNode.getOrder() - lastOrder;
                assembleNode.setOffset(offset);
                lastOrder = assembleNode.getOrder();
            }

            if (orderBySizeAssembleNode.size() <= 1) {
                GatherUtil.topN(res, orderBySizeAssembleNode.get(0).getTermInOneDoc().getDocId(),
                        orderBySizeAssembleNode.get(0).getTermInOneDoc().getRank(), topN);
                continue;
            }

            for (int firstPos : orderBySizeAssembleNode.get(0).getTermInOneDoc().getPositions()) {

                boolean flag = true;
                int next = firstPos;
                for (int j = 1; j < orderBySizeAssembleNode.size(); j++) {

                    next = next + orderBySizeAssembleNode.get(j).getOffset();
                    int index = Collections.binarySearch(orderBySizeAssembleNode.get(j).getTermInOneDoc().getPositions(), next);
                    if (index < 0) {
                        flag = false;
                        break;
                    }

                }

                if (flag) {
                    double rank = 0;
                    for (AssembleNode assembleNode : assembleNodeListOrigin) {
                        rank = 10 * rank + assembleNode.getTermInOneDoc().getRank();
                    }
                    GatherUtil.topN(res, termIntersection.getDocId(), rank, topN);
                    break;
                }
            }

        }

        return res;

    }

    public List<DocIdAndRank> doSearch(final String query, final int field, final int topN) {

        long begin = System.nanoTime();

        InvertCache invertCache = refreshTask.getInvertCache();
        final List<Integer> termCodeList = invertCache.getTermCodeListByQuery(query);

        List<TermIntersection> termIntersection = getDocIdIntersection(invertCache, termCodeList, field);
        long end = System.nanoTime();
        log.info("查询词:{}, 求交得到所有docIds耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, termIntersection.size());
        begin = end;

        if (CollectionUtils.isEmpty(termCodeList)) {
            log.info("termCodeList为 0， 不继续查询");
            return Lists.newArrayList();
        }

        final List<DocIdAndRank> docIdAndRankRes = Lists.newArrayList();
        final Object object = new Object();
        List<List<TermIntersection>> partitions = Lists.partition(termIntersection, 500);
        final CountDownLatch countDownLatch = new CountDownLatch(partitions.size());
        for (final List<TermIntersection> termIntersectionList : partitions) {

            threadPool.submit(new Runnable() {
                public void run() {

                    try {

                        List<DocIdAndRank> tmpDocIdAndRankList = assembleDoc(termIntersectionList, termCodeList, topN);
                        synchronized (object) {
                            for (DocIdAndRank docIdAndRank : tmpDocIdAndRankList) {
                                GatherUtil.topN(docIdAndRankRes, docIdAndRank, topN);
                            }
                        }

                    } catch (Exception e) {
                        log.error("assembleDoc时出现异常", e);
                    } finally {
                        countDownLatch.countDown();

                    }

                }
            });

        }


        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("countDownLatch 发生线程中断异常", e);
        }

        end = System.nanoTime();
        log.info("查询词:{}, 过滤到符合要求的docId耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, docIdAndRankRes.size());
        return Lists.reverse(docIdAndRankRes);

    }

    @Setter
    @Getter
    @ToString
    class AssembleNode implements Comparable<Object> {
        private int offset;
        private int order;
        private int termCode;
        private TermInOneDoc termInOneDoc;

        public AssembleNode(int termCode, int order, TermInOneDoc termInOneDoc) {
            this.termCode = termCode;
            this.order = order;
            this.termInOneDoc = termInOneDoc;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Integer) {
                return Ints.compare(termInOneDoc.getPositions().size(), (Integer) o);
            } else if (o instanceof AssembleNode) {
                AssembleNode tmp = (AssembleNode) o;
                return Ints.compare(termInOneDoc.getPositions().size(), tmp.getTermInOneDoc().getPositions().size());
            }
            return 0;
        }
    }
}
