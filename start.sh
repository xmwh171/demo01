#!/bin/bash
# 项目JAR包名称
APP_NAME=demo01-0.0.1-SNAPSHOT.jar
# JVM参数
JVM_OPTS="-Xms4096m -Xmx4096m -XX:NewRatio=1 -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:MaxGCPauseMillis=200 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/demo01/jvm/heapdump.hprof -Xloggc=/opt/demo01/jvm/gc.log"
# 启动项目
nohup java $JVM_OPTS -jar $APP_NAME > /opt/demo01/logs/start.log 2>&1 &
# 打印启动日志
echo "项目启动成功，PID：$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')"