#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MINIAPP_DIR="${MINIAPP_DIR:-${ROOT_DIR}/paicoding-miniapp}"
PROJECT_CONFIG="${PROJECT_CONFIG:-${MINIAPP_DIR}/project.config.json}"
STRICT="${STRICT:-false}"
WECHAT_DEVTOOLS_CLI="${WECHAT_DEVTOOLS_CLI:-/Applications/wechatwebdevtools.app/Contents/MacOS/cli}"
VALIDATE_REPORT="${VALIDATE_REPORT:-${MINIAPP_DIR}/cli-agent-run/validate-report.json}"
EVIDENCE_FILE="${EVIDENCE_FILE:-${MINIAPP_DIR}/ACCEPTANCE_EVIDENCE.md}"
EVIDENCE_DIR="${EVIDENCE_DIR:-${MINIAPP_DIR}/evidence}"
SENSITIVE_VALUE_PATTERN='authorization[[:space:]]*:[[:space:]]*bearer[[:space:]]+[A-Za-z0-9._~+/=-]{8,}|\bbearer[[:space:]]+eyJ[A-Za-z0-9._~+/=-]{8,}|"?(appsecret|app_secret|app secret|token|cookie|openid|session_key|secret)"?\s*[:=]\s*"?[^"[:space:]，,。]+|sk-[A-Za-z0-9_-]{12,}'
EVIDENCE_PASS_PATTERN='status[[:space:]]*:[[:space:]]*pass|状态[[:space:]]*[:：][[:space:]]*通过|验收[[:space:]]*[:：][[:space:]]*通过'

fail_count=0
warn_count=0

info() {
  printf '[miniapp-readiness] %s\n' "$*"
}

pass() {
  printf '[miniapp-readiness] PASS %s\n' "$*"
}

warn() {
  warn_count=$((warn_count + 1))
  printf '[miniapp-readiness] WARN %s\n' "$*" >&2
}

fail() {
  fail_count=$((fail_count + 1))
  printf '[miniapp-readiness] FAIL %s\n' "$*" >&2
}

gate() {
  if [ "${STRICT}" = "true" ]; then
    fail "$@"
  else
    warn "$@"
  fi
}

json_read() {
  local file="$1"
  local expr="$2"
  python3 - "$file" "$expr" <<'PY'
import json
import sys

path, expr = sys.argv[1], sys.argv[2]
with open(path) as f:
    data = json.load(f)
cur = data
for part in expr.split("."):
    if not part:
        continue
    if isinstance(cur, list):
        cur = cur[int(part)]
    else:
        cur = cur.get(part)
    if cur is None:
        print("")
        sys.exit(0)
if isinstance(cur, bool):
    print("true" if cur else "false")
else:
    print(cur)
PY
}

json_assert_true() {
  local file="$1"
  local script="$2"
  python3 - "$file" "$script" <<'PY'
import json
import sys

with open(sys.argv[1]) as f:
    data = json.load(f)
ok = eval(sys.argv[2], {"data": data})
sys.exit(0 if ok else 1)
PY
}

require_file() {
  if [ ! -e "$1" ]; then
    fail "missing file: $1"
    return 1
  fi
}

artifact_exists() {
  local pattern="$1"
  [ -d "${EVIDENCE_DIR}" ] && find "${EVIDENCE_DIR}" -maxdepth 1 -type f -name "${pattern}" -size +0c -print -quit | rg -q .
}

missing_artifact_labels=""
missing_artifact_semantics=""

require_evidence_artifact() {
  local label="$1"
  local semantic="$2"
  shift 2
  local found=false
  local semantic_found=false
  local pattern
  for pattern in "$@"; do
    if artifact_exists "${pattern}"; then
      found=true
      if [ -n "${semantic}" ] && find "${EVIDENCE_DIR}" -maxdepth 1 -type f -name "${pattern}" -size +0c -print0 \
        | xargs -0 rg -q -i "${semantic}" 2>/dev/null; then
        semantic_found=true
      fi
      break
    fi
  done
  if [ "${found}" = false ]; then
    missing_artifact_labels="${missing_artifact_labels} ${label}"
  elif [ -n "${semantic}" ] && [ "${semantic_found}" = false ]; then
    missing_artifact_semantics="${missing_artifact_semantics} ${label}"
  fi
}

scan_sensitive_file() {
  local file="$1"
  rg -n -i "${SENSITIVE_VALUE_PATTERN}" "${file}" >/dev/null
}

scan_sensitive_evidence_dir() {
  [ -d "${EVIDENCE_DIR}" ] && rg -n -i "${SENSITIVE_VALUE_PATTERN}" \
    "${EVIDENCE_DIR}" \
    -g '*.md' -g '*.txt' -g '*.json' -g '*.log' -g '*.csv' >/dev/null
}

