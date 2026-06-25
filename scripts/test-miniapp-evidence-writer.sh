#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "${tmpdir}"' EXIT

output="$(python3 scripts/write-miniapp-evidence.py \
  --group device-login \
  --status pass \
  --env pre \
  --app-id-suffix abc123 \
  --device 'iPhone 15 / WeChat 8.x' \
  --result '首次打开后真实微信登录成功，首页和个人页均能读取用户态。' \
  --output-dir "${tmpdir}")"
test -s "${output}"
rg -q 'status: pass' "${output}"
rg -q 'appIdSuffix: abc123' "${output}"

if python3 scripts/write-miniapp-evidence.py \
  --group device-login \
  --status pass \
  --env pre \
  --app-id-suffix abc123 \
  --device 'iPhone 15 / WeChat 8.x' \
  --result '未验证' \
  --output-dir "${tmpdir}" >/dev/null 2>&1; then
  printf 'miniapp evidence writer test: pass evidence must reject placeholders\n' >&2
  exit 1
fi

if python3 scripts/write-miniapp-evidence.py \
  --group device-login \
  --status pass \
  --env pre \
  --app-id-suffix abc123 \
  --device 'iPhone 15 / WeChat 8.x' \
  --result 'token=abc123456789 openid=oabcdef' \
  --output-dir "${tmpdir}" >/dev/null 2>&1; then
  printf 'miniapp evidence writer test: evidence must reject sensitive values\n' >&2
  exit 1
fi

if python3 scripts/write-miniapp-evidence.py \
  --group wechat-domain-privacy \
  --status pass \
  --env pre \
  --app-id-suffix abc123 \
  --result '微信后台域名与隐私保护指引均已配置并截图留档。' \
  --detail requestDomain=https://paicoding.com \
  --detail uploadDomain=https://paicoding.com \
  --output-dir "${tmpdir}" >/dev/null 2>&1; then
  printf 'miniapp evidence writer test: wechat evidence must require all domain/privacy details\n' >&2
  exit 1
fi

output="$(python3 scripts/write-miniapp-evidence.py \
  --group wechat-domain-privacy \
  --status pass \
  --env pre \
  --app-id-suffix abc123 \
  --result '微信后台 request、uploadFile、downloadFile 合法域名和隐私保护指引均已配置。' \
  --detail requestDomain=https://paicoding.com \
  --detail uploadDomain=https://paicoding.com \
  --detail downloadDomain=https://cdn.paicoding.com \
  --detail privacy=avatar-nickname \
  --output-dir "${tmpdir}")"
test -s "${output}"
rg -q 'privacy: avatar-nickname' "${output}"

printf 'miniapp evidence writer tests: ok\n'
