#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

SECRET_VALUE="unit-secret-must-not-appear"

run_expect_fail() {
  local name="$1"
  local expected="$2"
  shift 2
  local output
  if output="$("$@" 2>&1)"; then
    printf 'miniapp release gates test: expected failure for %s\n' "${name}" >&2
    exit 1
  fi
  if ! printf '%s' "${output}" | rg -q "${expected}"; then
    printf 'miniapp release gates test: expected %s for %s, got:\n%s\n' "${expected}" "${name}" "${output}" >&2
    exit 1
  fi
  if printf '%s' "${output}" | rg -q "${SECRET_VALUE}"; then
    printf 'miniapp release gates test: secret leaked for %s\n' "${name}" >&2
    exit 1
  fi
}

run_expect_fail "missing appid" "missing env: WECHAT_MINI_APP_ID" \
  env -i PATH="${PATH}" HOME="${HOME}" bash scripts/run-miniapp-release-gates.sh

run_expect_fail "appid mismatch" "frontend AppID wx12.*backend AppID wxab" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wxabcdef1234567890 \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "tourist frontend appid" "WECHAT_MINI_APP_ID must be a real wx AppID" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=touristappid \
    PAICODING_WX_MINI_APP_ID=touristappid \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "invalid backend appid" "PAICODING_WX_MINI_APP_ID must be a real wx AppID" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=not-a-real-appid \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "mock enabled" "PAICODING_WX_MINI_MOCK_ENABLED=true is forbidden" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    PAICODING_WX_MINI_MOCK_ENABLED=true \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "missing base url" "missing env: BASE_URL" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "missing smoke login code" "missing env: SMOKE_LOGIN_CODE" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    bash scripts/run-miniapp-release-gates.sh

run_expect_fail "missing ai validate script" "missing env: AI_VALIDATE_SCRIPT" \
  env -i PATH="${PATH}" HOME="${HOME}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    bash scripts/run-miniapp-release-gates.sh

smoke_line="$(rg -n "running API smoke" scripts/run-miniapp-release-gates.sh | cut -d: -f1 | head -1)"
readiness_line="$(rg -n "running strict readiness" scripts/run-miniapp-release-gates.sh | cut -d: -f1 | head -1)"
if [ -z "${smoke_line}" ] || [ -z "${readiness_line}" ] || [ "${smoke_line}" -ge "${readiness_line}" ]; then
  printf 'miniapp release gates test: API smoke must run before strict readiness so pre-api-smoke evidence can exist\n' >&2
  exit 1
fi

if ! rg -q 'pre-api-smoke\.\$\(date' scripts/run-miniapp-release-gates.sh; then
  printf 'miniapp release gates test: release gate must write pre-api-smoke evidence\n' >&2
  exit 1
fi

tmpdir="$(mktemp -d)"
trap 'rm -rf "${tmpdir}"' EXIT
mkdir -p "${tmpdir}/scripts" "${tmpdir}/paicoding-miniapp/cli-agent-run"
cp scripts/run-miniapp-release-gates.sh "${tmpdir}/scripts/run-miniapp-release-gates.sh"
cp scripts/write-miniapp-ai-evidence.py "${tmpdir}/scripts/write-miniapp-ai-evidence.py"

cat >"${tmpdir}/scripts/configure-miniapp-appid.sh" <<'STUB'
#!/usr/bin/env bash
set -euo pipefail
printf 'configure\n' >>"${ORDER_LOG}"
printf '[stub] configure appid\n'
STUB

cat >"${tmpdir}/scripts/preflight-miniapp.sh" <<'STUB'
#!/usr/bin/env bash
set -euo pipefail
if [ "${RUN_AI_VALIDATE:-false}" != "true" ]; then
  printf 'preflight expected RUN_AI_VALIDATE=true\n' >&2
  exit 1
fi
if [ -z "${AI_VALIDATE_SCRIPT:-}" ]; then
  printf 'preflight expected AI_VALIDATE_SCRIPT\n' >&2
  exit 1
fi
mkdir -p paicoding-miniapp/cli-agent-run
cat >paicoding-miniapp/cli-agent-run/validate-report.json <<'JSON'
{
  "summary": {
    "errors": 0,
    "warnings": 0,
    "buildStatus": "pass"
  },
  "build": {
    "stage": "done"
  },
  "execute": {
    "searchArticles": {
      "status": "pass"
    },
    "getArticleDetail": {
      "status": "pass"
    }
  },
  "render": {
    "article-card": {
      "status": "pass"
    }
  }
}
JSON
printf 'preflight\n' >>"${ORDER_LOG}"
printf '[stub] preflight\n'
STUB

cat >"${tmpdir}/scripts/smoke-miniapp-api.sh" <<'STUB'
#!/usr/bin/env bash
set -euo pipefail
if [ "${ALLOW_REMOTE_SMOKE:-false}" != "true" ]; then
  printf 'smoke expected ALLOW_REMOTE_SMOKE=true\n' >&2
  exit 1