cd "${ROOT_DIR}"

info "strict=${STRICT}"

require_file "${PROJECT_CONFIG}"
require_file "${MINIAPP_DIR}/app.json"
require_file "${MINIAPP_DIR}/app.js"
require_file "${MINIAPP_DIR}/utils/config.js"
require_file "${MINIAPP_DIR}/utils/privacy.js"
require_file "${MINIAPP_DIR}/pages/profile/profile.js"
require_file "${MINIAPP_DIR}/pages/profile/profile.wxml"
require_file "${MINIAPP_DIR}/skills/article-search/utils/request.js"
require_file "${ROOT_DIR}/scripts/run-miniapp-release-gates.sh"
require_file "${ROOT_DIR}/scripts/configure-miniapp-appid.sh"

if [ -x "${ROOT_DIR}/scripts/run-miniapp-release-gates.sh" ] && [ -x "${ROOT_DIR}/scripts/configure-miniapp-appid.sh" ]; then
  pass "miniapp release gate scripts are executable"
else
  fail "miniapp release gate scripts must be executable"
fi

appid="$(json_read "${PROJECT_CONFIG}" "appid")"
if [ -n "${appid}" ] && [ "${appid}" != "touristappid" ]; then
  pass "project.config.json appid is configured"
else
  gate "project.config.json still uses touristappid; replace with real AppID before preview/submit"
fi

if [ -n "${PAICODING_WX_MINI_APP_ID:-}" ] && [ -n "${appid}" ] && [ "${appid}" != "touristappid" ]; then
  if [ "${appid}" = "${PAICODING_WX_MINI_APP_ID}" ]; then
    pass "frontend and backend AppID match"
  else
    fail "frontend project.config.json AppID and PAICODING_WX_MINI_APP_ID differ"
  fi
fi

url_check="$(json_read "${PROJECT_CONFIG}" "setting.urlCheck")"
if [ "${url_check}" = "true" ]; then
  pass "project.config.json setting.urlCheck=true"
else
  fail "project.config.json setting.urlCheck must stay true for submit"
fi

if json_assert_true "${PROJECT_CONFIG}" 'any(x.get("type") == "folder" and x.get("value") == "skills" for x in data.get("packOptions", {}).get("include", []))'; then
  pass "project.config.json packOptions include skills"
else
  fail "project.config.json must include skills in packOptions.include"
fi

if json_assert_true "${PROJECT_CONFIG}" 'any(x.get("type") == "folder" and x.get("value") == "cli-agent-run" for x in data.get("packOptions", {}).get("ignore", []))'; then
  pass "project.config.json ignores cli-agent-run"
else
  fail "project.config.json must ignore cli-agent-run"
fi

lazy="$(json_read "${MINIAPP_DIR}/app.json" "lazyCodeLoading")"
if [ "${lazy}" = "requiredComponents" ]; then
  pass "app.json lazyCodeLoading=requiredComponents"
else
  fail "app.json lazyCodeLoading must be requiredComponents"
fi

if json_assert_true "${MINIAPP_DIR}/app.json" 'any(s.get("path") == "skills/article-search" and s.get("description") for s in data.get("agent", {}).get("skills", []))'; then
  pass "app.json agent.skills includes article-search"
else
  fail "app.json must declare agent.skills article-search with description"
fi

if json_assert_true "${MINIAPP_DIR}/app.json" 'any(p.get("root") == "skills" and p.get("independent") is True for p in data.get("subPackages", []))'; then
  pass "app.json has independent skills subpackage"
else
  fail "app.json must declare independent skills subpackage"
fi

if rg -q "forceMockLogin: false" "${MINIAPP_DIR}/utils/config.js"; then
  pass "miniapp forceMockLogin=false"
else
  fail "utils/config.js must keep forceMockLogin=false before submit"
fi

if rg -q "wx\\.onNeedPrivacyAuthorization" "${MINIAPP_DIR}/utils/privacy.js" \
  && rg -q "setupPrivacyAuthorization" "${MINIAPP_DIR}/app.js" \
  && rg -q "setupPrivacyAuthorization" "${MINIAPP_DIR}/pages/profile/profile.js" \
  && rg -q "agreePrivacyAuthorization" "${MINIAPP_DIR}/pages/profile/profile.js" \
  && rg -q 'open-type="agreePrivacyAuthorization"' "${MINIAPP_DIR}/pages/profile/profile.wxml"; then
  pass "miniapp privacy authorization callback is wired"
else
  fail "miniapp must wire wx.onNeedPrivacyAuthorization, app startup setup, and profile agreePrivacyAuthorization"
fi

