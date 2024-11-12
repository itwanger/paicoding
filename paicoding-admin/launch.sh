#!/usr/bin/env bash

# file to upload
WEB_PKG="dist.tar.gz"
WEB_PKG_BK="dist_bk.tar.gz"
TMP_WEB_PKG="tmp.tar.gz"

DEPLOY_SCRIPT="launch.sh"
START_FUNC_NAME="start"
BUILD_FUNC_NAME="build"
INSTALL_FUNC_NAME="install"
SERVER_FUNC_NAME="server"

#env, ssh remote, work dir
ENV_PRO="pro"
SSH_HOST_PRO=("admin@39.105.208.175")
WORK_DIR_PRO="/home/admin/workspace/admin/"
ADMIN_WORKSPACE="dist"



function build() {
    echo "---- start to build admin ----"
    echo "npm run build:pro"
    npm run build:pro
  	tar -zcvf ${WEB_PKG} dist

    echo "---------- 静态资源包dist.tar.gz已打包完成 -------------"
}

function upload() {
    # upload jar
    # rename to *.jar.bak
    scp ${WEB_PKG} $1:$2${TMP_WEB_PKG}
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        echo "Failed to scp ${WEB_PKG}"
        return 1
    fi

    # upload script
    scp ${DEPLOY_SCRIPT} $1:$2
    ret=$?
    if [[ ${ret} -ne 0 ]] ; then
        echo 'Failed to scp launch.sh'
        return 1
    fi
}

function install() {
    npm install
}

function server() {
    npm run dev
}

function start() {
    echo "---- 开始部署 ----"
		cd ${WORK_DIR_PRO}
		mv ${WEB_PKG} ${WEB_PKG_BK}
		mv ${ADMIN_WORKSPACE} "${ADMIN_WORKSPACE}_bk"
		mv ${TMP_WEB_PKG} ${WEB_PKG}
		tar -zxvf ${WEB_PKG}
		echo "---- 部署完成 ----"
}

function deploy() {
    # package
    echo "*******Start to package*******"
    build $1
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
        # upload jar and launch.sh
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
elif [ "$1" = "${BUILD_FUNC_NAME}" ]; then
    build $1
elif [ "$1" = "${INSTALL_FUNC_NAME}" ]; then
    install $1
elif [ "$1" = "${SERVER_FUNC_NAME}" ]; then
    server $1
elif [ "$1" = "${ENV_PRO}" ]; then
    deploy $1
else
    echo "=========== 本地环境安装 & 调试 =============="
    echo "安装依赖:  ./launch.sh install"
    echo "本地启动:  ./launch.sh server"
    echo "=========== 上传服务器 & 服务器解压使用 =============="
    echo "打包 dist.tar.gz:  ./launch.sh build"
    echo "打包静态资源并上传到服务器 & 解压执行:  ./launch.sh pro"
    echo "服务器上资源启用: ./launch.sh start"
fi
