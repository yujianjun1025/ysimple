import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;

import java.io.*;
import java.util.Map;

/**
 * Created by yjj on 15/12/28.
 */
public class JsonTest {


    public static void main(String[] args) {


        Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("b", 1);
        map.put("c", 1);
        map.put("d", 1);


        String json = JSON.toJSONString(map);

        Map<String, Integer> map1 = JSON.parseObject(json, new TypeReference<Map<String, Integer>>() {
        });


        System.out.println(map1.toString());

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("/tmp/xx.txt")));
            bufferedWriter.write(json);
            bufferedWriter.close();


            BufferedReader reader = new BufferedReader(new FileReader(new File("/tmp/xx.txt")));

            String oneLine  = reader.readLine();
            Map<String, Integer> map2 = JSON.parseObject(json, new TypeReference<Map<String, Integer>>() {
            });
            System.out.println(map2.toString());

            reader.close();


        }catch (Exception e){

            e.printStackTrace();

        }


    }


































}

