#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAVEN_BIN="${MAVEN_BIN:-/opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn}"
PORT="${MINIAPP_E2E_PORT:-}"
PORT_START="${MINIAPP_E2E_PORT_START:-18080}"
PORT_LIMIT="${MINIAPP_E2E_PORT_LIMIT:-18120}"
START_TIMEOUT_SECONDS="${MINIAPP_E2E_START_TIMEOUT_SECONDS:-180}"
LOG_DIR="${MINIAPP_E2E_LOG_DIR:-${ROOT_DIR}/.runtime/miniapp-e2e}"
RUN_AVATAR_SMOKE="${RUN_AVATAR_SMOKE:-false}"
AVATAR_FILE="${AVATAR_FILE:-}"
DEV_PORT_FILE="${ROOT_DIR}/.dev-port"

log() {
  printf '[miniapp-local-e2e] %s\n' "$*"
}

is_port_free() {
  ! lsof -iTCP:"$1" -sTCP:LISTEN -n -P >/dev/null 2>&1
}

choose_port() {
  if [ -n "${PORT}" ]; then
    if ! is_port_free "${PORT}"; then
      printf '[miniapp-local-e2e] requested port is already in use: %s\n' "${PORT}" >&2
      exit 1
    fi
    return
  fi
  local candidate
  candidate="${PORT_START}"
  while [ "${candidate}" -le "${PORT_LIMIT}" ]; do
    if is_port_free "${candidate}"; then
      PORT="${candidate}"
      return
    fi
    candidate=$((candidate + 1))
  done
  printf '[miniapp-local-e2e] no free local port found in %s-%s\n' "${PORT_START}" "${PORT_LIMIT}" >&2
  exit 1
}

wait_for_backend() {
  local url="$1"
  local deadline=$((SECONDS + START_TIMEOUT_SECONDS))
  while [ "${SECONDS}" -lt "${deadline}" ]; do
    if curl -fsS "${url}/mini/api/categories" >/dev/null 2>&1; then
      return 0
    fi
    if ! kill -0 "${APP_PID}" >/dev/null 2>&1; then
      return 1
    fi
    sleep 2
  done
  return 1
}

cleanup() {
  if [ -n "${APP_PID:-}" ] && kill -0 "${APP_PID}" >/dev/null 2>&1; then
    log "stopping backend pid=${APP_PID}"
    kill "${APP_PID}" >/dev/null 2>&1 || true
    wait "${APP_PID}" >/dev/null 2>&1 || true
  fi
  if [ "${HAD_DEV_PORT_FILE:-false}" = "true" ]; then
    printf '%s' "${OLD_DEV_PORT_VALUE}" >"${DEV_PORT_FILE}"
  else
    rm -f "${DEV_PORT_FILE}"
  fi
}

choose_port
mkdir -p "${LOG_DIR}"
RUN_ID="$(date '+%Y%m%d-%H%M%S')"
BUILD_LOG="${LOG_DIR}/local-e2e.${RUN_ID}.build.log"
CLASSPATH_FILE="${LOG_DIR}/local-e2e.${RUN_ID}.classpath.txt"
APP_LOG="${LOG_DIR}/local-e2e.${RUN_ID}.backend.log"
SMOKE_LOG="${LOG_DIR}/local-e2e.${RUN_ID}.smoke.log"
BASE_URL="http://127.0.0.1:${PORT}"

JAVA8_HOME="${JAVA8_HOME:-$(/usr/libexec/java_home -v 1.8)}"
export JAVA_HOME="${JAVA8_HOME}"
export PATH="${JAVA8_HOME}/bin:${PATH}"
export PAICODING_WX_MINI_MOCK_ENABLED="${PAICODING_WX_MINI_MOCK_ENABLED:-true}"

cd "${ROOT_DIR}"
trap cleanup EXIT

if [ -f "${DEV_PORT_FILE}" ]; then
  HAD_DEV_PORT_FILE=true
  OLD_DEV_PORT_VALUE="$(cat "${DEV_PORT_FILE}")"
else
  HAD_DEV_PORT_FILE=false
  OLD_DEV_PORT_VALUE=""
fi
printf '%s' "${PORT}" >"${DEV_PORT_FILE}"

log "packaging paicoding-web with Java 8"
log "build log: ${BUILD_LOG}"
if ! "${MAVEN_BIN}" -Pdev -pl paicoding-web -am -DskipTests package >"${BUILD_LOG}" 2>&1; then
  printf '[miniapp-local-e2e] backend package failed; see %s\n' "${BUILD_LOG}" >&2
  tail -n 80 "${BUILD_LOG}" >&2 || true
  exit 1
fi

if ! "${MAVEN_BIN}" -q -Pdev -pl paicoding-web -DskipTests \
  dependency:build-classpath \
  -DincludeScope=runtime \
  -Dmdep.outputFile="${CLASSPATH_FILE}" >>"${BUILD_LOG}" 2>&1; then
  printf '[miniapp-local-e2e] runtime classpath generation failed; see %s\n' "${BUILD_LOG}" >&2
  tail -n 80 "${BUILD_LOG}" >&2 || true
  exit 1
fi

if [ ! -s "${CLASSPATH_FILE}" ]; then
  printf '[miniapp-local-e2e] missing runtime classpath file: %s\n' "${CLASSPATH_FILE}" >&2
  exit 1
fi
MODULE_CLASSPATH="paicoding-web/target/classes:paicoding-ui/target/classes:paicoding-service/target/classes:paicoding-core/target/classes:paicoding-api/target/classes"
DEPENDENCY_CLASSPATH="$(tr ':' '\n' <"${CLASSPATH_FILE}" \
  | rg -v '/com/github/paicoding/forum/paicoding-(ui|api|core|service|web)/' \
  | paste -sd ':' -)"
RUNTIME_CLASSPATH="${MODULE_CLASSPATH}:${DEPENDENCY_CLASSPATH}"

log "starting paicoding-web on ${BASE_URL}"
log "backend log: ${APP_LOG}"
"${JAVA_HOME}/bin/java" -Dfile.encoding=UTF-8 -cp "${RUNTIME_CLASSPATH}" \
  com.github.paicoding.forum.web.QuickForumApplication \
  --server.port="${PORT}" >"${APP_LOG}" 2>&1 &
APP_PID="$!"

if ! wait_for_backend "${BASE_URL}"; then
  printf '[miniapp-local-e2e] backend did not become ready; see %s\n' "${APP_LOG}" >&2
  tail -n 80 "${APP_LOG}" >&2 || true
  exit 1
fi

log "backend ready; running smoke"
if [ "${RUN_AVATAR_SMOKE}" = "true" ]; then
  if [ -z "${AVATAR_FILE}" ]; then
    printf '[miniapp-local-e2e] RUN_AVATAR_SMOKE=true requires AVATAR_FILE\n' >&2
    exit 1
  fi
  RUN_MUTATION_SMOKE=true BASE_URL="${BASE_URL}" AVATAR_FILE="${AVATAR_FILE}" \
    scripts/smoke-miniapp-api.sh | tee "${SMOKE_LOG}"
else
  RUN_MUTATION_SMOKE=true BASE_URL="${BASE_URL}" \
    scripts/smoke-miniapp-api.sh | tee "${SMOKE_LOG}"
fi

log "passed; smoke log: ${SMOKE_LOG}"
