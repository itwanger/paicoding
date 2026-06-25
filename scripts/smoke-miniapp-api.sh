#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
SMOKE_LOGIN_CODE="${SMOKE_LOGIN_CODE:-}"
RUN_MUTATION_SMOKE="${RUN_MUTATION_SMOKE:-auto}"
ALLOW_REMOTE_SMOKE="${ALLOW_REMOTE_SMOKE:-false}"
ALLOW_PRODUCTION_SMOKE="${ALLOW_PRODUCTION_SMOKE:-false}"
AVATAR_FILE="${AVATAR_FILE:-}"
local_base=false

case "${BASE_URL}" in
  http://127.0.0.1:*|http://localhost:*|http://0.0.0.0:*)
    local_base=true
    ;;
  *)
    if [ "${ALLOW_REMOTE_SMOKE}" != "true" ]; then
      echo "miniapp smoke: refusing non-local BASE_URL=${BASE_URL}; set ALLOW_REMOTE_SMOKE=true for preflight on a non-production remote environment" >&2
      exit 1
    fi
    case "${BASE_URL}" in
      https://paicoding.com|https://paicoding.com/*)
        if [ "${ALLOW_PRODUCTION_SMOKE}" != "true" ]; then
          echo "miniapp smoke: refusing production BASE_URL=${BASE_URL}; production smoke requires explicit ALLOW_PRODUCTION_SMOKE=true and maintainer approval" >&2
          exit 1
        fi
        ;;
    esac
    ;;
esac

if [ -z "${SMOKE_LOGIN_CODE}" ]; then
  if [ "${local_base}" = true ]; then
    SMOKE_LOGIN_CODE="mock-smoke-$(date +%s)"
  else
    echo "miniapp smoke: remote BASE_URL requires SMOKE_LOGIN_CODE from a real wx.login session" >&2
    exit 1
  fi
elif [ "${local_base}" != true ]; then
  smoke_login_code_lower="$(printf '%s' "${SMOKE_LOGIN_CODE}" | tr '[:upper:]' '[:lower:]')"
  case "${smoke_login_code_lower}" in
    *mock*|*test*|*fake*|*dummy*|*local*|*tourist*|dev*)
      echo "miniapp smoke: remote BASE_URL requires a real wx.login code; mock/test/fake codes are forbidden" >&2
      exit 1
      ;;
  esac
fi

json_get() {
  python3 -c 'import json,sys
data=json.load(sys.stdin)
for key in sys.argv[1].strip("/").split("/"):
    data=data[int(key)] if isinstance(data, list) else data[key]
print(data)' "$1"
}

json_get_optional() {
  python3 -c 'import json,sys
data=json.load(sys.stdin)
cur=data
for key in sys.argv[1].strip("/").split("/"):
    if isinstance(cur, list):
        idx=int(key)
        if idx >= len(cur):
            sys.exit(0)
        cur=cur[idx]
    elif isinstance(cur, dict) and key in cur:
        cur=cur[key]
    else:
        sys.exit(0)
if cur is not None:
    print(cur)' "$1"
}

login_payload() {
  python3 - "${SMOKE_LOGIN_CODE}" <<'PY'
import json
import sys

print(json.dumps({"code": sys.argv[1]}, ensure_ascii=False))
PY
}

comment_payload() {
  python3 - "$@" <<'PY'
import json
import sys

payload = {"commentContent": sys.argv[1]}
if len(sys.argv) > 3:
    payload["parentCommentId"] = int(sys.argv[2])
    payload["topCommentId"] = int(sys.argv[3])
print(json.dumps(payload, ensure_ascii=False))
PY
}

find_comment_id_by_content() {
  python3 - "$1" <<'PY'
import json
import sys

target = sys.argv[1]
data = json.load(sys.stdin)

def scan(items):
    for item in items or []:
        if item.get("commentContent") == target:
            return item.get("commentId")
        found = scan(item.get("childComments") or [])
        if found:
            return found
    return None

comment_id = scan(((data.get("result") or {}).get("list") or []))
if comment_id:
    print(comment_id)
PY
}

echo "miniapp smoke: ${BASE_URL}"

login_body="$(curl -fsS -H 'content-type: application/json' \
  -d "$(login_payload)" \
  "${BASE_URL}/mini/api/auth/login")"
token="$(printf '%s' "${login_body}" | json_get "/result/token")"
test -n "${token}"

