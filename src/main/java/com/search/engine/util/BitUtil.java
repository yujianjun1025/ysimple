package com.search.engine.util;

/**
 * Created by yjj on 15/12/6.
 */
public class BitUtil {


    private long setHigh32(long container, long highValue) {
        container &= 0x00000000ffffffffL;
        container |= highValue << 32;
        return container;
    }

    private long getHigh(long container) {
        container &= 0x7fffffff00000000L;
        return container >>> 32;
    }

    private long getLow(long container) {
        container &= 0x00000000ffffffffL;
        return container;
    }

    private long setLow32(long container, long lowValue) {
        container &= 0x7fffffff00000000L;
        container |= lowValue;
        return container;
    }
}
