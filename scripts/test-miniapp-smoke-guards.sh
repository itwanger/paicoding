#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

if ! rg -q 'json.dumps\(\{"code": sys\.argv\[1\]\}' scripts/smoke-miniapp-api.sh; then
  echo "miniapp smoke guard test: login payload must be JSON-escaped via python json.dumps" >&2
  exit 1
fi

if rg -F -q -- '-d "{\"code\":\"${SMOKE_LOGIN_CODE}\"}"' scripts/smoke-miniapp-api.sh; then
  echo "miniapp smoke guard test: login payload must not be hand-concatenated JSON" >&2
  exit 1
fi

output="$(
  ALLOW_REMOTE_SMOKE=true \
  BASE_URL=https://paicoding.com \
  RUN_MUTATION_SMOKE=false \
  scripts/smoke-miniapp-api.sh 2>&1
)" && {
  echo "miniapp smoke guard test: expected production smoke to be refused" >&2
  exit 1
}

if ! printf '%s' "${output}" | rg -q "refusing production BASE_URL=https://paicoding.com"; then
  echo "miniapp smoke guard test: expected production refusal message, got:" >&2
  printf '%s\n' "${output}" >&2
  exit 1
fi

output="$(
  BASE_URL=https://pre.example.com \
  RUN_MUTATION_SMOKE=false \
  scripts/smoke-miniapp-api.sh 2>&1
)" && {
  echo "miniapp smoke guard test: expected remote smoke without ALLOW_REMOTE_SMOKE to be refused" >&2
  exit 1
}

if ! printf '%s' "${output}" | rg -q "refusing non-local BASE_URL=https://pre.example.com"; then
  echo "miniapp smoke guard test: expected remote refusal message, got:" >&2
  printf '%s\n' "${output}" >&2
  exit 1
fi

output="$(
  ALLOW_REMOTE_SMOKE=true \
  BASE_URL=https://pre.example.com \
  RUN_MUTATION_SMOKE=false \
  scripts/smoke-miniapp-api.sh 2>&1
)" && {
  echo "miniapp smoke guard test: expected remote smoke without SMOKE_LOGIN_CODE to be refused" >&2
  exit 1
}

if ! printf '%s' "${output}" | rg -q "remote BASE_URL requires SMOKE_LOGIN_CODE"; then
  echo "miniapp smoke guard test: expected remote login-code refusal message, got:" >&2
  printf '%s\n' "${output}" >&2
  exit 1
fi

output="$(
  ALLOW_REMOTE_SMOKE=true \
  BASE_URL=https://pre.example.com \
  SMOKE_LOGIN_CODE=mock-smoke-fake \
  RUN_MUTATION_SMOKE=false \
  scripts/smoke-miniapp-api.sh 2>&1
)" && {
  echo "miniapp smoke guard test: expected remote fake SMOKE_LOGIN_CODE to be refused" >&2
  exit 1
}

if ! printf '%s' "${output}" | rg -q "mock/test/fake codes are forbidden"; then
  echo "miniapp smoke guard test: expected fake login-code refusal message, got:" >&2
  printf '%s\n' "${output}" >&2
  exit 1
fi

output="$(
  ALLOW_REMOTE_SMOKE=true \
  BASE_URL=https://pre.example.com \
  SMOKE_LOGIN_CODE=real-test-code \
  RUN_MUTATION_SMOKE=false \
  scripts/smoke-miniapp-api.sh 2>&1
)" && {
  echo "miniapp smoke guard test: expected remote test-like SMOKE_LOGIN_CODE to be refused" >&2
  exit 1
}

if ! printf '%s' "${output}" | rg -q "mock/test/fake codes are forbidden"; then
  echo "miniapp smoke guard test: expected test-like login-code refusal message, got:" >&2
  printf '%s\n' "${output}" >&2
  exit 1
fi

echo "miniapp smoke guard tests: ok"
