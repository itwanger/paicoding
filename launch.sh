#!/usr/bin/env bash

WEB_PATH="forum-web"
JAR_NAME="forum-web-0.0.1-SNAPSHOT.jar"

# 部署
function start() {
    git pull

    # 杀掉之前的进程
    cat pid.log| xargs -I {} kill {}
    mv ${JAR_NAME} ${JAR_NAME}_bk

    mvn clean install -Dmaven.test.skip=True -Pprod
    cd ${WEB_PATH}
    mvn clean package spring-boot:repackage -Dmaven.test.skip=true -Pprod
    cd -

    mv ${WEB_PATH}/target/${JAR_NAME} ./
    echo "启动脚本：\n==========="
    echo "nohup java -server -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &"
    echo "==========="
    nohup java -server -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &
    echo $! 1> pid.log
}

# 重启
function restart() {
    # 杀掉之前的进程
    cat pid.log| xargs -I {} kill {}
    # 重新启动
    echo "启动脚本：\n==========="
    echo "nohup java -server -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &"
    echo "==========="
    nohup java -server -Xmn512m -Xmn512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &
    echo $! 1> pid.log
}

if [ $# == 0 ]; then
  echo "miss command: start | restart"
elif [ $1 == 'start' ]; then
  start
elif [ $1 == 'restart' ];then
  restart
else
  echo 'illegal command, support cmd: start | restart'
fi