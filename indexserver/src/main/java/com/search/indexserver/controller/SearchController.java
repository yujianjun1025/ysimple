package com.search.indexserver.controller;

import com.search.indexserver.service.TightnessSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
@Slf4j
public class SearchController {


    @Resource
    private TightnessSearch tightnessSearch;

    @RequestMapping("query.json")
    @ResponseBody
    public Map<String, Object> test(@RequestParam(value = "query", required = true) String query,
                                    @RequestParam(value = "limit", required = false) Integer limit) {
        Map<String, Object> res = new HashMap<String, Object>();

        try {

            int topN = limit != null ? limit : 3000;
            res.put("data", tightnessSearch.doSearch(query, 1, topN));
            res.put("ret", true);

        } catch (Exception e) {

            res.put("data", e);
            res.put("ret", false);

        }

        return res;
    }

}































