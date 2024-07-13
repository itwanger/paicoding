#!/usr/bin/env bash

# pid file
PID_FILE_NAME="pid.log"

# file to upload
WEB_PATH="paicoding-web"
EXECUTABLE_JAR_NAME="paicoding-web-0.0.1-SNAPSHOT.jar"
TMP_EXECUTABLE_JAR_NAME=${EXECUTABLE_JAR_NAME}".tmp"
BAK_EXECUTABLE_JAR_NAME=${EXECUTABLE_JAR_NAME}".bak"
EXECUTABLE_JAR_PATH="./${WEB_PATH}/target/${EXECUTABLE_JAR_NAME}"

DEPLOY_SCRIPT="deploy.sh"
START_FUNC_NAME="start"
STOP_FUNC_NAME="stop"
RESTART_FUNC_NAME="restart"

#env, ssh remote, work dir
ENV_PRO="prod"
SSH_HOST_PRO=("ubuntu@124.221.214.211")
WORK_DIR_PRO="/home/ubuntu/tech-pai/"


# log file
declare LOG_FILES
LOG_BACKUP_FOLDER="logs/"

function stop() {
    # kill
    echo "--- 应用线下 ---"
    if [ -f "${PID_FILE_NAME}" ]; then
        pid=$(cat ${PID_FILE_NAME})
        echo "kill -9 ${pid}"
        kill -9 ${pid}
    fi
    echo "----------------"
}

function start() {
    work_dir=`dirname $0`
    cd ${work_dir}

    stop

    mv ${EXECUTABLE_JAR_NAME} ${BAK_EXECUTABLE_JAR_NAME}
    mv ${TMP_EXECUTABLE_JAR_NAME} ${EXECUTABLE_JAR_NAME}

    chmod 755 ${EXECUTABLE_JAR_NAME}
    # run
    echo "===== 启动脚本：====="
    run
}

function restart() {
  work_dir=`dirname $0`
  cd ${work_dir}
  stop
  # run
  echo "===== 启动重启：====="
  run
}

function run() {
  echo "nohup java -server -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${EXECUTABLE_JAR_NAME} > /dev/null 2>&1 &"
  echo "==================="
  nohup java -server -Dspring.devtools.restart.enabled=false -Xms512m -Xmx512m -Xmn512m -XX:NativeMemoryTracking=detail -XX:-OmitStackTraceInFastThrow -jar ${EXECUTABLE_JAR_NAME} "$@" > /dev/null 2>&1 &
  echo $! > ${PID_FILE_NAME}
}

function compile() {
    echo "---- start to build jar ----"
    echo "安装依赖：mvn clean install -Dmaven.test.skip=True -P${1}"
    mvn clean install -Dmaven.test.skip=True -P${1}
    cd ${WEB_PATH}
    echo "构建可运行jar：mvn clean package spring-boot:repackage -Dmaven.test.skip=true -P${1}"
    mvn clean package spring-boot:repackage -Dmaven.test.skip=true -P${1}
    cd -
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        return 1
    fi
    echo "---------- jar包构建完成 -------------"
}

function upload() {
    # upload jar
    # rename to *.jar.bak
    scp ${EXECUTABLE_JAR_PATH} $1:$2${TMP_EXECUTABLE_JAR_NAME}
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        echo 'Failed to scp jar'
        return 1
    fi

    # upload script
    scp ${DEPLOY_SCRIPT} $1:$2
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        echo 'Failed to scp deploy.sh'
        return 1
    fi
}

function deploy() {
    # package
    echo "*******Start to package*******"
    compile $1
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        echo 'Failed to compile'
        exit ${ret}
    fi

    if [ "$1" = "${ENV_PRO}" ]; then
        SSH_HOST=${SSH_HOST_PRO[@]}
        WORK_DIR=${WORK_DIR_PRO}
    else
        echo "Unknown env: $1"
        exit
    fi

    for host in ${SSH_HOST[@]}
    do
        # upload jar and deploy.sh
        echo "*******Start to upload:${host} *******"
        upload ${host} ${WORK_DIR}
        ret=$?
        if [[ ${ret} -ne 0 ]] ; then
            echo 'Failed to upload files'
            exit ${ret}
        fi
    done

    for host in ${SSH_HOST[@]}
    do
        # run
        echo "*******Start service:${host} *******"
        ssh ${host} "bash ${WORK_DIR}${DEPLOY_SCRIPT} ${START_FUNC_NAME}"
        echo "*******Done*******"
    done
}

if [ "$1" = "${START_FUNC_NAME}" ]; then
    start "$@"
elif [ "$1" = "${ENV_PRO}" ]; then
    deploy $1
elif [ "$1" = "${STOP_FUNC_NAME}" ]; then
    stop
elif [ "$1" = "${RESTART_FUNC_NAME}" ]; then
    restart
else
    echo "部署jar到服务器:  ./deploy.sh prod"
    echo "服务器上应用重启: ./deploy.sh restart"
    echo "服务器上应用关闭: ./deploy.sh stop"
fi