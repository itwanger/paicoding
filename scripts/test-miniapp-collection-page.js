#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const COLLECTION_MODULE = path.join(ROOT, 'paicoding-miniapp/pages/collection/collection.js');
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
  const toastCalls = [];
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
    showToast(options) {
      toastCalls.push(options);
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
    toastCalls,
    stopPullDownCalls,
    get loginCalls() {
      return loginCalls;
    }
  };
}

async function testCollectionLoadPaginationAndNavigation() {
  const h = createHarness(COLLECTION_MODULE);
  h.requestQueue.push(
    ok({ token: 'collection-token', user: { userId: 41, nickName: 'collector' } }),
    ok({ list: [{ articleId: 9001, title: '收藏一' }], hasMore: true }),
    ok({ userId: 41, nickName: 'collector' }),
    ok({ list: [{ articleId: 9002, title: '收藏二' }], hasMore: false })
  );

  await h.page.onLoad();
  await h.page.loadMore();

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/collections'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { page: 1, size: 10 });
  assert.strictEqual(h.requestCalls[2].url.endsWith('/mini/api/user/me'), true);
  assert.deepStrictEqual(h.requestCalls[3].data, { page: 2, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [9001, 9002]);
  assert.strictEqual(h.page.data.hasMore, false);

  h.page.openDetail({ currentTarget: { dataset: { id: 9002 } } });
  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/detail/detail?id=9002' }]);
}

async function testCollectionRemove() {
  const h = createHarness(COLLECTION_MODULE);
  h.storage.PAICODING_TOKEN = 'collection-token';
  h.page.setData({
    articles: [
      { articleId: 9001, title: '收藏一' },
      { articleId: 9002, title: '收藏二' }
    ]
  });
  h.requestQueue.push(
    ok({ userId: 41, nickName: 'collector' }),
    ok({ done: true })
  );

  await h.page.removeCollection({ currentTarget: { dataset: { id: 9001 } } });

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/user/me'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/articles/9001/favor?type=5'), true);
  assert.strictEqual(h.requestCalls[1].method, 'POST');
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [9002]);
  assert.strictEqual(h.toastCalls[h.toastCalls.length - 1].title, '已取消收藏');
}

async function testCollectionPullDownRefreshReloadsFirstPage() {
  const h = createHarness(COLLECTION_MODULE);
  h.storage.PAICODING_TOKEN = 'collection-token';
  h.page.setData({
    articles: [{ articleId: 9001, title: '旧收藏' }],
    page: 3,
    hasMore: false
  });
  h.requestQueue.push(
    ok({ userId: 41, nickName: 'collector' }),
    ok({ list: [{ articleId: 9003, title: '刷新收藏' }], hasMore: true })
  );

  await h.page.onPullDownRefresh();

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/user/me'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/collections'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [9003]);
  assert.strictEqual(h.page.data.page, 2);
  assert.strictEqual(h.stopPullDownCalls.length, 1);
}

async function testProfileOpensCollectionPage() {
  const h = createHarness(PROFILE_MODULE);

  h.page.openCollections();

  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/collection/collection' }]);
}

(async () => {
  await testCollectionLoadPaginationAndNavigation();
  await testCollectionRemove();
  await testCollectionPullDownRefreshReloadsFirstPage();
  await testProfileOpensCollectionPage();
  console.log('miniapp collection page tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
