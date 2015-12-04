package com.search.engine.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.search.engine.pojo.TermCodeAndTermInfoList;
import com.search.engine.pojo.TermInfo;
import com.search.engine.pojo.TermIntersection;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.util.BitArray;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/22.
 */
public class SortUtil {

    private static final Logger logger = LoggerFactory.getLogger(SortUtil.class);

    private static List<TermIntersection> intersection(int leftCode, List<TermInfo> left, int rightCode, List<TermInfo> right) {

        List<TermIntersection> res = Lists.newArrayList();
        for (TermInfo termInfo : left) {

            int index = Collections.binarySearch(right, termInfo.getDocId());
            if (index >= 0) {

                Map<Integer, TermInfo> termInfoMap = Maps.newHashMap();
                termInfoMap.put(leftCode, termInfo);
                termInfoMap.put(rightCode, right.get(index));
                res.add(new TermIntersection(termInfo.getDocId(), termInfoMap));

            }

        }

        return res;
    }

    private static List<TermIntersection> intersectionOnlyTwo(List<TermCodeAndTermInfoList> termCodeAndTermInfoList) {

        return intersection(termCodeAndTermInfoList.get(0).getTermCode(), termCodeAndTermInfoList.get(0).getTermInfoList(),
                termCodeAndTermInfoList.get(1).getTermCode(), termCodeAndTermInfoList.get(1).getTermInfoList());
    }

    private static List<TermIntersection> intersectionOnlyOne(List<TermCodeAndTermInfoList> termCodeAndTermInfoList) {

        List<TermIntersection> res = Lists.newArrayList();
        Integer termCode = termCodeAndTermInfoList.get(0).getTermCode();
        for (TermInfo termInfo : termCodeAndTermInfoList.get(0).getTermInfoList()) {

            Map<Integer, TermInfo> termInfoMap = Maps.newHashMap();
            termInfoMap.put(termCode, termInfo);
            res.add(new TermIntersection(termInfo.getDocId(), termInfoMap));
        }

        return res;
    }


    private static List<TermIntersection> intersection(List<TermIntersection> termIntersectionList, int rightCode, List<TermInfo> right) {

        List<TermIntersection> res = Lists.newArrayList();


        for (TermIntersection termIntersection : termIntersectionList) {

            int index = Collections.binarySearch(right, termIntersection.getDocId());
            if (index > 0) {

                Map<Integer, TermInfo> termInfoMap = termIntersection.getTermInfoMap();
                termInfoMap.put(rightCode, right.get(index));
                res.add(new TermIntersection(termIntersection.getDocId(), termInfoMap));
            }

        }
        return res;

    }

    public static List<TermIntersection> intersection(List<TermCodeAndTermInfoList> termCodeAndTermInfoList) {

        if (CollectionUtils.isEmpty(termCodeAndTermInfoList)) {
            return Lists.newArrayList();
        }

        logger.info("需要求交集合排序前结果:\n{}", Joiner.on("\n").join(termCodeAndTermInfoList));

        Collections.sort(termCodeAndTermInfoList, new Comparator<TermCodeAndTermInfoList>() {
            public int compare(TermCodeAndTermInfoList o1, TermCodeAndTermInfoList o2) {
                return Ints.compare(o1.getTermInfoList().size(), o2.getTermInfoList().size());
            }
        });

        List<TermIntersection> res = Lists.newArrayList();
        logger.info("需要求交集合排序后结果:\n{}", Joiner.on("\n").join(termCodeAndTermInfoList));
        if (termCodeAndTermInfoList.size() == 1) {
            res = intersectionOnlyOne(termCodeAndTermInfoList);
            logger.info("一位求交结果:\n{}", Joiner.on(" ").join(res));
            return res;

        }

        res = intersectionOnlyTwo(termCodeAndTermInfoList);
        logger.info("前2位求交结果:\n{}", Joiner.on(" ").join(res));
        for (int i = 2; i < termCodeAndTermInfoList.size(); i++) {
            res = intersection(res, termCodeAndTermInfoList.get(i).getTermCode(), termCodeAndTermInfoList.get(i).getTermInfoList());
        }

        return res;
    }

    public static int[] getNext(String string) {

        int size = string.length();

        int[] next = new int[size + 1];

        for (int i = 1; i < size; i++) {
            next[i] = string.charAt(next[i - 1]) == string.charAt(i) ? next[i - 1] + 1 : 0;
        }

        return next;

    }

    public static int contain(String src, String dst, int[] dstNext) {

        if (dstNext == null) {
            dstNext = getNext(dst);
        }

        int i = 0, j = 0;
        while (i < src.length() && j < dst.length()) {
            if (src.charAt(i) == dst.charAt(j)) {
                i++;
                j++;
                continue;
            } else if (j == 0) {
                i++;
            } else {
                j = dstNext[j - 1];
            }

        }

        return j == dst.length() ? i - j : -1;

    }


    public static List<Integer> bitArrayToList(BitArray bitArray) {

        List<Integer> res = Lists.newArrayList();
        for (int i = 0; i < bitArray.length(); i++) {

            if (bitArray.get(i)) {
                res.add(i);
            }
        }

        return res;
    }

}
