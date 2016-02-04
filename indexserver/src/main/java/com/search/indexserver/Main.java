package com.search.indexserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by yjj on 16/2/3.
 */
public class Main {


    private static final Logger log = LoggerFactory.getLogger("stdInfo");

    private static final Logger log2 = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        log.info("XXXXXXXXXXXXXXXXXXXXXXX");
        log2.error("errrrrrrrrrrrrrrrrrrrrrrrrrr");
        log2.info("info loggggggggggggggggggggggg");
        Thread.currentThread().join();
        System.exit(1);
    }
}
