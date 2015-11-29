package com.search.engine.timetask;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by yjj on 15/10/12.
 * 用DealClient重magellan更新travel_deal_tripattrs_ex
 */


@Component
public class UpdateTravelAttrClient {

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public void updateTripAttr() {

        if (!reentrantLock.tryLock()) {
            logger.info("定时任务正在运行");
            return;
        }

        try {


        } catch (Exception e) {
            logger.error("更新数据发生异常", e);
        } finally {

            reentrantLock.unlock();
        }
    }

}
