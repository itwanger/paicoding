#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MINIAPP_DIR="${ROOT_DIR}/paicoding-miniapp"
MAVEN_BIN="${MAVEN_BIN:-/opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn}"
RUN_JAVA_TESTS="${RUN_JAVA_TESTS:-true}"
RUN_API_SMOKE="${RUN_API_SMOKE:-false}"
RUN_AI_VALIDATE="${RUN_AI_VALIDATE:-false}"
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
AI_VALIDATE_SCRIPT="${AI_VALIDATE_SCRIPT:-}"
WECHAT_DEVTOOLS_CLI="${WECHAT_DEVTOOLS_CLI:-/Applications/wechatwebdevtools.app/Contents/MacOS/cli}"

log() {
  printf '\n[miniapp-preflight] %s\n' "$*"
}

require_file() {
  if [ ! -e "$1" ]; then
    printf '[miniapp-preflight] missing: %s\n' "$1" >&2
    exit 1
  fi
}

require_file "${MINIAPP_DIR}/project.config.json"
require_file "${MINIAPP_DIR}/app.json"

cd "${ROOT_DIR}"

log "checking miniapp JavaScript syntax"
find paicoding-miniapp -name '*.js' -print0 | xargs -0 -n1 node --check

log "checking miniapp JSON syntax"
find paicoding-miniapp -name '*.json' -print0 | xargs -0 -n1 python3 -m json.tool >/dev/null

log "checking shell scripts"
bash -n scripts/smoke-miniapp-api.sh
bash -n scripts/preflight-miniapp.sh
bash -n scripts/check-miniapp-readiness.sh
bash -n scripts/test-miniapp-smoke-guards.sh
bash -n scripts/configure-miniapp-appid.sh
bash -n scripts/test-miniapp-configure-appid.sh
bash -n scripts/run-miniapp-release-gates.sh
bash -n scripts/test-miniapp-release-gates.sh
bash -n scripts/run-miniapp-local-e2e.sh
bash -n scripts/test-miniapp-local-e2e.sh
bash -n scripts/test-miniapp-readiness-evidence.sh
bash -n scripts/test-miniapp-readiness-core.sh
bash -n scripts/test-miniapp-ai-validate-safety.sh
bash -n scripts/test-miniapp-ai-evidence-writer.sh
bash -n scripts/test-miniapp-evidence-writer.sh
python3 -m py_compile scripts/write-miniapp-ai-evidence.py
python3 -m py_compile scripts/write-miniapp-evidence.py

log "checking miniapp auth retry logic"
node --check scripts/test-miniapp-auth.js
node scripts/test-miniapp-auth.js

log "checking miniapp detail page login boundaries"
node --check scripts/test-miniapp-detail-page.js
node scripts/test-miniapp-detail-page.js

log "checking miniapp profile page avatar boundaries"
node --check scripts/test-miniapp-profile-page.js
node scripts/test-miniapp-profile-page.js

log "checking miniapp collection page behavior"
node --check scripts/test-miniapp-collection-page.js
node scripts/test-miniapp-collection-page.js

log "checking miniapp history page behavior"
node --check scripts/test-miniapp-history-page.js
node scripts/test-miniapp-history-page.js

log "checking miniapp feed/search page behavior"
node --check scripts/test-miniapp-feed-search-pages.js
node scripts/test-miniapp-feed-search-pages.js

log "checking miniapp AI Skill API contracts"
node --check scripts/test-miniapp-ai-skill.js
node scripts/test-miniapp-ai-skill.js

log "checking miniapp AI Skill read-only boundary"
node --check scripts/test-miniapp-ai-boundary.js
node scripts/test-miniapp-ai-boundary.js

log "checking miniapp API base URL consistency"
node --check scripts/test-miniapp-config-consistency.js
node scripts/test-miniapp-config-consistency.js

log "checking miniapp smoke safety guards"
scripts/test-miniapp-smoke-guards.sh

log "checking miniapp AppID configurator"
scripts/test-miniapp-configure-appid.sh

