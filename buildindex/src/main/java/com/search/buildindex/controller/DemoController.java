package com.search.buildindex.controller;

import com.search.buildindex.service.RefreshTask;
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



    @RequestMapping("test.json")
    @ResponseBody
    public Map<String, Object> test(@RequestParam(value = "query", required = true) String query,
                                    @RequestParam(value = "limit", required = false) Integer limit) {
        Map<String, Object> res = new HashMap<String, Object>();

        try {

            RefreshTask refreshTask = RefreshTask.getInstance();
            res.put("data", refreshTask.start());
            res.put("ret", true);

        } catch (Exception e) {

            res.put("data", e);
            res.put("ret", false);

        }

        return res;
    }

}































