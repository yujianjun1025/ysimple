package com.search.indexserver.util;

import com.google.common.collect.Lists;
import sun.security.util.BitArray;

import java.util.List;

/**
 * Created by yjj on 15/12/6.
 * bitArray工具类
 */
public class BitArrayUtil {

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