log "checking miniapp release gates"
scripts/test-miniapp-release-gates.sh

log "checking miniapp local e2e runner"
scripts/test-miniapp-local-e2e.sh

log "checking miniapp readiness evidence gates"
scripts/test-miniapp-readiness-evidence.sh

log "checking miniapp readiness core gates"
scripts/test-miniapp-readiness-core.sh

log "checking miniapp AI validate safety gates"
scripts/test-miniapp-ai-validate-safety.sh

log "checking miniapp AI evidence writer"
scripts/test-miniapp-ai-evidence-writer.sh

log "checking miniapp handoff evidence writer"
scripts/test-miniapp-evidence-writer.sh

log "checking git whitespace"
git diff --check

if [ "${RUN_JAVA_TESTS}" = "true" ]; then
  log "running Java 8 miniapp tests"
  JAVA8_HOME="${JAVA8_HOME:-$(/usr/libexec/java_home -v 1.8)}"
  export JAVA_HOME="${JAVA8_HOME}"
  export PATH="${JAVA8_HOME}/bin:${PATH}"
  "${MAVEN_BIN}" -pl paicoding-web -am -DfailIfNoTests=false \
    -Dtest=GlobalInitServiceMiniProgramTest,ReqRecordFilterMiniProgramTest,WxMiniProgramAuthServiceTest,WxMiniProgramRestControllerTest,DynamicConfigContainerRedactionTest test

  log "checking Java 8 bytecode descriptors"
  descriptor_report="$(mktemp)"
  "${JAVA_HOME}/bin/javap" \
    -classpath "paicoding-web/target/classes:paicoding-service/target/classes:paicoding-api/target/classes" \
    -s -p \
    com.github.paicoding.forum.web.front.miniprogram.rest.WxMiniProgramRestController \
    com.github.paicoding.forum.web.front.miniprogram.service.WxMiniProgramAuthService \
    >"${descriptor_report}"
  if rg 'L(ArticleReadService|CategoryService|UserFootService|UserService|SessionDeviceMeta);' "${descriptor_report}"; then
    printf '[miniapp-preflight] bad short Java descriptor detected\n' >&2
    rm -f "${descriptor_report}"
    exit 1
  fi
  rm -f "${descriptor_report}"
else
  log "skipping Java tests: RUN_JAVA_TESTS=${RUN_JAVA_TESTS}"
fi

if [ "${RUN_API_SMOKE}" = "true" ]; then
  log "running miniapp API smoke against ${BASE_URL}"
  BASE_URL="${BASE_URL}" bash scripts/smoke-miniapp-api.sh
else
  log "skipping API smoke: RUN_API_SMOKE=${RUN_API_SMOKE}"
fi

if [ "${RUN_AI_VALIDATE}" = "true" ]; then
  if [ -z "${AI_VALIDATE_SCRIPT}" ]; then
    printf '[miniapp-preflight] AI_VALIDATE_SCRIPT is required when RUN_AI_VALIDATE=true; use a reviewed checkout of the official validator outside /tmp\n' >&2
    exit 1
  fi
  case "${AI_VALIDATE_SCRIPT}" in
    /tmp/*|/private/tmp/*)
      printf '[miniapp-preflight] refusing AI_VALIDATE_SCRIPT under temporary directory: %s\n' "${AI_VALIDATE_SCRIPT}" >&2
      exit 1
      ;;
  esac
  require_file "${AI_VALIDATE_SCRIPT}"
  require_file "${WECHAT_DEVTOOLS_CLI}"
  log "running WeChat AI Skill validate"
  env -u PAICODING_WX_MINI_APP_SECRET \
    -u PAICODING_WX_MINI_SESSION_SECRET \
    -u PAICODING_WX_MINI_TOKEN \
    node "${AI_VALIDATE_SCRIPT}" "${MINIAPP_DIR}" --cli-path "${WECHAT_DEVTOOLS_CLI}"
else
  log "skipping WeChat AI validate: RUN_AI_VALIDATE=${RUN_AI_VALIDATE}"
fi

log "done"
