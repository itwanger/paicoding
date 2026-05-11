#!/usr/bin/env bash

set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNTIME_DIR="${ROOT_DIR}/.runtime"
PID_DIR="${RUNTIME_DIR}/pids"
LOG_DIR="${RUNTIME_DIR}/logs"

load_env_file() {
    local env_file="$1"
    if [ -f "${env_file}" ]; then
        echo "加载环境配置: ${env_file}"
        set -a
        # shellcheck disable=SC1090
        source "${env_file}"
        set +a
    fi
}

load_env_file "${ROOT_DIR}/.env"
load_env_file "${ROOT_DIR}/.env.local"

mkdir -p "${PID_DIR}" "${LOG_DIR}"

ELASTICSEARCH_DIR="${ES_HOME:-${ELASTICSEARCH_DIR:-/Users/itwanger/Downloads/elasticsearch-8.10.0}}"
ELASTICSEARCH_BIN="${ES_BIN:-${ELASTICSEARCH_DIR}/bin/elasticsearch}"
ELASTICSEARCH_PORT="${ES_HTTP_PORT:-${ELASTICSEARCH_PORT:-9200}}"
ELASTICSEARCH_TRANSPORT_PORT="${ES_TRANSPORT_PORT:-9300}"
ELASTICSEARCH_SCHEME="${ELASTICSEARCH_SCHEME:-${PAICODING_ES_SCHEME:-https}}"
ELASTICSEARCH_JAVA_OPTS="${ES_JAVA_OPTS:--Xms500M -Xmx500M}"
ELASTICSEARCH_CLUSTER_NAME="${ES_CLUSTER_NAME:-elasticsearch}"

PID_FILE="${PID_DIR}/elasticsearch.pid"
LOG_FILE="${LOG_DIR}/elasticsearch.log"

java_major_version() {
    local java_bin="$1"
    local version_line version
    version_line="$("${java_bin}" -version 2>&1 | head -n 1)"
    version="$(printf '%s\n' "${version_line}" | sed -E 's/.*version "([^"]+)".*/\1/')"

    if [[ "${version}" == 1.* ]]; then
        echo "${version#1.}" | cut -d. -f1
        return 0
    fi

    echo "${version}" | cut -d. -f1
}

detect_java_home() {
    if [ -n "${ES_JAVA_HOME:-}" ] && [ -x "${ES_JAVA_HOME}/bin/java" ]; then
        echo "${ES_JAVA_HOME}"
        return 0
    fi

    if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
        local current_major
        current_major="$(java_major_version "${JAVA_HOME}/bin/java")"
        if [ "${current_major}" -ge 17 ]; then
            echo "${JAVA_HOME}"
            return 0
        fi
    fi

    if command -v /usr/libexec/java_home > /dev/null 2>&1; then
        local detected_home
        detected_home="$(/usr/libexec/java_home -v 17+ 2>/dev/null || true)"
        if [ -n "${detected_home}" ] && [ -x "${detected_home}/bin/java" ]; then
            echo "${detected_home}"
            return 0
        fi
    fi

    return 1
}

elasticsearch_pattern() {
    local escaped_dir
    escaped_dir="$(printf '%s\n' "${ELASTICSEARCH_DIR}" | sed 's/[.[\*^$()+?{}|]/\\&/g')"
    echo "org\\.elasticsearch\\.bootstrap\\.Elasticsearch|org\\.elasticsearch\\.server|jdk\\.module\\.main=org\\.elasticsearch\\.server|${escaped_dir}"
}

discover_pid() {
    local pattern pid
    pattern="$(elasticsearch_pattern)"
    pid="$(pgrep -f "${pattern}" | head -n 1)"
    if [ -n "${pid}" ]; then
        echo "${pid}"
        return 0
    fi

    return 1
}

discover_pids() {
    local pattern
    pattern="$(elasticsearch_pattern)"
    pgrep -f "${pattern}" || true
}

port_is_listening() {
    local port="$1"
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN > /dev/null 2>&1
}

elastic_http_status() {
    local scheme="$1"
    curl \
        --silent \
        --output /dev/null \
        --write-out '%{http_code}' \
        --max-time 2 \
        --insecure \
        "${scheme}://127.0.0.1:${ELASTICSEARCH_PORT}" 2>/dev/null || true
}

elastic_health_check() {
    local status

    if [ "${ELASTICSEARCH_SCHEME}" = "https" ]; then
        status="$(elastic_http_status "https")"
        case "${status}" in
            200|401|403) return 0 ;;
        esac
        status="$(elastic_http_status "http")"
        case "${status}" in
            200|401|403) return 0 ;;
        esac
        port_is_listening "${ELASTICSEARCH_PORT}" && return 0
        return 1
    fi

    status="$(elastic_http_status "http")"
    case "${status}" in
        200|401|403) return 0 ;;
    esac
    status="$(elastic_http_status "https")"
    case "${status}" in
        200|401|403) return 0 ;;
    esac
    port_is_listening "${ELASTICSEARCH_PORT}" && return 0
    return 1
}

