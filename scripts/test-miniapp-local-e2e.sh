#!/usr/bin/env bash
set -euo pipefail

SCRIPT="scripts/run-miniapp-local-e2e.sh"

bash -n "${SCRIPT}"

if ! rg -q '/usr/libexec/java_home -v 1\.8' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must force Java 8\n' >&2
  exit 1
fi

if ! rg -q -- '-Pdev -pl paicoding-web -am -DskipTests' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must package paicoding-web with dev profile and skip tests\n' >&2
  exit 1
fi

if ! rg -q 'dependency:build-classpath' "${SCRIPT}" \
  || ! rg -q 'com.github.paicoding.forum.web.QuickForumApplication' "${SCRIPT}" \
  || ! rg -q 'MODULE_CLASSPATH=' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must launch QuickForumApplication with module runtime classpath\n' >&2
  exit 1
fi

if ! rg -q 'paicoding-\(ui\|api\|core\|service\|web\)' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must filter local paicoding module jars from dependency classpath\n' >&2
  exit 1
fi

if ! rg -q 'printf '\''%s'\'' "\$\{PORT\}" >"\$\{DEV_PORT_FILE\}"' "${SCRIPT}" \
  || ! rg -q 'OLD_DEV_PORT_VALUE' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must pin and restore .dev-port\n' >&2
  exit 1
fi

if rg -q 'mvn clean|clean[[:space:]]' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must not run clean\n' >&2
  exit 1
fi

if ! rg -q 'scripts/smoke-miniapp-api\.sh' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must run miniapp API smoke\n' >&2
  exit 1
fi

if ! rg -q 'kill "\$\{APP_PID\}"' "${SCRIPT}"; then
  printf 'miniapp local e2e test: script must clean up the backend it started\n' >&2
  exit 1
fi

printf 'miniapp local e2e tests: ok\n'