if rg -q 'open-type="chooseAvatar"' "${MINIAPP_DIR}/pages/profile/profile.wxml" \
  && rg -q 'type="nickname"' "${MINIAPP_DIR}/pages/profile/profile.wxml" \
  && rg -q "openPrivacyContract" "${MINIAPP_DIR}/pages/profile/profile.js"; then
  pass "profile page exposes avatar, nickname, and privacy contract entry"
else
  fail "profile page must expose chooseAvatar, nickname input, and privacy contract entry"
fi

js_api_base_url() {
  local file="$1"
  local env_name="$2"
  python3 - "$file" "$env_name" <<'PY'
import re
import sys

path, env_name = sys.argv[1], sys.argv[2]
text = open(path).read()
match = re.search(r"\b" + re.escape(env_name) + r"\s*:\s*['\"]([^'\"]+)['\"]", text)
print(match.group(1) if match else "")
PY
}

main_trial_url="$(js_api_base_url "${MINIAPP_DIR}/utils/config.js" trial)"
main_release_url="$(js_api_base_url "${MINIAPP_DIR}/utils/config.js" release)"
skill_trial_url="$(js_api_base_url "${MINIAPP_DIR}/skills/article-search/utils/request.js" trial)"
skill_release_url="$(js_api_base_url "${MINIAPP_DIR}/skills/article-search/utils/request.js" release)"

