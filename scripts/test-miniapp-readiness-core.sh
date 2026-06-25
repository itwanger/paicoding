#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "${tmpdir}"' EXIT

project_config="${tmpdir}/project.config.json"
evidence_file="${tmpdir}/ACCEPTANCE_EVIDENCE.md"
evidence_dir="${tmpdir}/evidence"
miniapp_dir="${tmpdir}/paicoding-miniapp"
mkdir -p "${evidence_dir}"
cp -R paicoding-miniapp "${miniapp_dir}"
cp paicoding-miniapp/project.config.json "${project_config}"

python3 - "${project_config}" wx1234567890abcdef <<'PY'
import json
import sys
from pathlib import Path

path = Path(sys.argv[1])
appid = sys.argv[2]
data = json.loads(path.read_text())
data["appid"] = appid
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
PY

write_complete_evidence() {
  cat >"${evidence_file}" <<'EOF'
# 微信小程序上线前验收证据

- 小程序 AppID 后 6 位：abcdef
- 真机设备与微信版本：iPhone 15 / WeChat 8.x

| 场景 | 期望结果 | 证据 |
| --- | --- | --- |
| 首次打开 | 自动登录成功 | 通过 |

## 发布结论

- 是否允许提审：是
- 未解决问题：无
- 回滚确认人：维护者
EOF
}

write_complete_artifacts() {
  cat >"${evidence_dir}/pre-api-smoke.md" <<'EOF'
miniapp smoke: ok, article_id=1001
EOF
  for name in device-login device-avatar-profile device-article-flow device-interactions wechat-domain-privacy
  do
    printf 'status: pass\n' >"${evidence_dir}/${name}.md"
  done
  cat >"${evidence_dir}/ai-preview.md" <<'EOF'
summary.buildStatus: pass
EOF
  cat >"${evidence_dir}/ai-execute-render.md" <<'EOF'
status: pass
execute: pass
render: pass
EOF
}

run_readiness() {
  env \
    STRICT=false \
    MINIAPP_DIR="${miniapp_dir}" \
    PROJECT_CONFIG="${project_config}" \
    EVIDENCE_FILE="${evidence_file}" \
    EVIDENCE_DIR="${evidence_dir}" \
    WECHAT_DEVTOOLS_CLI=/nonexistent/wechat-devtools-cli \
    PAICODING_WX_MINI_APP_ID="${1:-wx1234567890abcdef}" \
    PAICODING_WX_MINI_APP_SECRET=unit-secret-present \
    bash scripts/check-miniapp-readiness.sh 2>&1 || true
}

write_complete_evidence
write_complete_artifacts

skill_request="${miniapp_dir}/skills/article-search/utils/request.js"
skill_request_backup="${tmpdir}/skill-request.js.bak"
cp "${skill_request}" "${skill_request_backup}"
privacy_file="${miniapp_dir}/utils/privacy.js"
privacy_file_backup="${tmpdir}/privacy.js.bak"
cp "${privacy_file}" "${privacy_file_backup}"

python3 - "${skill_request}" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
text = path.read_text()
text = text.replace("release: 'https://paicoding.com'", "release: 'http://paicoding.com'")
path.write_text(text)
PY
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'AI Skill trial/release API base URLs must use HTTPS'; then
  printf 'readiness core test: expected AI Skill HTTPS failure\n%s\n' "${output}" >&2
  exit 1
fi
cp "${skill_request_backup}" "${skill_request}"

python3 - "${skill_request}" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
text = path.read_text()
text = text.replace("release: 'https://paicoding.com'", "release: 'https://api.paicoding.com'")
path.write_text(text)
PY
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'main miniapp and AI Skill API base URLs must match'; then
  printf 'readiness core test: expected main/AI Skill API URL mismatch failure\n%s\n' "${output}" >&2
  exit 1
fi
cp "${skill_request_backup}" "${skill_request}"

python3 - "${privacy_file}" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
text = path.read_text()
text = text.replace("wx.onNeedPrivacyAuthorization", "wx.onNeedPrivacyAuthzDisabled")
path.write_text(text)
PY
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'miniapp must wire wx.onNeedPrivacyAuthorization'; then
  printf 'readiness core test: expected privacy authorization wiring failure\n%s\n' "${output}" >&2
  exit 1
fi
cp "${privacy_file_backup}" "${privacy_file}"

output="$(run_readiness wxabcdef1234567890)"
if ! printf '%s' "${output}" | rg -q 'frontend project.config.json AppID and PAICODING_WX_MINI_APP_ID differ'; then
  printf 'readiness core test: expected AppID mismatch failure\n%s\n' "${output}" >&2
  exit 1
fi

cat >"${evidence_file}" <<'EOF'
# 微信小程序上线前验收证据

- 小程序 AppID 后 6 位：
- 真机设备与微信版本：

## 发布结论

- 是否允许提审：
- 未解决问题：
- 回滚确认人：
EOF

output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'acceptance evidence still has blank required fields'; then
  printf 'readiness core test: expected blank required field warning\n%s\n' "${output}" >&2
  exit 1
fi

cat >"${evidence_file}" <<'EOF'
# 微信小程序上线前验收证据

- 小程序 AppID 后 6 位：abcdef
- 真机设备与微信版本：iPhone 15 / WeChat 8.x

## 发布结论

- 是否允许提审：否
- 未解决问题：等待人工上线确认
- 回滚确认人：维护者
EOF

output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'acceptance evidence must set 是否允许提审'; then
  printf 'readiness core test: expected explicit submit approval warning\n%s\n' "${output}" >&2
  exit 1
fi
if ! printf '%s' "${output}" | rg -q 'acceptance evidence must set 未解决问题'; then
  printf 'readiness core test: expected unresolved-issue warning\n%s\n' "${output}" >&2
  exit 1
fi

write_complete_evidence
printf '{"token":"should-not-be-here"}\n' >>"${evidence_file}"
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'acceptance evidence may contain sensitive values'; then
  printf 'readiness core test: expected JSON token sensitive-value failure\n%s\n' "${output}" >&2
  exit 1
fi

printf 'miniapp readiness core tests: ok\n'
