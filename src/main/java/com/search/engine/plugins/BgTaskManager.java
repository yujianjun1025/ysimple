package com.search.engine.plugins;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by yjj on 15/10/17.
 * 任务池
 */
@Component
public class BgTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(BgTaskManager.class);

    public void submitThread(final ProceedingJoinPoint joinPoint) {

        new Thread(new Runnable() {
            public void run() {

                String clazzName = joinPoint.getTarget().getClass().getName();
                String methodName = joinPoint.getSignature().getName();
                Object[] objects = joinPoint.getArgs();

                logger.info("开始运行后台任务{}.{}()", new Object[]{clazzName, methodName});
                long start = System.currentTimeMillis();
                try {
                    if (objects == null || objects.length == 0) {
                        joinPoint.proceed();
                    } else {
                        joinPoint.proceed(objects);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                } finally {
                    logger.info("{}.{}()运行耗时{}毫秒", new Object[]{clazzName, methodName, System.currentTimeMillis() - start});
                }
            }
        }).start();

    }
}
