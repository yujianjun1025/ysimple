package com.search.buildindex.controller;

import com.search.buildindex.timetask.RebuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
public class BuildController {

    private static final Logger logger = LoggerFactory.getLogger(BuildController.class);


    @Resource
    private RebuildTask rebuildTask;


    @RequestMapping("test.json")
    @ResponseBody
    public Map<String, Object> test() {
        Map<String, Object> res = new HashMap<String, Object>();

        try {

            res.put("data", rebuildTask.rebuild());
            res.put("ret", true);

        } catch (Exception e) {

            res.put("data", e);
            res.put("ret", false);

        }

        return res;
    }

}































