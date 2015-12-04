package com.search.engine.service;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.search.engine.cache.InvertCache;
import com.search.engine.pojo.TermCodeAndTermInfoList;
import com.search.engine.pojo.TermInfo;
import com.search.engine.pojo.TermIntersection;
import com.search.engine.util.SegUtil;
import com.search.engine.util.SortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yjj on 15/11/29.
 */

public class Search {

    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private InvertCache invertCache = InvertCache.getInstance();


    private Search() {
    }

    public static Search getInstance() {
        return SearchHolder.instance;
    }

    public List<TermIntersection> getDocIdIntersection(String string) {

        if (string == null || string.length() == 0) {
            return Lists.newArrayList();
        }

        long begin = System.nanoTime();

        List<TermCodeAndTermInfoList> termCodeAndTermInfoLists = Lists.newArrayList();
        for (String str : SegUtil.split(string)) {

            int termCode = invertCache.getStringCode(str);
            termCodeAndTermInfoLists.add(new TermCodeAndTermInfoList(termCode, invertCache.getTermInfo(termCode)));
        }

        List<TermIntersection> res = SortUtil.intersection(termCodeAndTermInfoLists);

        long end = System.nanoTime();
        logger.info("得到所有的docId耗时:{}毫秒", new Object[]{(end - begin) * 1.0 / 1000000});
        return res;
    }


    public List<Integer> assembleDoc(List<TermIntersection> termIntersectionList, List<Integer> termCodeList) {

        List<Integer> res = Lists.newArrayList();

        for (TermIntersection termIntersection : termIntersectionList) {


            List<Node> nodeList = Lists.newArrayList();

            int order = 1;
            for (Integer termCode : termCodeList) {
                nodeList.add(new Node(termCode, order++, termIntersection.getTermInfoMap().get(termCode)));
            }

            Collections.sort(nodeList, new Comparator<Node>() {
                public int compare(Node o1, Node o2) {
                    return Ints.compare(o1.getTermInfo().getPosList().size(), o2.getTermInfo().getPosList().size());
                }
            });


            int lastOrder = nodeList.get(0).getOrder();
            for (Node node : nodeList) {
                int offset = node.getOrder() - lastOrder;
                node.setOffset(offset);
                lastOrder = node.getOrder();
            }

            if (nodeList.size() <= 1) {
                continue;
            }

            for (int k = 0; k < nodeList.get(0).getTermInfo().getPosList().size(); k++) {

                boolean flag = true;
                int i = nodeList.get(0).getTermInfo().getPosList().get(k);
                for (int j = 1; j < nodeList.size(); j++) {
                    int index = Collections.binarySearch(nodeList.get(j).getTermInfo().getPosList(), i + j);
                    if (index < 0) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    res.add(termIntersection.getDocId());
                    break;
                }
            }

        }

        return res;

    }

    public List<Integer> doSearch(final String string) {

        long begin = System.nanoTime();
        final List<Integer> termCodeList = invertCache.getTermCodeList(string);
        List<TermIntersection> termIntersection = getDocIdIntersection(string);
        long end = System.nanoTime();
        logger.info("查询词:{}, 得到所有docIds耗时:{}毫秒, 结果数{}", new Object[]{string, (end - begin) * 1.0 / 1000000, termIntersection.size()});
        begin = end;

        final Object object = new Object();
        List<List<TermIntersection>> partitions = Lists.partition(termIntersection, 100);
        final List<Integer> res = Lists.newArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(partitions.size());
        for (final List<TermIntersection> integers : partitions) {

            threadPool.submit(new Runnable() {
                public void run() {
                    List<Integer> tmp = assembleDoc(integers, termCodeList);
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

    class Node implements Comparable<Integer> {
        private int offset;
        private int order;

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

        private int termCode;

        private TermInfo termInfo;

        public Node(int termCode, int order, TermInfo termInfo) {
            this.termCode = termCode;
            this.order = order;
            this.termInfo = termInfo;
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
    }

    private static final class SearchHolder {
        private static final Search instance = new Search();
    }
}
