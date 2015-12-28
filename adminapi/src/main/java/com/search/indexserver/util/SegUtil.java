package com.search.indexserver.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yjj on 15/12/4.
 * 分词工具类
 */
public class SegUtil {

    public static List<String> split(String value) {

     /*   List<Term> terms = ToAnalysis.parse(value);


        return  Lists.transform(terms, new Function<Term, String>() {
            public String apply(Term input) {
                return input.getName();
            }
        });*/


        List<String> result = Lists.newArrayList();


        for (Character character : value.toCharArray()) {
            result.add(character.toString());
        }
        return result;
    }
}
