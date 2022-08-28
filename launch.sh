#!/usr/bin/env bash

WEB_PATH="forum-web"
JAR_NAME="forum-web-0.0.1-SNAPSHOT.jar"
git pull

# 杀掉之前的进程
cat pid.log| xargs -I {} kill {}
mv ${JAR_NAME} ${JAR_NAME}_bk

mvn clean install -Dmaven.test.skip=True -Pprod
cd ${WEB_PATH}
mvn clean package spring-boot:repackage -Dmaven.test.skip=true -Pprod
cd -

mv ${WEB_PATH}/target/${JAR_NAME} ./
nohup java -server -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} > /dev/null 2>&1 &
echo $! 1> pid.log