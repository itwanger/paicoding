#!/usr/bin/env bash

WEB_PATH="paicoding-web"
JAR_NAME="paicoding-web-0.0.1-SNAPSHOT.jar"

# 部署
function start() {
    git pull

    # 杀掉之前的进程
    cat pid.log| xargs -I {} kill {}
    mv ${JAR_NAME} ${JAR_NAME}.bak

    mvn clean install -Dmaven.test.skip=True -Pprod
    cd ${WEB_PATH}
    mvn clean package spring-boot:repackage -Dmaven.test.skip=true -Pprod
    cd -

    mv ${WEB_PATH}/target/${JAR_NAME} ./
    run
}

# 重启
function restart() {
    # 杀掉之前的进程
    cat pid.log| xargs -I {} kill {}
    # 重新启动
    run
}

function run() {
  echo "启动脚本：==========="
  echo "nohup java -server -Xms1g -Xmx1g -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &"
  echo "==========="
  # ms 堆大小  mx 最大堆大小  mn 新生代大小
  nohup java -server -Dspring.devtools.restart.enabled=false -Xms1g -Xmx1g -Xmn256m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &
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