#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "${tmpdir}"' EXIT

run_readiness() {
  env \
    STRICT=false \
    EVIDENCE_DIR="${tmpdir}" \
    WECHAT_DEVTOOLS_CLI=/nonexistent/wechat-devtools-cli \
    bash scripts/check-miniapp-readiness.sh 2>&1 || true
}

touch "${tmpdir}/pre-api-smoke.empty.md"
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'pre-api-smoke'; then
  printf 'readiness evidence test: empty artifact should not satisfy pre-api-smoke\n%s\n' "${output}" >&2
  exit 1
fi

for name in \
  pre-api-smoke \
  device-login \
  device-avatar-profile \
  device-article-flow \
  device-interactions \
  wechat-domain-privacy \
  ai-preview \
  ai-execute-render
do
  printf 'redacted evidence for %s\n' "${name}" >"${tmpdir}/${name}.md"
done

output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'artifact pass markers missing'; then
  printf 'readiness evidence test: generic non-empty artifacts should not satisfy pass-marker check\n%s\n' "${output}" >&2
  exit 1
fi

cat >"${tmpdir}/pre-api-smoke.md" <<'EOF'
miniapp smoke: ok, article_id=1001
EOF
cat >"${tmpdir}/device-login.md" <<'EOF'
status: pass
EOF
cat >"${tmpdir}/device-avatar-profile.md" <<'EOF'
status: pass
EOF
cat >"${tmpdir}/device-article-flow.md" <<'EOF'
status: pass
EOF
cat >"${tmpdir}/device-interactions.md" <<'EOF'
status: pass
EOF
cat >"${tmpdir}/wechat-domain-privacy.md" <<'EOF'
status: pass
EOF
cat >"${tmpdir}/ai-preview.md" <<'EOF'
summary.buildStatus: pass
EOF
cat >"${tmpdir}/ai-execute-render.md" <<'EOF'
status: pass
execute: pass
render: pass
EOF

output="$(run_readiness)"
if printf '%s' "${output}" | rg -q 'artifact groups missing'; then
  printf 'readiness evidence test: complete non-empty artifacts should satisfy group check\n%s\n' "${output}" >&2
  exit 1
fi
if ! printf '%s' "${output}" | rg -q 'acceptance evidence artifact groups are present'; then
  printf 'readiness evidence test: expected artifact groups pass\n%s\n' "${output}" >&2
  exit 1
fi
if printf '%s' "${output}" | rg -q 'artifact pass markers missing'; then
  printf 'readiness evidence test: pass-marked artifacts should satisfy semantic check\n%s\n' "${output}" >&2
  exit 1
fi

printf 'token: should-not-be-here\n' >"${tmpdir}/device-login.md"
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'artifact text files may contain sensitive values'; then
  printf 'readiness evidence test: expected sensitive artifact failure\n%s\n' "${output}" >&2
  exit 1
fi

printf '{"openid":"should-not-be-here"}\n' >"${tmpdir}/device-login.md"
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'artifact text files may contain sensitive values'; then
  printf 'readiness evidence test: expected JSON sensitive artifact failure\n%s\n' "${output}" >&2
  exit 1
fi

printf 'status: pass\nAuthorization: Bearer eyJshouldNotBeHere\n' >"${tmpdir}/device-login.md"
output="$(run_readiness)"
if ! printf '%s' "${output}" | rg -q 'artifact text files may contain sensitive values'; then
  printf 'readiness evidence test: expected bearer token artifact failure\n%s\n' "${output}" >&2
  exit 1
fi

printf 'miniapp readiness evidence tests: ok\n'
