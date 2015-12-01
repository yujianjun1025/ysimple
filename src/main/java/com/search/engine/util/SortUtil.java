package com.search.engine.util;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.search.engine.pojo.Doc;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.util.BitArray;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */
public class SortUtil {

    private static final Logger logger = LoggerFactory.getLogger(SortUtil.class);

    //taat term at a time
    //left , right 必须是已经排序好的链表, 得到docId的并集
    public static List<Integer> merge(List<Integer> up, List<Integer> down) {

        List<Integer> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(up) || CollectionUtils.isEmpty(down)) {
            return result;
        }


        Integer p1 = 0, p2 = 0;
        int upSize = up.size(), downSize = down.size();
        do {

            int tmp = Ints.compare(up.get(p1), down.get(p2));

            if (tmp == 0) {
                result.add(up.get(p1));
                p1++;
                p2++;
            } else if (tmp < 0) {
                p1 = Collections.binarySearch(up, down.get(p2));
                p1 = p1 >= 0 ? p1 : Math.abs(p1 + 1);
                if (p1 >= upSize) {
                    return result;
                }
                break;
            } else {
                p2 = Collections.binarySearch(down, up.get(p1));
                p2 = p2 >= 0 ? p2 : Math.abs(p2 + 1);
                if (p2 >= downSize) {
                    return result;
                }
                break;
            }

        } while (p1 < upSize && p2 < downSize);


        while (!(Ints.compare(p1, upSize) == 0) && !(Ints.compare(p2, downSize) == 0)) {

            int leftValue = up.get(p1);
            int compares = Ints.compare(leftValue, down.get(p2));
            if (compares == 0) {
                result.add(leftValue);
                p1++;
                p2++;
            } else if (compares < 0) {
                p1++;
            } else {
                p2++;
            }

        }


        return result;
    }

    //得到所有的docId并集
    public static List<Integer> merge(List<List<Integer>> nodeForwardLists) {

        long begin = System.nanoTime();
        if (CollectionUtils.isEmpty(nodeForwardLists)) {
            return Lists.newArrayList();
        }

        Collections.sort(nodeForwardLists, new Comparator<List<Integer>>() {
            public int compare(List<Integer> o1, List<Integer> o2) {
                return Ints.compare(o1.size(), o2.size());
            }
        });

        int size = nodeForwardLists.size();
        if (size <= 1) {
            return nodeForwardLists.get(0);
        }

        List<Integer> mergedDocIds = nodeForwardLists.get(0);
        for (int i = 1; i < size; i++) {
            mergedDocIds = merge(mergedDocIds, nodeForwardLists.get(i));
        }

        long end = System.nanoTime();
        logger.info("求交耗时:{}毫秒", new Object[]{(end - begin) * 1.0 / 1000000});


        return mergedDocIds;
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

    public static void main(String[] args) {

        List<Integer> integers2 = Lists.newArrayList(46, 194, 209, 261, 262, 265, 348, 356, 410, 417, 437, 461, 557, 610, 657, 697, 977, 988, 1015, 1019, 999992, 999993, 999994, 999995, 999996, 999997, 999998, 999999);
        List<Integer> integers1 = Lists.newArrayList(1292, 2498, 2932, 54403, 54576, 999995, 999996, 999997, 999998, 999999);


        List<Integer> res = merge(integers1, integers2);

        System.out.println("merge 结果:" + Joiner.on(" ").join(res).toString());

    }


}