actual_scheme() {
    local status
    status="$(elastic_http_status "${ELASTICSEARCH_SCHEME}")"
    case "${status}" in
        200|401|403) echo "${ELASTICSEARCH_SCHEME}"; return 0 ;;
    esac

    status="$(elastic_http_status "https")"
    case "${status}" in
        200|401|403) echo "https"; return 0 ;;
    esac

    status="$(elastic_http_status "http")"
    case "${status}" in
        200|401|403) echo "http"; return 0 ;;
    esac

    echo "${ELASTICSEARCH_SCHEME}"
}

is_running() {
    if elastic_health_check; then
        local discovered_pid
        discovered_pid="$(discover_pid || true)"
        if [ -n "${discovered_pid}" ]; then
            echo "${discovered_pid}" > "${PID_FILE}"
        fi
        return 0
    fi

    if [ -f "${PID_FILE}" ]; then
        local pid
        pid="$(cat "${PID_FILE}")"
        if [ -n "${pid}" ] && ps -p "${pid}" > /dev/null 2>&1; then
            return 0
        fi
    fi

    local discovered_pid
    discovered_pid="$(discover_pid || true)"
    if [ -n "${discovered_pid}" ]; then
        echo "${discovered_pid}" > "${PID_FILE}"
        return 0
    fi

    rm -f "${PID_FILE}"
    return 1
}

wait_for_elasticsearch() {
    local i
    for i in $(seq 1 30); do
        if elastic_health_check; then
            return 0
        fi

        if [ -f "${PID_FILE}" ]; then
            local pid
            pid="$(cat "${PID_FILE}")"
            if [ -n "${pid}" ] && ! ps -p "${pid}" > /dev/null 2>&1; then
                return 1
            fi
        fi

        sleep 1
    done

    return 1
}

validate_elasticsearch() {
    if [ ! -d "${ELASTICSEARCH_DIR}" ]; then
        echo "Elasticsearch 目录不存在: ${ELASTICSEARCH_DIR}"
        echo "可通过 ES_HOME=/path/to/elasticsearch 指定本机安装目录"
        return 1
    fi

    if [ ! -x "${ELASTICSEARCH_BIN}" ]; then
        echo "Elasticsearch 启动文件不存在或不可执行: ${ELASTICSEARCH_BIN}"
        echo "可通过 ES_BIN=/path/to/bin/elasticsearch 指定启动文件"
        return 1
    fi

    if ! detect_java_home > /dev/null; then
        echo "启动 Elasticsearch 需要 Java 17+，但当前未找到可用的 JDK"
        return 1
    fi

    return 0
}

start_elasticsearch() {
    validate_elasticsearch || return 1

    if is_running; then
        echo "Elasticsearch 已在运行，PID: $(cat "${PID_FILE}" 2>/dev/null || echo unknown)"
        status_elasticsearch
        return 0
    fi

    local java_home
    java_home="$(detect_java_home)" || return 1

    echo "启动 Elasticsearch..."
    echo "目录: ${ELASTICSEARCH_DIR}"
    echo "Java: ${java_home}"
    echo "日志: ${LOG_FILE}"

    (
        cd "${ELASTICSEARCH_DIR}" || exit 1
        export ES_JAVA_HOME="${java_home}"
        if command -v setsid > /dev/null 2>&1; then
            nohup setsid bash -lc "ES_JAVA_OPTS='${ELASTICSEARCH_JAVA_OPTS}' '${ELASTICSEARCH_BIN}' -E cluster.name='${ELASTICSEARCH_CLUSTER_NAME}' -E http.port='${ELASTICSEARCH_PORT}' -E transport.port='${ELASTICSEARCH_TRANSPORT_PORT}'" >> "${LOG_FILE}" 2>&1 < /dev/null &
        else
            nohup bash -lc "ES_JAVA_OPTS='${ELASTICSEARCH_JAVA_OPTS}' '${ELASTICSEARCH_BIN}' -E cluster.name='${ELASTICSEARCH_CLUSTER_NAME}' -E http.port='${ELASTICSEARCH_PORT}' -E transport.port='${ELASTICSEARCH_TRANSPORT_PORT}'" >> "${LOG_FILE}" 2>&1 < /dev/null &
        fi
        echo $! > "${PID_FILE}"
    )

    if wait_for_elasticsearch && is_running; then
        echo "Elasticsearch 启动成功，PID: $(cat "${PID_FILE}")"
        echo "访问: $(actual_scheme)://127.0.0.1:${ELASTICSEARCH_PORT}"
        echo "日志: ${LOG_FILE}"
        return 0
    fi

    echo "Elasticsearch 启动失败，请检查日志: ${LOG_FILE}"
    tail -40 "${LOG_FILE}" 2>/dev/null || true
    return 1
}

