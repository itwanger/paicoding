#!/usr/bin/env node
'use strict';

const assert = require('assert');
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const MAIN_CONFIG = path.join(ROOT, 'paicoding-miniapp/utils/config.js');
const AI_CONFIG = path.join(ROOT, 'paicoding-miniapp/skills/article-search/utils/request.js');
const PREFLIGHT = path.join(ROOT, 'scripts/preflight-miniapp.sh');

function extractApiBaseUrls(file) {
  const source = fs.readFileSync(file, 'utf8');
  const urls = {};
  for (const env of ['develop', 'trial', 'release']) {
    const pattern = new RegExp(`${env}:\\s*['"]([^'"]+)['"]`);
    const matched = source.match(pattern);
    assert(matched, `${path.relative(ROOT, file)} missing ${env} API_BASE_URL`);
    urls[env] = matched[1];
  }
  return urls;
}

const mainUrls = extractApiBaseUrls(MAIN_CONFIG);
const aiUrls = extractApiBaseUrls(AI_CONFIG);
assert.deepStrictEqual(aiUrls, mainUrls, 'main miniapp and AI Skill API_BASE_URL must match in all envs');

const preflight = fs.readFileSync(PREFLIGHT, 'utf8');
assert(
  preflight.includes(`BASE_URL="\${BASE_URL:-${mainUrls.develop}}"`),
  `preflight default BASE_URL must match develop API base URL ${mainUrls.develop}`
);

console.log('miniapp config consistency tests: ok');
