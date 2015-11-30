# search_engine
基于java的搜索引擎demo

运行方式:
0. 修改logback.xml fileDir 指定日志目录
1. tar -xzvf search_data.txt.tar.gz | 必须截断为200W以下，不然会outofmemery
2.export MAVEN_OPTS=-Xms10000m
3.mvn jetty:run
4. 访问：http://127.0.0.1:8080/test.json?world=XX