stop_elasticsearch() {
    if ! is_running; then
        echo "Elasticsearch 未运行"
        return 0
    fi

    local pids
    pids="$(discover_pids)"
    if [ -z "${pids}" ] && [ -f "${PID_FILE}" ]; then
        pids="$(cat "${PID_FILE}")"
    fi

    echo "停止 Elasticsearch (PID: ${pids//$'\n'/ })..."
    while IFS= read -r pid; do
        [ -n "${pid}" ] || continue
        kill "${pid}" 2>/dev/null || true
    done <<< "${pids}"

    for _ in 1 2 3 4 5; do
        if ! is_running; then
            rm -f "${PID_FILE}"
            echo "Elasticsearch 已停止"
            return 0
        fi
        sleep 1
    done

    echo "Elasticsearch 未在预期时间内退出，执行强制停止"
    while IFS= read -r pid; do
        [ -n "${pid}" ] || continue
        kill -9 "${pid}" 2>/dev/null || true
    done <<< "${pids}"
    rm -f "${PID_FILE}"
    echo "Elasticsearch 已强制停止"
}

status_elasticsearch() {
    if is_running; then
        local pid scheme
        pid="$(cat "${PID_FILE}" 2>/dev/null || echo unknown)"
        scheme="$(actual_scheme)"
        echo "Elasticsearch: 运行中 (PID: ${pid}, Port: ${ELASTICSEARCH_PORT}, URL: ${scheme}://127.0.0.1:${ELASTICSEARCH_PORT})"
        return 0
    fi

    echo "Elasticsearch: 未运行"
}

logs_elasticsearch() {
    if [ ! -f "${LOG_FILE}" ]; then
        echo "Elasticsearch 暂无日志: ${LOG_FILE}"
        return 1
    fi

    tail -f "${LOG_FILE}"
}

show_url() {
    echo "Elasticsearch: $(actual_scheme)://127.0.0.1:${ELASTICSEARCH_PORT}"
}

show_help() {
    cat <<'EOF'
用法:
  ./start-elasticsearch.sh start
  ./start-elasticsearch.sh stop
  ./start-elasticsearch.sh restart
  ./start-elasticsearch.sh status
  ./start-elasticsearch.sh logs
  ./start-elasticsearch.sh url

说明:
  默认只管理本机 Elasticsearch，不启动 Docker、MinIO、Kafka。

可选环境变量:
  ES_HOME=/path/to/elasticsearch
  ES_BIN=/path/to/elasticsearch/bin/elasticsearch
  ELASTICSEARCH_SCHEME=https|http
  ES_HTTP_PORT=9200
  ES_TRANSPORT_PORT=9300
  ES_JAVA_OPTS="-Xms500M -Xmx500M"
  ES_CLUSTER_NAME=elasticsearch
EOF
}

main() {
    local command="${1:-start}"
    case "${command}" in
        start)
            start_elasticsearch
            ;;
        stop)
            stop_elasticsearch
            ;;
        restart)
            stop_elasticsearch
            start_elasticsearch
            ;;
        status)
            status_elasticsearch
            ;;
        logs)
            logs_elasticsearch
            ;;
        url|urls)
            show_url
            ;;
        -h|--help|help)
            show_help
            ;;
        *)
            echo "不支持的命令: ${command}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

main "$@"
