package com.search.engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);


    @RequestMapping("test.json")
    @ResponseBody
    public Map<String, Object> test() {
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("ret", true);
        /*try {

            List<Integer> ticketIds = ticketQueryClient.getAllTicketProductIds(false);
            res.put("data", ticketQueryClient.queryTicketListByIds(IdType.PRODUCT, QueryOptions.defaultOptions(),ticketIds.subList(0,18)));
            res.put("dealClient", tripDealClientService.toString());

        } catch (TException e) {
            res.put("data", e.toString());
        }*/
        return res;
    }

}































