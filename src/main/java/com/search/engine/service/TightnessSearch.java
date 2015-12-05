package com.search.engine.service;

import com.google.common.collect.Lists;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    private InvertCache invertCache = InvertCache.getInstance();


    private TightnessSearch() {
    }

    public static TightnessSearch getInstance() {
        return SearchHolder.instance;
    }

    public List<TermIntersection> getDocIdIntersection(List<Integer> termCodeList) {

        if (CollectionUtils.isEmpty(termCodeList)) {
            return Lists.newArrayList();
        }

        long begin = System.nanoTime();

        List<TermCodeAndTermInfoList> termCodeAndTermInfoLists = Lists.newArrayList();

        for (Integer termCode : termCodeList) {
            termCodeAndTermInfoLists.add(new TermCodeAndTermInfoList(termCode, invertCache.getTermInfo(termCode)));
        }

        List<TermIntersection> res = GatherUtil.intersectionByBinSearch(termCodeAndTermInfoLists);

        long end = System.nanoTime();
        logger.info("求交得到所有的docId耗时:{}毫秒", new Object[]{(end - begin) * 1.0 / 1000000});
        return res;
    }


    public List<DocIdAndRank> assembleDoc(List<TermIntersection> termIntersectionList, List<Integer> termCodeList, int topN) {

        List<DocIdAndRank> res = Lists.newArrayList();

        for (TermIntersection termIntersection : termIntersectionList) {


            //logger.info("需要过滤的信息:\n{}", termIntersection.toString());
            int order = 1;
            AssembleNode[] orderBySizeAssembleNode = new AssembleNode[termCodeList.size()];
            List<AssembleNode> assembleNodeListOrigin = Lists.newArrayList();
            for (int i = 0; i < termCodeList.size(); i++) {
                int termCode = termCodeList.get(i);
                AssembleNode assembleNode = new AssembleNode(termCode, order++, termIntersection.getTermInfoMap().get(termCode));
                assembleNodeListOrigin.add(assembleNode);
                orderBySizeAssembleNode[i] = assembleNode;

            }

            //logger.info("排序前nodeList:\n{}", Joiner.on("\n").join(assembleNodeList));
            Arrays.sort(orderBySizeAssembleNode, new Comparator<AssembleNode>() {
                public int compare(AssembleNode o1, AssembleNode o2) {
                    return Ints.compare(o1.getTermInfo().getPosList().size(), o2.getTermInfo().getPosList().size());
                }
            });


            //assembleNodeList = Lists.newArrayList(a);

           /* Collections.sort(assembleNodeList, new Comparator<AssembleNode>() {
                public int compare(AssembleNode o1, AssembleNode o2) {
                    return Ints.compare(o1.getTermInfo().getPosList().size(), o2.getTermInfo().getPosList().size());
                }
            });*/

            //logger.info("排序后nodeList:\n{}", Joiner.on("\n").join(assembleNodeList));

            int lastOrder = orderBySizeAssembleNode[0].getOrder();
            for (AssembleNode assembleNode : orderBySizeAssembleNode) {
                int offset = assembleNode.getOrder() - lastOrder;
                assembleNode.setOffset(offset);
                lastOrder = assembleNode.getOrder();
            }

            //logger.info("计算offset后的nodeList:\n{}", Joiner.on("\n").join(assembleNodeList));

            if (orderBySizeAssembleNode.length <= 1) {
                GatherUtil.topN(res, orderBySizeAssembleNode[0].getTermInfo().getDocId(),
                        orderBySizeAssembleNode[0].getTermInfo().getRank(), topN);
                //res.add(assembleNodeList.get(0).getTermInfo().getDocId());
                continue;
            }

            for (int firstPos : orderBySizeAssembleNode[0].getTermInfo().getPosList()) {

                boolean flag = true;
                int next = firstPos;
                for (int j = 1; j < orderBySizeAssembleNode.length; j++) {

                    next = next + orderBySizeAssembleNode[j].getOffset();
                    int index = Collections.binarySearch(orderBySizeAssembleNode[j].getTermInfo().getPosList(), next);
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

    public List<DocIdAndRank> doSearch(final String query, final int topN) {

        long begin = System.nanoTime();
        final List<Integer> termCodeList = invertCache.getTermCodeListByQuery(query);
        List<TermIntersection> termIntersection = getDocIdIntersection(termCodeList);
        long end = System.nanoTime();
        logger.info("查询词:{}, 得到所有docIds耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, termIntersection.size());
        begin = end;

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
        logger.info("查询词:{}, filterDocId耗时:{}毫秒, 结果数{}", query, (end - begin) * 1.0 / 1000000, docIdAndRankRes.size());
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
