package com.search.engine.pojo;

import java.util.List;

/**
 * Created by yjj on 15/11/22.
 */
public interface Doc {
    List<String> split();
    Integer getDocId();
    String getValue();
}
