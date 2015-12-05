package com.search.engine.util;

/**
 * Created by yjj on 15/12/5.
 * 串匹配、查找工具类
 */
public class MatchUtil {

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
}
