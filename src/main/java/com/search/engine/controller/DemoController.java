package com.search.engine.controller;

import com.search.engine.service.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


@Controller
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);


    private Search search = Search.getInstance();

    @RequestMapping("test.json")
    @ResponseBody
    public Map<String, Object> test(@RequestParam(value = "world", required = true) String world) {
        Map<String, Object> res = new HashMap<String, Object>();

        try {
            res.put("data", search.doSearch(world));
            res.put("ret", true);

        } catch (Exception e) {

            res.put("data", e);
            res.put("ret", false);

        }

        return res;
    }

}