fi
if [ "${BASE_URL:-}" != "https://pre.example.com" ]; then
  printf 'smoke expected BASE_URL=https://pre.example.com\n' >&2
  exit 1
fi
if [ "${SMOKE_LOGIN_CODE:-}" != "real-login-code" ]; then
  printf 'smoke expected SMOKE_LOGIN_CODE=real-login-code\n' >&2
  exit 1
fi
printf 'smoke\n' >>"${ORDER_LOG}"
printf 'miniapp smoke: ok, article_id=1001\n'
STUB

cat >"${tmpdir}/scripts/check-miniapp-readiness.sh" <<'STUB'
#!/usr/bin/env bash
set -euo pipefail
if [ "${STRICT:-false}" != "true" ]; then
  printf 'readiness expected STRICT=true\n' >&2
  exit 1
fi
test -s paicoding-miniapp/evidence/ai-preview.validate.md
test -s paicoding-miniapp/evidence/ai-execute-render.validate.md
find paicoding-miniapp/evidence -maxdepth 1 -type f -name 'pre-api-smoke.*.md' -size +0c | rg -q .
printf 'readiness\n' >>"${ORDER_LOG}"
printf '[stub] readiness\n'
STUB

chmod +x "${tmpdir}/scripts/"*.sh
ORDER_LOG="${tmpdir}/order.log"
touch "${ORDER_LOG}"
output="$(
  cd "${tmpdir}" && env \
    PATH="${PATH}" \
    HOME="${HOME}" \
    ORDER_LOG="${ORDER_LOG}" \
    WECHAT_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_ID=wx1234567890abcdef \
    PAICODING_WX_MINI_APP_SECRET="${SECRET_VALUE}" \
    PAICODING_WX_MINI_MOCK_ENABLED=false \
    BASE_URL=https://pre.example.com \
    SMOKE_LOGIN_CODE=real-login-code \
    AI_VALIDATE_SCRIPT=/opt/reviewed/wxa-skills-validate/scripts/validate.mjs \
    bash scripts/run-miniapp-release-gates.sh 2>&1
)"

if printf '%s' "${output}" | rg -q "${SECRET_VALUE}"; then
  printf 'miniapp release gates test: secret leaked during success path\n%s\n' "${output}" >&2
  exit 1
fi

expected_order=$'configure\npreflight\nsmoke\nreadiness'
actual_order="$(cat "${ORDER_LOG}")"
if [ "${actual_order}" != "${expected_order}" ]; then
  printf 'miniapp release gates test: unexpected success-path order\nexpected:\n%s\nactual:\n%s\noutput:\n%s\n' \
    "${expected_order}" "${actual_order}" "${output}" >&2
  exit 1
fi

if ! test -s "${tmpdir}/paicoding-miniapp/evidence/ai-preview.validate.md"; then
  printf 'miniapp release gates test: missing ai-preview evidence\n' >&2
  exit 1
fi
if ! test -s "${tmpdir}/paicoding-miniapp/evidence/ai-execute-render.validate.md"; then
  printf 'miniapp release gates test: missing ai-execute-render evidence\n' >&2
  exit 1
fi
if ! find "${tmpdir}/paicoding-miniapp/evidence" -maxdepth 1 -type f -name 'pre-api-smoke.*.md' -size +0c | rg -q .; then
  printf 'miniapp release gates test: missing pre-api-smoke evidence\n' >&2
  exit 1
fi

tmpdir_no_render="$(mktemp -d)"
trap 'rm -rf "${tmpdir}" "${tmpdir_no_render}"' EXIT
mkdir -p "${tmpdir_no_render}/scripts" "${tmpdir_no_render}/paicoding-miniapp/cli-agent-run"
cp scripts/write-miniapp-ai-evidence.py "${tmpdir_no_render}/scripts/write-miniapp-ai-evidence.py"
cat >"${tmpdir_no_render}/paicoding-miniapp/cli-agent-run/validate-report.json" <<'JSON'
{
  "summary": {
    "errors": 0,
    "warnings": 0,
    "buildStatus": "pass"
  },
  "build": {
    "stage": "done"
  }
}
JSON
python3 "${tmpdir_no_render}/scripts/write-miniapp-ai-evidence.py" \
  "${tmpdir_no_render}/paicoding-miniapp/cli-agent-run/validate-report.json" \
  "${tmpdir_no_render}/paicoding-miniapp/evidence"
if ! test -s "${tmpdir_no_render}/paicoding-miniapp/evidence/ai-preview.validate.md"; then
  printf 'miniapp release gates test: preview evidence should still be written without execute/render\n' >&2
  exit 1
fi
if test -e "${tmpdir_no_render}/paicoding-miniapp/evidence/ai-execute-render.validate.md"; then
  printf 'miniapp release gates test: execute/render evidence must not be forged when report lacks execute/render sections\n' >&2
  exit 1
fi

printf 'miniapp release gates tests: ok\n'
