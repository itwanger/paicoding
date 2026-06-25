#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

if rg -q "find /tmp" scripts/preflight-miniapp.sh; then
  printf 'miniapp ai validate safety test: preflight must not discover validators from /tmp\n' >&2
  exit 1
fi

if ! rg -q "AI_VALIDATE_SCRIPT is required when RUN_AI_VALIDATE=true" scripts/preflight-miniapp.sh; then
  printf 'miniapp ai validate safety test: preflight must require explicit AI_VALIDATE_SCRIPT\n' >&2
  exit 1
fi

if ! rg -q 'refusing AI_VALIDATE_SCRIPT under temporary directory' scripts/preflight-miniapp.sh; then
  printf 'miniapp ai validate safety test: preflight must reject temporary validator paths\n' >&2
  exit 1
fi

if ! rg -q -- '-u PAICODING_WX_MINI_APP_SECRET' scripts/preflight-miniapp.sh; then
  printf 'miniapp ai validate safety test: preflight must remove AppSecret from AI validator env\n' >&2
  exit 1
fi

printf 'miniapp ai validate safety tests: ok\n'
