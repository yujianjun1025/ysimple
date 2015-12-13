package com.search.engine.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.FieldInfo;
import com.search.engine.pojo.TermInfo;
import com.search.engine.util.SegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/12/11.
 * 倒排索引类
 */
public class InvertCache {


    private static final Logger logger = LoggerFactory.getLogger(InvertCache.class);

    private int WORD_COUNT = 0;
    private int DOC_COUNT = 0;

    private Map<Integer, List<Integer>> invertCache = Maps.newHashMap();
    private Map<String, Integer> str2int = Maps.newHashMap();

    public static InvertCache getInstance() {
        return InvertCacheHolder.instance;
    }

    public List<Integer> getDocIdByTermCode(Integer termCode) {

        List<Integer> res = invertCache.get(termCode);
        return res != null ? res : Lists.<Integer>newArrayList();

    }

    public List<TermInfo> getTermInfo(Integer stringCode, int field) {
       /* List<TermInfo> res = invertCache.get(stringCode);

        if (res == null) {
            return Lists.newArrayList();
        }


        FieldAndDocId min = new FieldAndDocId(field, Integer.MIN_VALUE);
        int minIndex = Collections.binarySearch(res, min);
        FieldAndDocId max = new FieldAndDocId(field, Integer.MAX_VALUE);
        int maxIndex = Collections.binarySearch(res, max);


        minIndex = Math.abs(minIndex + 1);
        maxIndex = Math.abs(maxIndex + 1);

        if (minIndex >= res.size()) {
            return Lists.newArrayList();
        }

        if (maxIndex > res.size()) {
            maxIndex = res.size();
        }

        if (Integer.compare(minIndex, maxIndex) == 0) {
            return Lists.newArrayList();
        }


        return res.subList(minIndex, maxIndex);*/

        return Lists.newArrayList();
    }

    private Integer str2Int(String string) {
        return str2int.get(String.valueOf(string));
    }


    public Integer putIfAbsent(String string) {

        Integer value = str2int.get(string);
        if (value == null) {
            str2int.put(string, WORD_COUNT++);
            return WORD_COUNT - 1;
        }
        return value;
    }

    public List<Integer> getTermCodeListByQuery(String query) {

        List<Integer> res = Lists.newArrayList();
        for (String string : SegUtil.split(query)) {

            Integer string2code = str2Int(string);
            if (string2code == null) {
                return Lists.newArrayList();
            }
            res.add(string2code);
        }
        return res;
    }


    public void addDocInfo(DocInfo docInfo) {

        DOC_COUNT++;

        int docId = docInfo.getDocId();
        for (FieldInfo fieldInfo : docInfo.getField()) {
            int fieldId = fieldInfo.getField();
            for (Map.Entry<Integer, Collection<Integer>> entry : fieldInfo.getWorldPosition().asMap().entrySet()) {


                Integer termCode = entry.getKey();

                List<Integer> forwardInfoList = invertCache.get(termCode);
                if (forwardInfoList == null) {
                    forwardInfoList = Lists.newArrayList(docId);
                    invertCache.put(termCode, forwardInfoList);
                } else {
                    int index = Collections.binarySearch(forwardInfoList, docId);
                    if (index < 0) {
                        index = Math.abs(index + 1);
                        forwardInfoList.add(index, docId);
                    }
                }

            }


        }


    }

    public void calculateRank() {

       /* long begin = System.currentTimeMillis();
        for (List<TermInfo> entry : invertCache) {

            double idf = Math.log(DOC_COUNT / entry.size());
            for (TermInfo termInfo : entry) {
                double rank = idf * termInfo.getTf();
                termInfo.setRank(rank);
            }
        }
        logger.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        logger.info("str2int size:{},  invertCache size:{}", new Object[]{str2int.size(), invertCache.size()});
*/
    }

    public static final class InvertCacheHolder {
        private static final InvertCache instance = new InvertCache();
    }

}
