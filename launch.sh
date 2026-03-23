#!/usr/bin/env bash

WEB_PATH="paicoding-web"
JAR_NAME="paicoding-web-0.0.1-SNAPSHOT.jar"
LOG_DIR="logs"
STARTUP_LOG_FILE="${LOG_DIR}/startup-prod.log"

function load_env_file() {
    local env_file="$1"
    if [ -f "${env_file}" ]; then
        echo "加载环境配置: ${env_file}"
        set -a
        # shellcheck disable=SC1090
        source "${env_file}"
        set +a
    fi
}

function load_env() {
    load_env_file ".env"
    load_env_file ".env.local"
}

function stop_if_running() {
    if [ -f pid.log ]; then
        pid=$(cat pid.log)
        if [ -n "${pid}" ]; then
            kill "${pid}" 2>/dev/null || true
        fi
    fi
}

# 部署
function start() {
    git pull

    # 杀掉之前的进程
    stop_if_running
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
    stop_if_running
    # 重新启动
    run
}

function run() {
  load_env
  mkdir -p "${LOG_DIR}"
  echo "启动脚本：==========="
  echo "nohup java -server -Xms1g -Xmx1g -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} >> ${STARTUP_LOG_FILE} 2>&1 &"
  echo "==========="
  # ms 堆大小  mx 最大堆大小  mn 新生代大小
  nohup java -server -Dspring.devtools.restart.enabled=false -Xms1g -Xmx1g -Xmn256m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${JAR_NAME} >> "${STARTUP_LOG_FILE}" 2>&1 &
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
