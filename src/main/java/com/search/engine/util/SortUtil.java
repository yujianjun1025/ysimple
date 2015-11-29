package com.search.engine.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.search.engine.pojo.DocImp;
import com.search.engine.pojo.Doc;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */
public class SortUtil {

    //taat term at a time
    //left , right 必须是已经排序好的链表, 得到docId的并集
    public static List<Integer> merge(List<Integer> left, List<Integer> right) {

        List<Integer> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(left) || CollectionUtils.isEmpty(right)) {
            return result;
        }


        Integer p1 = 0, p2 = 0;

        do {

            p2 = Collections.binarySearch(right, left.get(0));
            if (p2 >= 0) {
                p1 = 0;
                break;
            }

            p1 = Collections.binarySearch(left, right.get(0));
            if (p1 >= 0) {
                p2 = 0;
                break;
            }

            return result;

        } while (false);


        int leftSize = left.size(), rightSize = right.size();
        do {

            int leftValue = left.get(p1);
            int rightValue = right.get(p2);

            int compares = Ints.compare(leftValue, rightValue);
            if (compares == 0) {
                result.add(leftValue);
                p1++;
                p2++;
            } else if (compares < 0) {
                p1++;
            } else {
                p2++;
            }

        } while (!(Ints.compare(p1, leftSize) == 0) && !(Ints.compare(p2, rightSize) == 0));

        return result;
    }

    //得到所有的docId并集
    public static List<Integer> merge(List<List<Integer>> nodeForwardLists) {

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

        return mergedDocIds;
    }

    public static List<Doc> fileToDoc(String fileName) {

        List<Doc> allContent = Lists.newArrayList();

        try {

            Integer docId = 0;
            for (String content : Files.readLines(new File(fileName), Charsets.UTF_8)) {
                content = content.trim();
                if (content.length() == 0) {
                    continue;
                }
                allContent.add(new DocImp(docId++, content));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allContent;
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



    public static void main(String[] args) {
        List<String> stringList = Lists.newArrayList("ab", "aba", "abcd", "ababcab", "abcdabd", "aa", "abacacab", "abx", "");

        String src = "abbbcabacdababacdabcdaabdeabx";

        for (String target : stringList) {

            int[] next = getNext(target);
            StringBuffer buffer1 = new StringBuffer();
            StringBuffer buffer2 = new StringBuffer();
            for (int i = 0; i < target.length(); i++) {
                buffer1.append(target.charAt(i)).append(" ");
                buffer2.append(next[i]).append(" ");
            }

            System.out.println(buffer1);
            System.out.println(buffer2);


            int i = 0, j = 0;
            while (i < src.length() && j < target.length()) {
                if (src.charAt(i) == target.charAt(j)) {
                    i++;
                    j++;
                    continue;
                } else if (j == 0) {
                    i++;
                } else {
                    j = next[j - 1];
                }

            }

            if (j == target.length()) {
                System.out.println("contain: " + src.substring(i - j, i - j + target.length()));
            } else {
                System.out.println("didn't contain");
            }

        }


    }


}
