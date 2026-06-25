#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_URL="${BASE_URL:-}"
EVIDENCE_DIR="${EVIDENCE_DIR:-${ROOT_DIR}/paicoding-miniapp/evidence}"
VALIDATE_REPORT="${VALIDATE_REPORT:-${ROOT_DIR}/paicoding-miniapp/cli-agent-run/validate-report.json}"

usage() {
  cat <<'EOF'
Usage:
  WECHAT_MINI_APP_ID=wx... \
  PAICODING_WX_MINI_APP_ID=wx... \
  PAICODING_WX_MINI_APP_SECRET=... \
  SMOKE_LOGIN_CODE=real-wx-login-code \
  BASE_URL=https://pre.example.com \
  AI_VALIDATE_SCRIPT=/path/to/reviewed/wxa-skills-validate/scripts/validate.mjs \
  scripts/run-miniapp-release-gates.sh

This script runs release gates only. It does not deploy, submit, or publish.
EOF
}

need_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    printf '[miniapp-release] missing env: %s\n' "${name}" >&2
    return 1
  fi
}

mask_appid() {
  local value="$1"
  printf '%s...%s' "${value:0:4}" "${value: -4}"
}

validate_real_appid() {
  local name="$1"
  local value="$2"
  if [ "${value}" = "touristappid" ] || ! [[ "${value}" =~ ^wx[0-9A-Za-z]{16,32}$ ]]; then
    printf '[miniapp-release] %s must be a real wx AppID, got %s\n' "${name}" "$(mask_appid "${value}")" >&2
    exit 1
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
print(cur)
PY
}

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
  usage
  exit 0
fi

need_env WECHAT_MINI_APP_ID
need_env PAICODING_WX_MINI_APP_ID
need_env PAICODING_WX_MINI_APP_SECRET
need_env BASE_URL
need_env SMOKE_LOGIN_CODE

validate_real_appid WECHAT_MINI_APP_ID "${WECHAT_MINI_APP_ID}"
validate_real_appid PAICODING_WX_MINI_APP_ID "${PAICODING_WX_MINI_APP_ID}"

if [ "${PAICODING_WX_MINI_MOCK_ENABLED:-false}" = "true" ]; then
  printf '[miniapp-release] PAICODING_WX_MINI_MOCK_ENABLED=true is forbidden\n' >&2
  exit 1
fi

if [ "${WECHAT_MINI_APP_ID}" != "${PAICODING_WX_MINI_APP_ID}" ]; then
  printf '[miniapp-release] frontend AppID %s and backend AppID %s differ\n' \
    "$(mask_appid "${WECHAT_MINI_APP_ID}")" "$(mask_appid "${PAICODING_WX_MINI_APP_ID}")" >&2
  exit 1
fi

need_env AI_VALIDATE_SCRIPT

cd "${ROOT_DIR}"

printf '[miniapp-release] configuring miniapp AppID=%s\n' "$(mask_appid "${WECHAT_MINI_APP_ID}")"
scripts/configure-miniapp-appid.sh "${WECHAT_MINI_APP_ID}"

printf '[miniapp-release] running preflight + WeChat AI validate\n'
RUN_AI_VALIDATE=true scripts/preflight-miniapp.sh

mkdir -p "${EVIDENCE_DIR}"
if [ -f "${VALIDATE_REPORT}" ]; then
  python3 scripts/write-miniapp-ai-evidence.py "${VALIDATE_REPORT}" "${EVIDENCE_DIR}"
  printf '[miniapp-release] wrote AI preview evidence: %s\n' "${EVIDENCE_DIR}/ai-preview.validate.md"
  if [ -f "${EVIDENCE_DIR}/ai-execute-render.validate.md" ]; then
    printf '[miniapp-release] wrote AI execute/render evidence: %s\n' "${EVIDENCE_DIR}/ai-execute-render.validate.md"
  else
    printf '[miniapp-release] AI execute/render evidence not written; validate report has no passing execute/render sections\n'
  fi
fi

smoke_evidence="${EVIDENCE_DIR}/pre-api-smoke.$(date '+%Y%m%d-%H%M%S').md"
printf '[miniapp-release] running API smoke against %s\n' "${BASE_URL}"
{
  printf '# Pre API smoke evidence\n\n'
  printf -- '- generatedAt: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %Z')"
  printf -- '- baseUrl: %s\n\n' "${BASE_URL}"
  printf '```text\n'
  ALLOW_REMOTE_SMOKE=true RUN_MUTATION_SMOKE=true BASE_URL="${BASE_URL}" SMOKE_LOGIN_CODE="${SMOKE_LOGIN_CODE}" scripts/smoke-miniapp-api.sh
  printf '```\n'
} | tee "${smoke_evidence}"
printf '[miniapp-release] wrote API smoke evidence: %s\n' "${smoke_evidence}"

printf '[miniapp-release] running strict readiness\n'
STRICT=true scripts/check-miniapp-readiness.sh

printf '[miniapp-release] gates passed; submit/publish still requires maintainer action\n'
