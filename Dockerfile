FROM openjdk:8-jdk-alpine as builder
MAINTAINER yihui

# 定义两个变量
ARG JAR_NAME=paicoding-web-0.0.1-SNAPSHOT.jar
ARG WORK_PATH=/home/yihui/workspace/paicoding

# 创建工作目录
RUN mkdir -p $WORK_PATH
# 将jar拷贝过去
COPY /paicoding-web/target/$JAR_NAME $WORK_PATH/$JAR_NAME
# 将说明文件也拷贝过去
COPY /README.md $WORK_PATH/

# 指定工作目录
WORKDIR $WORK_PATH

# 运行jar
ENTRYPOINT ["java", "-jar", "paicoding-web-0.0.1-SNAPSHOT.jar"]