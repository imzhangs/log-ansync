##1. 工具说明
####1.1 描述
  
    a. 工具支持文件断点续写，有断点记录位置在/log-ansync/position/ ， 打开工具会自动读取上次的同步的位置； 
    b. 定时每5秒同步一次，日志可能稍微会有点延迟写入，主要取决于 源日志文件的写入时间，貌似iis的日志写入是异步有延迟写入，nginx日志写入还是比较实时的； 
    c. 参数三选一，可选填，不支持多种混合填，运行一个进程只读写一个日志目录，自动读取该目录所有*.log， 一一对应，参数之间空格分隔。
  
      
####1.2 输入示例
``` 
        
   -logType=iis -iis.input.dir=c:/aaaa/bbb/ccc/ -iis.output.dir=d:/ddd/ee/ff/
```

##2. 参数说明
####2.1 日志类型参数说明 
```
 -logType=[iis|nginx|other]    工具默认logType=other  
```

####2.2 iis日志目录参数说明
```
  -iis.input.dir=c:/xxx/xx/    			工具默认iis输入 (系统盘:/inetpub/logs/LogFiles/W3SVC1)  
  -iis.output.dir=d:/xxx/xxx/  				工具默认iis输出(D:/log-ansync/output/LogFiles/)  
```
 
####2.3 nginx 日志目录参数说明
```
  -nginx.input.dir=/usr/local/nginx/logs     工具默认nginx输入 (/usr/local/nginx/logs)  
  -nginx.output.dir/log-ansync/xxxx/	工具默认nginx输出(/log-ansync/nginxlogs/)   
```

####2.4 other 日志目录参数说明
```
  -nginx.input.dir=/home/logs				    工具默认other输入 (/home/logs)  
  -iis.output.dir/log-ansync/xxxx/		            工具默认other输出(/log-ansync/nginxlogs/)   
```