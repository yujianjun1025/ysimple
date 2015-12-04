package com.search.engine.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.search.engine.pojo.DocInfo;
import com.search.engine.pojo.TermInfo;
import com.search.engine.service.Search;
import com.search.engine.util.SegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yjj on 15/11/29.
 */
public class InvertCache implements KryoSerializable {

    private static final long serialVersionUID = -2385906232920579818L;
    private static final Logger logger = LoggerFactory.getLogger(InvertCache.class);


    private static int WORD_COUNT = 0;
    private static int DOC_COUNT = 0;
    private Map<Integer, List<TermInfo>> invertCache = Maps.newHashMap();
    private Map<String, Integer> str2int = Maps.newHashMap();

    public List<TermInfo> getTermInfo(Integer stringCode) {
        List<TermInfo> res = invertCache.get(stringCode);
        return res != null ? res : Lists.<TermInfo>newArrayList();
    }

    public Integer getStringCode(String string) {

        Integer res = str2int.get(String.valueOf(string));
        return res != null ? res : -1;
    }

    public List<Integer> getTermCodeList(String string) {

        List<Integer> res = Lists.newArrayList();
        for (String str : SegUtil.split(string)) {
            res.add(getStringCode(str));
        }
        return res;
    }

    public void addDocInfo(DocInfo docInfo) {

        DOC_COUNT++;
        for (Map.Entry<String, Collection<Integer>> entry : docInfo.getWorldPosition().asMap().entrySet()) {


            String world = entry.getKey();
            if (!str2int.containsKey(world.intern())) {
                str2int.put(world.intern(), WORD_COUNT++);
            }
            Integer stringCode = str2int.get(world);

            List<TermInfo> termInfoList = invertCache.get(stringCode);
            if (termInfoList == null) {
                termInfoList = Lists.newArrayList();
                invertCache.put(stringCode, termInfoList);
            }

            TermInfo termInfo = null;
            int index = Collections.binarySearch(termInfoList, docInfo.getDocId());
            if (index < 0) {

                int tf = (entry.getValue().size() * 1000) / docInfo.getWorldCount();
                termInfo = new TermInfo(docInfo.getDocId(), entry.getValue(), tf);
                termInfoList.add(Math.abs(index + 1), termInfo);
            }
        }

    }

    public void calculateRank() {

        long begin = System.currentTimeMillis();
        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {

            double idf = Math.log(DOC_COUNT / entry.getValue().size());
            for (TermInfo termInfo : entry.getValue()) {
                double rank = idf * termInfo.getTf();
                termInfo.setRank(rank);
            }
        }
        logger.info("完成rank值计算，耗时{}毫秒", System.currentTimeMillis() - begin);
        logger.info("str2int size:{},  invertCache size:{}", new Object[]{str2int.size(), invertCache.size()});

    }

    public void write(Kryo kryo, Output output) {

    }

    public void read(Kryo kryo, Input input) {

    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<Integer, List<TermInfo>> entry : invertCache.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(Joiner.on(" ").join(entry.getValue())).append("\n");
        }

        return "InvertCache{\n" + stringBuilder.toString() + '}';
    }


    public static final class InvertCache2Holder {
        private static final InvertCache instance = new InvertCache();
    }

    public static InvertCache getInstance() {
        return InvertCache2Holder.instance;
    }

}
