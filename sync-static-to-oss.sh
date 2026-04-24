#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_DIR="${ROOT_DIR}/paicoding-ui/src/main/resources/static"
TARGET_DIR="paicodingui$(date +%m%d)"
DELETE_OLD_DIRS=true
DRY_RUN=false
OSSUTIL_BIN=""

usage() {
  cat <<'EOF'
Usage: ./sync-static-to-oss.sh [options]

Options:
  --target-dir <dir>     Upload into oss://bucket/<dir>/static/. Default: paicodinguiMMDD
  --source-dir <dir>     Local static dir. Default: paicoding-ui/src/main/resources/static
  --delete-old-dirs      Delete previous paicodingui* dirs after upload. This is the default.
  --keep-old-dirs        Keep previous paicodingui* dirs instead of deleting them
  --dry-run              Print commands without executing upload/delete
  -h, --help             Show this help

Required env in .env or .env.local:
  PAICODING_OSS_AK
  PAICODING_OSS_SK
  PAICODING_OSS_ENDPOINT
  PAICODING_OSS_BUCKET
  PAICODING_OSS_REGION (optional if endpoint like oss-cn-beijing.aliyuncs.com can be auto-derived)
EOF
}

load_env_file() {
  local env_file="$1"
  if [[ -f "$env_file" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
  fi
}

require_env() {
  local key="$1"
  local value="${!key:-}"
  if [[ -z "$value" ]]; then
    echo "Missing required env: $key" >&2
    exit 1
  fi
}

derive_region_from_endpoint() {
  local endpoint="$1"
  endpoint="${endpoint#http://}"
  endpoint="${endpoint#https://}"
  endpoint="${endpoint%%/*}"
  if [[ "$endpoint" =~ ^oss-([^.]+)\. ]]; then
    printf '%s\n' "${BASH_REMATCH[1]}"
  fi
}

run_cmd() {
  if [[ "$DRY_RUN" == "true" ]]; then
    local masked=()
    local redact_next=false
    local arg=""
    for arg in "$@"; do
      if [[ "$redact_next" == "true" ]]; then
        masked+=("******")
        redact_next=false
        continue
      fi
      case "$arg" in
        --access-key-id|--access-key-secret|-i|-k)
          masked+=("$arg")
          redact_next=true
          ;;
        *)
          masked+=("$arg")
          ;;
      esac
    done
    printf '[dry-run] '
    printf '%q ' "${masked[@]}"
    printf '\n'
    return 0
  fi
  "$@"
}

while (($#)); do
  case "$1" in
    --target-dir)
      shift
      TARGET_DIR="${1:?missing value for --target-dir}"
      ;;
    --target-dir=*)
      TARGET_DIR="${1#*=}"
      ;;
    --source-dir)
      shift
      SOURCE_DIR="${1:?missing value for --source-dir}"
      ;;
    --source-dir=*)
      SOURCE_DIR="${1#*=}"
      ;;
    --keep-old-dirs)
      DELETE_OLD_DIRS=false
      ;;
    --delete-old-dirs)
      DELETE_OLD_DIRS=true
      ;;
    --dry-run)
      DRY_RUN=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift || true
done

load_env_file "${ROOT_DIR}/.env"
load_env_file "${ROOT_DIR}/.env.local"

command -v aliyun >/dev/null 2>&1 || {
  true
}

if [[ -x "${HOME}/.aliyun/ossutil" ]]; then
  OSSUTIL_BIN="${HOME}/.aliyun/ossutil"
elif command -v ossutil >/dev/null 2>&1; then
  OSSUTIL_BIN="$(command -v ossutil)"
fi

if [[ -z "$OSSUTIL_BIN" ]]; then
  echo "ossutil binary not found. Run 'aliyun ossutil' once or install ossutil first." >&2
  exit 1
fi

require_env PAICODING_OSS_AK
require_env PAICODING_OSS_SK
require_env PAICODING_OSS_ENDPOINT
require_env PAICODING_OSS_BUCKET

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "static source dir not found: $SOURCE_DIR" >&2
  exit 1
fi

TARGET_DIR="${TARGET_DIR#/}"
TARGET_DIR="${TARGET_DIR%/}"
if [[ -z "$TARGET_DIR" || "$TARGET_DIR" == *"/"* ]]; then
  echo "--target-dir must be a single top-level dir like paicodingui0423" >&2
  exit 1
fi

OSS_REGION="${PAICODING_OSS_REGION:-$(derive_region_from_endpoint "${PAICODING_OSS_ENDPOINT}")}"
if [[ -z "$OSS_REGION" ]]; then
  echo "Missing PAICODING_OSS_REGION and failed to derive region from PAICODING_OSS_ENDPOINT=${PAICODING_OSS_ENDPOINT}" >&2
  exit 1
fi

OSS_GLOBAL_FLAGS=(
  --mode AK
  --access-key-id "${PAICODING_OSS_AK}"
  --access-key-secret "${PAICODING_OSS_SK}"
  --endpoint "${PAICODING_OSS_ENDPOINT}"
  --region "${OSS_REGION}"
)

DEST="oss://${PAICODING_OSS_BUCKET}/${TARGET_DIR}/static/"

echo "Uploading ${SOURCE_DIR} -> ${DEST}"
run_cmd "${OSSUTIL_BIN}" cp "${SOURCE_DIR%/}/" "${DEST}" -r -f "${OSS_GLOBAL_FLAGS[@]}"

if [[ "$DELETE_OLD_DIRS" == "true" ]]; then
  echo "Listing old paicodingui* dirs in oss://${PAICODING_OSS_BUCKET}/"
  OLD_DIRS="$("${OSSUTIL_BIN}" api list-objects-v2 \
    --bucket "${PAICODING_OSS_BUCKET}" \
    --prefix "paicodingui" \
    --delimiter "/" \
    --output-query "CommonPrefixes" \
    --output-format json \
    "${OSS_GLOBAL_FLAGS[@]}" \
    | grep -oE '"paicodingui[^"/]+/"' \
    | tr -d '"' \
    | sed 's:/$::' \
    | sort -u \
    | grep -vxF "${TARGET_DIR}" || true)"

  if [[ -n "$OLD_DIRS" ]]; then
    while IFS= read -r old_dir; do
      [[ -z "$old_dir" ]] && continue
      echo "Deleting old dir oss://${PAICODING_OSS_BUCKET}/${old_dir}/"
      run_cmd "${OSSUTIL_BIN}" rm "oss://${PAICODING_OSS_BUCKET}/${old_dir}/" -r -f "${OSS_GLOBAL_FLAGS[@]}"
    done <<< "$OLD_DIRS"
  else
    echo "No old paicodingui* dirs to delete."
  fi
fi

echo "Done."
if [[ -n "${PAICODING_OSS_HOST:-}" ]]; then
  CLEAN_HOST="${PAICODING_OSS_HOST%/}"
  echo "${CLEAN_HOST}/${TARGET_DIR}/static"
fi