curl -fsS "${BASE_URL}/mini/api/categories" >/dev/null
curl -fsS -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/user/me" >/dev/null

if [ -n "${AVATAR_FILE}" ]; then
  test -f "${AVATAR_FILE}"
  curl -fsS -X POST -H "Authorization: Bearer ${token}" \
    -F "image=@${AVATAR_FILE}" \
    "${BASE_URL}/mini/api/user/avatar" >/dev/null
fi

articles_body="$(curl -fsS "${BASE_URL}/mini/api/articles?categoryId=0&page=1&size=3")"
article_id="$(printf '%s' "${articles_body}" | json_get "/result/list/0/articleId")"
test -n "${article_id}"

curl -fsS "${BASE_URL}/mini/api/search/hint?key=Java" >/dev/null
curl -fsS "${BASE_URL}/mini/api/articles/${article_id}" >/dev/null

run_mutation_smoke=false
case "${RUN_MUTATION_SMOKE}" in
  true)
    run_mutation_smoke=true
    ;;
  auto)
    case "${BASE_URL}" in
      http://127.0.0.1:*|http://localhost:*|http://0.0.0.0:*)
        run_mutation_smoke=true
        ;;
    esac
    ;;
esac

if [ "${run_mutation_smoke}" = true ]; then
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/favor?type=2" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/favor?type=4" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/favor?type=3" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/favor?type=5" >/dev/null
  invalid_favor_body="$(curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/favor?type=1")"
  invalid_favor_code="$(printf '%s' "${invalid_favor_body}" | json_get "/status/code")"
  if [ "${invalid_favor_code}" = "0" ]; then
    echo "miniapp smoke: invalid favor type=1 unexpectedly succeeded" >&2
    exit 1
  fi

  curl -fsS "${BASE_URL}/mini/api/articles/${article_id}/comments?page=1&size=10" >/dev/null
  comment_content="miniapp smoke comment $(date '+%Y%m%d%H%M%S') $$"
  comment_body="$(curl -fsS -X POST -H "Authorization: Bearer ${token}" -H 'content-type: application/json' \
    -d "$(comment_payload "${comment_content}")" \
    "${BASE_URL}/mini/api/articles/${article_id}/comments")"
  comment_id="$(printf '%s' "${comment_body}" | json_get_optional "/result/submittedCommentId")"
  if [ -z "${comment_id}" ]; then
    comment_id="$(printf '%s' "${comment_body}" | find_comment_id_by_content "${comment_content}")"
  fi
  if [ -z "${comment_id}" ]; then
    echo "miniapp smoke: posted comment id was not returned" >&2
    exit 1
  fi

  reply_content="miniapp smoke reply $(date '+%Y%m%d%H%M%S') $$"
  reply_body="$(curl -fsS -X POST -H "Authorization: Bearer ${token}" -H 'content-type: application/json' \
    -d "$(comment_payload "${reply_content}" "${comment_id}" "${comment_id}")" \
    "${BASE_URL}/mini/api/articles/${article_id}/comments")"
  reply_id="$(printf '%s' "${reply_body}" | json_get_optional "/result/submittedCommentId")"
  if [ -z "${reply_id}" ]; then
    reply_id="$(printf '%s' "${reply_body}" | find_comment_id_by_content "${reply_content}")"
  fi
  if [ -z "${reply_id}" ]; then
    echo "miniapp smoke: posted reply id was not returned" >&2
    exit 1
  fi

  curl -fsS "${BASE_URL}/mini/api/articles/${article_id}/comments/${comment_id}/children?page=1&size=10" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/comments/${comment_id}/favor?type=2" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/comments/${comment_id}/favor?type=4" >/dev/null
  invalid_comment_favor_body="$(curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/comments/${comment_id}/favor?type=3")"
  invalid_comment_favor_code="$(printf '%s' "${invalid_comment_favor_body}" | json_get "/status/code")"
  if [ "${invalid_comment_favor_code}" = "0" ]; then
    echo "miniapp smoke: invalid comment favor type=3 unexpectedly succeeded" >&2
    exit 1
  fi
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/comments/${reply_id}/delete" >/dev/null
  curl -fsS -X POST -H "Authorization: Bearer ${token}" "${BASE_URL}/mini/api/articles/${article_id}/comments/${comment_id}/delete" >/dev/null
fi

echo "miniapp smoke: ok, article_id=${article_id}"
