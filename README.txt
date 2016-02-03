引言：
    最近想自己基于java完全重底层开始实现一套搜索, 目前我已写了一个最最基本的大体框架, 
    内部也做过一些内存、响应时间方面的优化,重提交记录能看出来其实做过很多的尝试，
    我深感搜索系统的复杂， 个人精力有限，后面还有很长的路要走，诚邀志同道合之士一起开发。
    没人加入也无所谓，路过的点个赞我就很满足啦, 要是能够加入我一起开发提交点代码、fix_bug就更爽歪歪啦

目前的情况:
    支持多field（实验数据为本5497479行单field数据），内存索引大概在5毫秒左右，磁盘索引在18毫秒左右
    git地址:https://github.com/yujianjun1025/search_engine

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
    
接下来需要做的事:
    1、内存、响应时间是需要持之以恒的,优化永无止境
    2、目前基于ansj分词， 对于自定义词典还没研究
    3、查询语法,目前仅支持紧密度查询, 如“AB”@all_filed这种查询语法，下一步需要丰富查询语法的解析与索引 
    4、检索字段,目前仅支持单field检索，下一步还需要多文本域检索及属性字段检索

个人邮箱:yujianjun1025@126.com
