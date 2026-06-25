#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE_CONFIG="${ROOT_DIR}/paicoding-miniapp/project.config.json"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

CONFIG="${TMP_DIR}/project.config.json"
cp "${SOURCE_CONFIG}" "${CONFIG}"

PROJECT_CONFIG="${CONFIG}" "${ROOT_DIR}/scripts/configure-miniapp-appid.sh" wx1234567890abcdef >/dev/null
python3 - "${CONFIG}" <<'PY'
import json
import sys
with open(sys.argv[1]) as f:
    assert json.load(f)["appid"] == "wx1234567890abcdef"
PY

PROJECT_CONFIG="${CONFIG}" "${ROOT_DIR}/scripts/configure-miniapp-appid.sh" --tourist >/dev/null
python3 - "${CONFIG}" <<'PY'
import json
import sys
with open(sys.argv[1]) as f:
    assert json.load(f)["appid"] == "touristappid"
PY

if PROJECT_CONFIG="${CONFIG}" "${ROOT_DIR}/scripts/configure-miniapp-appid.sh" not-a-real-appid >/dev/null 2>&1; then
  printf 'invalid appid was accepted\n' >&2
  exit 1
fi

if PROJECT_CONFIG="${CONFIG}" WECHAT_MINI_APP_ID=touristappid "${ROOT_DIR}/scripts/configure-miniapp-appid.sh" >/dev/null 2>&1; then
  printf 'touristappid env was accepted without --tourist\n' >&2
  exit 1
fi

printf 'miniapp configure appid tests: ok\n'
