package com.search.engine.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yjj on 15/12/4.
 */
public class SegUtil {

    public static List<String> split(String value) {

        List<String> result = Lists.newArrayList();
        for (Character character : value.toCharArray()) {
            result.add(character.toString());
        }
        return result;
    }
}
