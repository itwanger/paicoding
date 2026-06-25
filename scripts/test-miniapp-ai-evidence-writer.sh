#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "${tmpdir}"' EXIT

cat >"${tmpdir}/validate-pass.json" <<'JSON'
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
      "success": true
    }
  },
  "render": {
    "article-card": {
      "status": "ok"
    }
  }
}
JSON

python3 scripts/write-miniapp-ai-evidence.py "${tmpdir}/validate-pass.json" "${tmpdir}/evidence-pass"
test -s "${tmpdir}/evidence-pass/ai-preview.validate.md"
test -s "${tmpdir}/evidence-pass/ai-execute-render.validate.md"
rg -q 'summary\.buildStatus: pass' "${tmpdir}/evidence-pass/ai-preview.validate.md"
rg -q 'status: pass' "${tmpdir}/evidence-pass/ai-execute-render.validate.md"

cat >"${tmpdir}/validate-preview-only.json" <<'JSON'
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

python3 scripts/write-miniapp-ai-evidence.py "${tmpdir}/validate-preview-only.json" "${tmpdir}/evidence-preview-only"
test -s "${tmpdir}/evidence-preview-only/ai-preview.validate.md"
if [ -e "${tmpdir}/evidence-preview-only/ai-execute-render.validate.md" ]; then
  printf 'miniapp ai evidence writer test: must not forge execute/render evidence\n' >&2
  exit 1
fi

cat >"${tmpdir}/validate-render-fail.json" <<'JSON'
{
  "summary": {
    "errors": 0,
    "warnings": 0,
    "buildStatus": "pass"
  },
  "execute": {
    "searchArticles": {
      "status": "pass"
    }
  },
  "render": {
    "article-card": {
      "status": "failed"
    }
  }
}
JSON

python3 scripts/write-miniapp-ai-evidence.py "${tmpdir}/validate-render-fail.json" "${tmpdir}/evidence-render-fail"
test -s "${tmpdir}/evidence-render-fail/ai-preview.validate.md"
if [ -e "${tmpdir}/evidence-render-fail/ai-execute-render.validate.md" ]; then
  printf 'miniapp ai evidence writer test: failed render must not create pass evidence\n' >&2
  exit 1
fi

printf 'miniapp ai evidence writer tests: ok\n'
