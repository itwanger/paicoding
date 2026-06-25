#!/usr/bin/env node
'use strict';

const assert = require('assert');
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SKILL_DIR = path.join(ROOT, 'paicoding-miniapp/skills/article-search');
const MCP_FILE = path.join(SKILL_DIR, 'mcp.json');
const INDEX_FILE = path.join(SKILL_DIR, 'index.js');
const REQUEST_FILE = path.join(SKILL_DIR, 'utils/request.js');
const COMPONENT_FILE = path.join(SKILL_DIR, 'components/article-card/index.js');
const DETAIL_PAGE_FILE = path.join(ROOT, 'paicoding-miniapp/pages/detail/detail.js');
const APP_FILE = path.join(ROOT, 'paicoding-miniapp/app.js');

const EXPECTED_APIS = new Map([
  ['searchArticles', 'apis/searchArticles.js'],
  ['getArticleDetail', 'apis/getArticleDetail.js']
]);
const EXPECTED_COMPONENTS = new Map([
  ['article-card', 'components/article-card/index']
]);
const FORBIDDEN_ENDPOINT_PARTS = [
  '/mini/api/auth',
  '/mini/api/user',
  '/mini/api/upload',
  '/mini/api/comment',
  '/mini/api/admin',
  '/mini/api/articles/${args.articleId}/favor',
  '/favor'
];
const FORBIDDEN_WX_APIS = [
  'wx.login',
  'wx.uploadFile',
  'wx.chooseAvatar',
  'wx.getUserProfile',
  'wx.getUserInfo',
  'wx.requestPayment'
];

function read(file) {
  return fs.readFileSync(file, 'utf8');
}

function listFiles(dir, predicate) {
  const result = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      result.push(...listFiles(full, predicate));
    } else if (!predicate || predicate(full)) {
      result.push(full);
    }
  }
  return result;
}

function rel(file) {
  return path.relative(SKILL_DIR, file).replace(/\\/g, '/');
}

function assertMcpBoundary() {
  const mcp = JSON.parse(read(MCP_FILE));
  assert.strictEqual(mcp.name, 'article-search');
  assert.strictEqual(Array.isArray(mcp.apis), true);
  assert.strictEqual(mcp.apis.length, EXPECTED_APIS.size);

  for (const api of mcp.apis) {
    assert.strictEqual(EXPECTED_APIS.has(api.name), true, `unexpected API in mcp.json: ${api.name}`);
    assert.strictEqual(api.path, EXPECTED_APIS.get(api.name), `unexpected path for API ${api.name}`);
    assert.strictEqual(path.normalize(api.path).startsWith('..'), false, `API path escapes skill dir: ${api.path}`);
    assert.strictEqual(fs.existsSync(path.join(SKILL_DIR, api.path)), true, `missing API file: ${api.path}`);
  }

  assert.strictEqual(Array.isArray(mcp.components), true);
  assert.strictEqual(mcp.components.length, EXPECTED_COMPONENTS.size);
  for (const component of mcp.components) {
    assert.strictEqual(
      EXPECTED_COMPONENTS.get(component.name),
      component.path,
      `unexpected component binding for ${component.name}`
    );
    assert.strictEqual(path.normalize(component.path).startsWith('..'), false, `component path escapes skill dir: ${component.path}`);
  }
}

function assertRegistrationBoundary() {
  const index = read(INDEX_FILE);
  const registrations = Array.from(index.matchAll(/registerAPI\(\s*['"`]([^'"`]+)['"`]/g)).map((match) => match[1]);
  assert.deepStrictEqual(registrations.sort(), Array.from(EXPECTED_APIS.keys()).sort());
}

function assertRequestBoundary() {
  const request = read(REQUEST_FILE);
  assert.match(request, /method:\s*['"`]GET['"`]/);
  assert.doesNotMatch(request, /options\.method/, 'AI Skill request utility must not allow write-method override');

  const apiFiles = Array.from(EXPECTED_APIS.values()).map((apiPath) => path.join(SKILL_DIR, apiPath));
  const allowedUrls = new Set(['/mini/api/search', '/mini/api/articles/${articleId}']);

  for (const file of apiFiles) {
    const source = read(file);
    assert.doesNotMatch(source, /\bmethod\s*:/, `${rel(file)} must use default GET only`);

    for (const forbidden of FORBIDDEN_ENDPOINT_PARTS) {
      assert.strictEqual(source.includes(forbidden), false, `${rel(file)} contains forbidden endpoint ${forbidden}`);
    }

    const urls = Array.from(source.matchAll(/\burl\s*:\s*(['"`])([\s\S]*?)\1/g)).map((match) => match[2]);
    assert.strictEqual(urls.length > 0, true, `${rel(file)} should declare an explicit request url`);
    for (const url of urls) {
      assert.strictEqual(allowedUrls.has(url), true, `${rel(file)} uses unexpected url: ${url}`);
    }
  }
}

function assertNoAuthOrMutationApis() {
  for (const file of listFiles(SKILL_DIR, (name) => name.endsWith('.js'))) {
    const source = read(file);
    for (const api of FORBIDDEN_WX_APIS) {
      assert.strictEqual(source.includes(api), false, `${rel(file)} uses forbidden WeChat API ${api}`);
    }
    assert.doesNotMatch(source, /\bmethod\s*:\s*['"`](POST|PUT|DELETE|PATCH)['"`]/i, `${rel(file)} contains write HTTP method`);
  }
}

function assertAiEntryDoesNotAutoLogin() {
  const component = read(COMPONENT_FILE);
  assert.match(component, /from=ai-skill/, 'AI card detail URL must mark the entry source');

  const detail = read(DETAIL_PAGE_FILE);
  assert.match(detail, /fromAiSkill\s*=\s*options\.from\s*===\s*['"`]ai-skill['"`]/);
  assert.match(detail, /if\s*\(\s*!fromAiSkill\s*\)\s*{[^}]*await auth\.ensureLogin\(\);/s);

  const app = read(APP_FILE);
  assert.doesNotMatch(app, /auth\.ensureLogin\(\)/, 'app launch must not auto-login before AI detail entry handles from=ai-skill');
}

function assertAiComponentHasEmptyState() {
  const component = read(COMPONENT_FILE);
  const wxml = read(path.join(SKILL_DIR, 'components/article-card/index.wxml'));
  assert.match(component, /emptyText:\s*['"`]暂无文章['"`]/);
  assert.match(component, /暂无匹配文章/);
  assert.match(wxml, /wx:elif="\{\{article\.articleId\}\}"/);
  assert.match(wxml, /class="article-empty"/);
}

assertMcpBoundary();
assertRegistrationBoundary();
assertRequestBoundary();
assertNoAuthOrMutationApis();
assertAiEntryDoesNotAutoLogin();
assertAiComponentHasEmptyState();

console.log('miniapp ai boundary tests: ok');
