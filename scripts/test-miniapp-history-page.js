#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const HISTORY_MODULE = path.join(ROOT, 'paicoding-miniapp/pages/history/history.js');
const PROFILE_MODULE = path.join(ROOT, 'paicoding-miniapp/pages/profile/profile.js');
const AUTH_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/auth.js');
const REQUEST_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/request.js');
const CONFIG_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/config.js');
const PRIVACY_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/privacy.js');

function ok(result) {
  return {
    statusCode: 200,
    data: {
      status: { code: 0 },
      result
    }
  };
}

function nextTick(fn) {
  setTimeout(fn, 0);
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function applyPath(target, pathKey, value) {
  const parts = pathKey.split('.');
  let current = target;
  for (let i = 0; i < parts.length - 1; i += 1) {
    if (!current[parts[i]]) {
      current[parts[i]] = {};
    }
    current = current[parts[i]];
  }
  current[parts[parts.length - 1]] = value;
}

function clearModules(modulePath) {
  [modulePath, AUTH_MODULE, REQUEST_MODULE, CONFIG_MODULE, PRIVACY_MODULE].forEach((mod) => {
    delete require.cache[mod];
  });
}

function createHarness(modulePath) {
  const storage = {};
  const requestQueue = [];
  const requestCalls = [];
  const navigateCalls = [];
  const stopPullDownCalls = [];
  let loginCalls = 0;
  let pageDefinition = null;

  global.Page = (definition) => {
    pageDefinition = definition;
  };

  global.wx = {
    getAccountInfoSync() {
      return { miniProgram: { envVersion: 'develop' } };
    },
    getStorageSync(key) {
      return storage[key];
    },
    setStorageSync(key, value) {
      storage[key] = value;
    },
    removeStorageSync(key) {
      delete storage[key];
    },
    login(options) {
      loginCalls += 1;
      nextTick(() => options.success({ code: `wx-code-${loginCalls}` }));
    },
    request(options) {
      requestCalls.push(options);
      const handler = requestQueue.shift();
      if (!handler) {
        nextTick(() => options.fail({ errMsg: `unexpected request ${options.url}` }));
        return;
      }
      nextTick(() => options.success(typeof handler === 'function' ? handler(options) : handler));
    },
    navigateTo(options) {
      navigateCalls.push(options);
    },
    stopPullDownRefresh() {
      stopPullDownCalls.push(true);
    },
    getPrivacySetting(options) {
      nextTick(() => options.success({ needAuthorization: false }));
    }
  };

  clearModules(modulePath);
  require(modulePath);

  const page = {
    data: clone(pageDefinition.data),
    setData(patch) {
      Object.keys(patch).forEach((key) => {
        applyPath(this.data, key, patch[key]);
      });
    }
  };

  Object.keys(pageDefinition).forEach((key) => {
    if (key !== 'data') {
      page[key] = pageDefinition[key].bind(page);
    }
  });

  return {
    page,
    storage,
    requestQueue,
    requestCalls,
    navigateCalls,
    stopPullDownCalls,
    get loginCalls() {
      return loginCalls;
    }
  };
}

async function testHistoryLoadPaginationAndNavigation() {
  const h = createHarness(HISTORY_MODULE);
  h.requestQueue.push(
    ok({ token: 'history-token', user: { userId: 51, nickName: 'reader' } }),
    ok({ list: [{ articleId: 9101, title: '读过一' }], hasMore: true }),
    ok({ userId: 51, nickName: 'reader' }),
    ok({ list: [{ articleId: 9102, title: '读过二' }], hasMore: false })
  );

  await h.page.onLoad();
  await h.page.loadMore();

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/reads'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { page: 1, size: 10 });
  assert.strictEqual(h.requestCalls[2].url.endsWith('/mini/api/user/me'), true);
  assert.deepStrictEqual(h.requestCalls[3].data, { page: 2, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [9101, 9102]);
  assert.strictEqual(h.page.data.hasMore, false);

  h.page.openDetail({ currentTarget: { dataset: { id: 9102 } } });
  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/detail/detail?id=9102' }]);
}

async function testProfileOpensHistoryPage() {
  const h = createHarness(PROFILE_MODULE);

  h.page.openHistory();

  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/history/history' }]);
}

async function testHistoryPullDownRefreshReloadsFirstPage() {
  const h = createHarness(HISTORY_MODULE);
  h.storage.PAICODING_TOKEN = 'history-token';
  h.page.setData({
    articles: [{ articleId: 9101, title: '旧历史' }],
    page: 3,
    hasMore: false
  });
  h.requestQueue.push(
    ok({ userId: 51, nickName: 'reader' }),
    ok({ list: [{ articleId: 9103, title: '刷新历史' }], hasMore: true })
  );

  await h.page.onPullDownRefresh();

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/user/me'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/reads'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [9103]);
  assert.strictEqual(h.page.data.page, 2);
  assert.strictEqual(h.stopPullDownCalls.length, 1);
}

(async () => {
  await testHistoryLoadPaginationAndNavigation();
  await testHistoryPullDownRefreshReloadsFirstPage();
  await testProfileOpensHistoryPage();
  console.log('miniapp history page tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