if [[ "${main_trial_url}" == https://* && "${main_release_url}" == https://* ]]; then
  pass "trial/release API base URLs use HTTPS"
else
  fail "trial/release API base URLs must use HTTPS"
fi

if [[ "${skill_trial_url}" == https://* && "${skill_release_url}" == https://* ]]; then
  pass "AI Skill trial/release API base URLs use HTTPS"
else
  fail "AI Skill trial/release API base URLs must use HTTPS"
fi

if [ -n "${main_trial_url}" ] && [ "${main_trial_url}" = "${skill_trial_url}" ] \
  && [ -n "${main_release_url}" ] && [ "${main_release_url}" = "${skill_release_url}" ]; then
  pass "main miniapp and AI Skill API base URLs match"
else
  fail "main miniapp and AI Skill API base URLs must match for trial/release"
fi

if [ -x "${WECHAT_DEVTOOLS_CLI}" ]; then
  login_json="$("${WECHAT_DEVTOOLS_CLI}" islogin 2>/dev/null || true)"
  if printf '%s' "${login_json}" | rg -q '"login":true'; then
    pass "WeChat DevTools CLI is logged in"
  else
    gate "WeChat DevTools CLI is not logged in; current result: ${login_json:-empty}"
  fi
else
  gate "WeChat DevTools CLI not found or not executable: ${WECHAT_DEVTOOLS_CLI}"
fi

if [ -f "${VALIDATE_REPORT}" ]; then
  summary_errors="$(json_read "${VALIDATE_REPORT}" "summary.errors")"
  summary_warnings="$(json_read "${VALIDATE_REPORT}" "summary.warnings")"
  build_status="$(json_read "${VALIDATE_REPORT}" "summary.buildStatus")"
  build_stage="$(json_read "${VALIDATE_REPORT}" "build.stage")"
  if [ "${summary_errors}" = "0" ]; then
    pass "AI validate static errors=0 warnings=${summary_warnings:-unknown}"
  else
    fail "AI validate static errors=${summary_errors:-unknown}"
  fi
  if [ "${build_status}" = "pass" ]; then
    pass "AI validate preview build passed"
  else
    gate "AI validate preview buildStatus=${build_status:-missing} stage=${build_stage:-missing}; rerun after DevTools login and real AppID"
  fi
else
  gate "AI validate report missing: ${VALIDATE_REPORT}"
fi

if rg -q "\\[ai-mode\\] article-card overflow monitor=on" "${MINIAPP_DIR}/skills/article-search/components/article-card/index.js"; then
  pass "AI component has overflow monitor baseline log"
else
  fail "AI component must bind overflow monitor and log '[ai-mode] article-card overflow monitor=on' for render validation"
fi

if rg -q 'mockEnabled: \$\{PAICODING_WX_MINI_MOCK_ENABLED:false\}' paicoding-web/src/main/resources-env/pre/application-login.yml \
  && rg -q 'mockEnabled: \$\{PAICODING_WX_MINI_MOCK_ENABLED:false\}' paicoding-web/src/main/resources-env/prod/application-login.yml; then
  pass "pre/prod wx-mini mockEnabled defaults to false"
else
  fail "pre/prod wx-mini mockEnabled must default to false"
fi

if [ -n "${PAICODING_WX_MINI_APP_ID:-}" ]; then
  pass "PAICODING_WX_MINI_APP_ID is present in current shell"
else
  gate "PAICODING_WX_MINI_APP_ID is not present in current shell; pre/prod runtime must provide it"
fi

if [ -n "${PAICODING_WX_MINI_APP_SECRET:-}" ]; then
  pass "PAICODING_WX_MINI_APP_SECRET is present in current shell"
else
  gate "PAICODING_WX_MINI_APP_SECRET is not present in current shell; pre/prod runtime must provide it"
fi

if [ "${PAICODING_WX_MINI_MOCK_ENABLED:-false}" = "true" ]; then
  fail "PAICODING_WX_MINI_MOCK_ENABLED=true is forbidden for pre/prod readiness"
else
  pass "PAICODING_WX_MINI_MOCK_ENABLED is not true"
fi

if [ -f "${EVIDENCE_FILE}" ]; then
  pass "acceptance evidence file exists"
  pending_count="$( (rg -n "未执行|未验证|未确认" "${EVIDENCE_FILE}" || true) | wc -l | tr -d ' ')"
  if [ "${pending_count}" = "0" ]; then
    pass "acceptance evidence has no pending placeholders"
  else
    gate "acceptance evidence still has ${pending_count} pending placeholders"
  fi
  if scan_sensitive_file "${EVIDENCE_FILE}"; then
    fail "acceptance evidence may contain sensitive values; remove secrets/tokens/openid before handoff"
  else
    pass "acceptance evidence sensitive-value scan passed"
  fi
  if rg -n '^- (小程序 AppID 后 6 位|真机设备与微信版本|是否允许提审|未解决问题|回滚确认人)：[[:space:]]*$' "${EVIDENCE_FILE}" >/dev/null; then
    gate "acceptance evidence still has blank required fields"
  else
    pass "acceptance evidence required fields are filled"
  fi
  if rg -n '^- 是否允许提审：[[:space:]]*(是|允许|通过)[[:space:]]*$' "${EVIDENCE_FILE}" >/dev/null; then
    pass "acceptance evidence explicitly allows submit"
  else
    gate "acceptance evidence must set 是否允许提审 to 是/允许/通过 before strict handoff"
  fi
  if rg -n '^- 未解决问题：[[:space:]]*(无|无阻断|没有|none|None|NONE)[[:space:]]*$' "${EVIDENCE_FILE}" >/dev/null; then
    pass "acceptance evidence has no unresolved blocking issues"
  else
    gate "acceptance evidence must set 未解决问题 to 无/无阻断/没有 before strict handoff"
  fi
  require_evidence_artifact "pre-api-smoke" "miniapp smoke:[[:space:]]*ok" "pre-api-smoke.*"
  require_evidence_artifact "device-login" "${EVIDENCE_PASS_PATTERN}" "device-login.*"
  require_evidence_artifact "device-avatar-profile" "${EVIDENCE_PASS_PATTERN}" "device-avatar-profile.*"
  require_evidence_artifact "device-article-flow" "${EVIDENCE_PASS_PATTERN}" "device-article-flow.*"
  require_evidence_artifact "device-interactions" "${EVIDENCE_PASS_PATTERN}" "device-interactions.*"
  require_evidence_artifact "wechat-domain-privacy" "${EVIDENCE_PASS_PATTERN}" "wechat-domain-privacy.*"
  require_evidence_artifact "ai-preview" 'summary\.buildStatus:[[:space:]]*pass|"?buildStatus"?[[:space:]]*:[[:space:]]*"?pass' "ai-preview.*"
  require_evidence_artifact "ai-execute-render" 'execute.*pass.*render|render.*pass.*execute|status[[:space:]]*:[[:space:]]*pass' "ai-execute-render.*"
  if [ -n "${missing_artifact_labels}" ]; then
    gate "acceptance evidence artifact groups missing:${missing_artifact_labels}"
  else
    pass "acceptance evidence artifact groups are present"
  fi
  if [ -n "${missing_artifact_labels}" ]; then
    info "acceptance evidence artifact pass markers skipped until all artifact groups are present"
  elif [ -n "${missing_artifact_semantics}" ]; then
    gate "acceptance evidence artifact pass markers missing:${missing_artifact_semantics}"
  else
    pass "acceptance evidence artifact pass markers are present"
  fi
  if scan_sensitive_evidence_dir; then
    fail "acceptance evidence artifact text files may contain sensitive values; remove secrets/tokens/openid before handoff"
  else
    pass "acceptance evidence artifact sensitive-value scan passed"
  fi
else
  gate "acceptance evidence file missing: ${EVIDENCE_FILE}"
fi

info "summary: failures=${fail_count}, warnings=${warn_count}"
if [ "${fail_count}" -gt 0 ]; then
  exit 1
fi
