package com.search.buildindex.controller;

import com.search.buildindex.timetask.RebuildTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
@Slf4j
public class BuildController {

    @Resource
    private RebuildTask rebuildTask;


    @RequestMapping("build.json")
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































