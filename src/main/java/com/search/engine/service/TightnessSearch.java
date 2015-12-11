package com.search.engine.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.search.engine.cache.InvertCache;
import com.search.engine.pojo.DocIdAndRank;
import com.search.engine.pojo.TermCodeAndTermInfoList;
import com.search.engine.pojo.TermInfo;
import com.search.engine.pojo.TermIntersection;
import com.search.engine.util.GatherUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yjj on 15/11/29.
 * 紧密度搜索类
 */

public class TightnessSearch {

    private static final Logger logger = LoggerFactory.getLogger(TightnessSearch.class);

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    Ordering<AssembleNode> ordering = new Ordering<AssembleNode>() {
        @Override
        public int compare(AssembleNode left, AssembleNode right) {
            return Ints.compare(left.getTermInfo().getPosList().size(), right.getTermInfo().getPosList().size());
        }
    };
    private InvertCache invertCache = InvertCache.getInstance();

    private TightnessSearch() {
    }

    public static TightnessSearch getInstance() {
        return SearchHolder.instance;
    }

    public List<TermIntersection> getDocIdIntersection(List<Integer> termCodeList, int field) {

        if (CollectionUtils.isEmpty(termCodeList)) {
            return Lists.newArrayList();
        }


        List<TermCodeAndTermInfoList> termCodeAndTermInfoLists = Lists.newArrayList();

        for (Integer termCode : termCodeList) {
            termCodeAndTermInfoLists.add(new TermCodeAndTermInfoList(termCode, invertCache.getTermInfo(termCode, field)));
        }

        return GatherUtil.intersectionByBinSearch(termCodeAndTermInfoLists);
    }

    public List<DocIdAndRank> assembleDoc(List<TermIntersection> termIntersectionList, List<Integer> termCodeList, int topN) {

        List<DocIdAndRank> res = Lists.newArrayList();

        for (TermIntersection termIntersection : termIntersectionList) {


            //logger.info("需要过滤的信息:\n{}", termIntersection.toString());
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

            //logger.info("计算offset后的nodeList:\n{}", Joiner.on("\n").join(assembleNodeList));

            if (orderBySizeAssembleNode.size() <= 1) {
                GatherUtil.topN(res, orderBySizeAssembleNode.get(0).getTermInfo().getDocId(),
                        orderBySizeAssembleNode.get(0).getTermInfo().getRank(), topN);
                //res.add(assembleNodeList.get(0).getTermInfo().getDocId());
                continue;
            }

            for (int firstPos : orderBySizeAssembleNode.get(0).getTermInfo().getPosList()) {

                boolean flag = true;
                int next = firstPos;
                for (int j = 1; j < orderBySizeAssembleNode.size(); j++) {

                    next = next + orderBySizeAssembleNode.get(j).getOffset();
                    int index = Collections.binarySearch(orderBySizeAssembleNode.get(j).getTermInfo().getPosList(), next);
                    if (index < 0) {
                        flag = false;
                        break;
                    }

                }

                if (flag) {
                    double rank = 0;
                    for (AssembleNode assembleNode : assembleNodeListOrigin) {
                        rank = 10 * rank + assembleNode.getTermInfo().getRank();
                    }
                    GatherUtil.topN(res, termIntersection.getDocId(), rank, topN);
                    //res.add(termIntersection.getDocId());
                    break;
                }
            }

        }

        return res;

    }

    public List<DocIdAndRank> doSearch(final String query, final int field, final int topN) {

        long begin = System.nanoTime();
        final List<Integer> termCodeList = invertCache.getTermCodeListByQuery(query);

        List<TermIntersection> termIntersection = getDocIdIntersection(termCodeList, field);
        long end = System.nanoTime();
        logger.info("查询词:{}, 求交得到所有docIds耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, termIntersection.size());
        begin = end;

        if (CollectionUtils.isEmpty(termCodeList)) {
            logger.info("termCodeList为 0， 不继续查询");
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
                        logger.error("assembleDoc时出现异常", e);
                    } finally {
                        countDownLatch.countDown();

                    }

                }
            });

        }


        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("countDownLatch 发生线程中断异常", e);
        }

        end = System.nanoTime();
        logger.info("查询词:{}, 过滤到符合要求的docId耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, docIdAndRankRes.size());
        return Lists.reverse(docIdAndRankRes);

    }

    private static final class SearchHolder {
        private static final TightnessSearch instance = new TightnessSearch();
    }

    class AssembleNode implements Comparable<Integer> {
        private int offset;
        private int order;
        private int termCode;
        private TermInfo termInfo;

        public AssembleNode(int termCode, int order, TermInfo termInfo) {
            this.termCode = termCode;
            this.order = order;
            this.termInfo = termInfo;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getTermCode() {
            return termCode;
        }

        public void setTermCode(int termCode) {
            this.termCode = termCode;
        }

        public TermInfo getTermInfo() {
            return termInfo;
        }

        public void setTermInfo(TermInfo termInfo) {
            this.termInfo = termInfo;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int compareTo(Integer o) {
            return Ints.compare(termInfo.getPosList().size(), o);
        }

        @Override
        public String toString() {
            return "AssembleNode{" +
                    "offset=" + offset +
                    ", order=" + order +
                    ", termCode=" + termCode +
                    ", termInfo=" + termInfo +
                    '}';
        }
    }
}
