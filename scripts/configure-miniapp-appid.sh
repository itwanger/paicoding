#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_CONFIG="${PROJECT_CONFIG:-${ROOT_DIR}/paicoding-miniapp/project.config.json}"
APPID="${1:-${WECHAT_MINI_APP_ID:-}}"
ALLOW_TOURIST=false

usage() {
  cat <<'EOF'
Usage:
  scripts/configure-miniapp-appid.sh wx1234567890abcdef
  WECHAT_MINI_APP_ID=wx1234567890abcdef scripts/configure-miniapp-appid.sh
  scripts/configure-miniapp-appid.sh --tourist

Environment:
  PROJECT_CONFIG  Optional path to project.config.json, useful for tests.
EOF
}

mask_appid() {
  local value="$1"
  if [ "${value}" = "touristappid" ]; then
    printf 'touristappid'
    return
  fi
  printf '%s...%s' "${value:0:4}" "${value: -4}"
}

if [ "${APPID}" = "-h" ] || [ "${APPID}" = "--help" ]; then
  usage
  exit 0
fi

if [ "${APPID}" = "--tourist" ]; then
  APPID="touristappid"
  ALLOW_TOURIST=true
fi

if [ -z "${APPID}" ]; then
  usage >&2
  exit 1
fi

if [ "${APPID}" = "touristappid" ] && [ "${ALLOW_TOURIST}" != "true" ]; then
  printf '[miniapp-config] touristappid is only allowed via --tourist\n' >&2
  exit 1
fi

if [ "${APPID}" != "touristappid" ] && ! [[ "${APPID}" =~ ^wx[0-9A-Za-z]{16,32}$ ]]; then
  printf '[miniapp-config] invalid AppID format: %s\n' "$(mask_appid "${APPID}")" >&2
  exit 1
fi

if [ ! -f "${PROJECT_CONFIG}" ]; then
  printf '[miniapp-config] missing project config: %s\n' "${PROJECT_CONFIG}" >&2
  exit 1
fi

python3 - "${PROJECT_CONFIG}" "${APPID}" <<'PY'
import json
import sys
from pathlib import Path

path = Path(sys.argv[1])
appid = sys.argv[2]
data = json.loads(path.read_text())
data["appid"] = appid
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
PY

printf '[miniapp-config] project.config.json appid=%s\n' "$(mask_appid "${APPID}")"
