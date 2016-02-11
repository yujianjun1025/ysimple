引言：
    仅仅只是想看看与自己写的搜索与lucene差距有多大？java与C++的性能方面差距有多大？而造的搜索引擎轮子。
    目前我已写了一个最最基本的大体框架。
    内部也做过一些内存、响应时间方面的优化,重提交记录能看出来其实做过很多的尝试，
    我深感搜索系统的复杂， 个人精力有限，后面还有很长的路要走，诚邀志同道合之士一起开发。
    没人加入也无所谓，路过的点个赞我就很满足啦, 要是能够加入我一起开发提交点代码、fix_bug就更爽歪歪啦

目前的情况:
    支持多field（实验数据为本5497479行单field数据），内存索引大概在5毫秒左右，磁盘索引在18毫秒左右
    git地址:https://github.com/yujianjun1025/ysimple

工程结构:
    adminapi:api工程
    buildindex:建索引工程
    indexserver:检索服务工程

文件结构:
    默认日志及数据文件路径：/tmp/search/
    数据源文件：
    /tmp/search//data/search_data.txt
    倒排索引文件:
    /tmp/search//invert/position.txt
    /tmp/search//invert/str2int.txt
    /tmp/search//invert/termInfo.dat
    /tmp/search//invert/version.txt
    buildIndex日志文件:
    /tmp/search//log/buildIndex/debug.log
    /tmp/search//log/buildIndex/error.log
    /tmp/search//log/buildIndex/info.log
    indexserver日志文件:
    /tmp/search//log/indexServer/debug.log
    /tmp/search//log/indexServer/error.log
    /tmp/search//log/indexServer/info.log
    
执行步骤:
    1. 直接执行./init.sh
    2. 启动buildindex, cd ./buildindex && ./run.sh
    3. 快速生成倒排数据到磁盘, 浏览器访问"http://127.0.0.1:9090/build.json"
    4. 启动indexserver cd ./indexserver && ./run.sh,
    5. 浏览器访问"http://127.0.0.1:8080/query.json?query=北京&limit=5" ( limit 为空时，默认返回top3000条数据)
    返回数据格式:
    {
        ret: true,
        data: [
        {
            docId: 164334,
            rank: 24900.37237152425
        },
        {
            docId: 699048,
            rank: 23614.956482398586
        },
        {
            docId: 1903045,
            rank: 18891.965185918867
        },
        {
            docId: 2894797,
            rank: 16583.64799943515
        },
        {
            docId: 311685,
            rank: 16583.64799943515
        }
        ]
    }
    
搜索日志格式:
    limit限制为5条时搜索北京、上海、山海日志：
    [2015-12-05 22:46:07.536] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:北京, 过滤到符合要求的docId耗时:2.819毫秒, 结果数5
    [2015-12-05 22:46:07.794] [621747766@qtp-669663528-2] [INFO ]<logger:153 doSearch>查询词:北京, 求交得到所有docIds耗时:2.999毫秒, 结果数16359
    [2015-12-05 22:46:07.795] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:北京, 过滤到符合要求的docId耗时:1.728毫秒, 结果数5
    [2015-12-05 22:46:08.280] [621747766@qtp-669663528-2] [INFO ]<logger:153 doSearch>查询词:北京, 求交得到所有docIds耗时:3.101毫秒, 结果数16359
    [2015-12-05 22:46:08.282] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:北京, 过滤到符合要求的docId耗时:1.87毫秒, 结果数5
    [2015-12-05 22:46:24.148] [621747766@qtp-669663528-2] [INFO ]<logger:153 doSearch>查询词:山海, 求交得到所有docIds耗时:3.438毫秒, 结果数4093
    [2015-12-05 22:46:24.149] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:山海, 过滤到符合要求的docId耗时:0.77毫秒, 结果数5
    [2015-12-05 22:46:25.348] [621747766@qtp-669663528-2] [INFO ]<logger:153 doSearch>查询词:山海, 求交得到所有docIds耗时:3.694毫秒, 结果数4093
    [2015-12-05 22:46:25.349] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:山海, 过滤到符合要求的docId耗时:0.884毫秒, 结果数5
    [2015-12-05 22:46:33.002] [621747766@qtp-669663528-2] [INFO ]<logger:153 doSearch>查询词:上海, 求交得到所有docIds耗时:3.481毫秒, 结果数14477
    [2015-12-05 22:46:33.003] [621747766@qtp-669663528-2] [INFO ]<logger:195 doSearch>查询词:上海, 过滤到符合要求的docId耗时:1.331毫秒, 结果数5
    磁盘索引日志:
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache 开始位置:40042492, 大小:199068 byte  
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache fc.map()耗时0.045787毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache byteBuffer.get()耗时0.236684毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache deserializeByProto()耗时3.114516毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache getTermInfoListByTermCode(termCode)耗时3.585566毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache 开始位置:94382617, 大小:102998 byte  
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache fc.map()耗时0.028592毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache byteBuffer.get()耗时0.144498毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache deserializeByProto()耗时1.851659毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.cache.InvertCache getTermInfoListByTermCode(termCode)耗时2.133863毫秒 
    INFO 2016-02-04 12:23:13 com.search.indexserver.service.TightnessSearch 需要求交的集合termCode:175 termInfoList size:62872 
    INFO 2016-02-04 12:23:13 com.search.indexserver.service.TightnessSearch 需要求交的集合termCode:986 termInfoList size:32117 
    INFO 2016-02-04 12:23:13 com.search.indexserver.service.TightnessSearch 查询词:北京, 求交得到所有docIds耗时:7.544401毫秒, 结果数16359 
    INFO 2016-02-04 12:23:13 com.search.indexserver.service.TightnessSearch 查询词:北京, 过滤到符合要求的docId耗时:2.524375毫秒, 结果数3000 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache 开始位置:74892539, 大小:139823 byte  
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache fc.map()耗时0.039005毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache byteBuffer.get()耗时0.130654毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache deserializeByProto()耗时2.075117毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache getTermInfoListByTermCode(termCode)耗时2.56754毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache 开始位置:6612848, 大小:519516 byte  
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache fc.map()耗时0.072454毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache byteBuffer.get()耗时0.327635毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache deserializeByProto()耗时4.659443毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.cache.InvertCache getTermInfoListByTermCode(termCode)耗时5.227373毫秒 
    INFO 2016-02-04 12:23:19 com.search.indexserver.service.TightnessSearch 需要求交的集合termCode:516 termInfoList size:42341 
    INFO 2016-02-04 12:23:19 com.search.indexserver.service.TightnessSearch 需要求交的集合termCode:29 termInfoList size:157593 
    INFO 2016-02-04 12:23:19 com.search.indexserver.service.TightnessSearch 查询词:上海, 求交得到所有docIds耗时:10.960468毫秒, 结果数14477 
    INFO 2016-02-04 12:23:19 com.search.indexserver.service.TightnessSearch 查询词:上海, 过滤到符合要求的docId耗时:3.311432毫秒, 结果数3000
       
接下来需要做的事:
    1、内存、响应时间是需要持之以恒的,优化永无止境
    2、目前基于ansj分词， 对于自定义词典还没研究
    3、查询语法,目前仅支持紧密度查询, 如“AB”@all_filed这种查询语法，下一步需要丰富查询语法的解析与索引 
    4、检索字段,目前的多field检索还只是一个空壳，还不能满足实际需求的多field检索

个人邮箱:yujianjun1025@126.com
