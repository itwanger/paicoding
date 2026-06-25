#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const INDEX_MODULE = path.join(ROOT, 'paicoding-miniapp/pages/index/index.js');
const SEARCH_MODULE = path.join(ROOT, 'paicoding-miniapp/pages/search/search.js');
const AUTH_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/auth.js');
const REQUEST_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/request.js');
const CONFIG_MODULE = path.join(ROOT, 'paicoding-miniapp/utils/config.js');

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

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
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
  [modulePath, AUTH_MODULE, REQUEST_MODULE, CONFIG_MODULE].forEach((mod) => {
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
      if (typeof handler === 'function' && handler.length > 0) {
        handler(options);
        return;
      }
      nextTick(() => options.success(typeof handler === 'function' ? handler(options) : handler));
    },
    navigateTo(options) {
      navigateCalls.push(options);
    },
    stopPullDownRefresh() {
      stopPullDownCalls.push(true);
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

async function testIndexInitialLoadAndPagination() {
  const h = createHarness(INDEX_MODULE);
  h.requestQueue.push(
    ok({ token: 'index-token', user: { userId: 21, nickName: 'index-user' } }),
    ok([{ categoryId: 0, category: '全部' }, { categoryId: 7, category: 'Java' }]),
    ok({ list: [{ articleId: 1001, title: '第一页' }], hasMore: true }),
    ok({ list: [{ articleId: 1002, title: '第二页' }], hasMore: false })
  );

  await h.page.onLoad();
  h.page.loadMore();
  await wait(10);

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/categories'), true);
  assert.strictEqual(h.requestCalls[2].url.endsWith('/mini/api/articles'), true);
  assert.deepStrictEqual(h.requestCalls[2].data, { categoryId: 0, page: 1, size: 10 });
  assert.deepStrictEqual(h.requestCalls[3].data, { categoryId: 0, page: 2, size: 10 });
  assert.deepStrictEqual(h.page.data.categories, [{ categoryId: 0, category: '全部' }, { categoryId: 7, category: 'Java' }]);
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [1001, 1002]);
  assert.strictEqual(h.page.data.hasMore, false);
}

async function testIndexCategoryAndDetailNavigation() {
  const h = createHarness(INDEX_MODULE);
  h.page.setData({
    articles: [{ articleId: 1001, title: '旧文章' }],
    page: 3,
    hasMore: false
  });
  h.requestQueue.push(ok({ list: [{ articleId: 7001, title: 'Java 文章' }], hasMore: true }));

  h.page.onCategoryTap({ currentTarget: { dataset: { id: '7' } } });
  await wait(10);

  assert.strictEqual(h.requestCalls.length, 1);
  assert.deepStrictEqual(h.requestCalls[0].data, { categoryId: 7, page: 1, size: 10 });
  assert.strictEqual(h.page.data.activeCategoryId, 7);
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [7001]);

  h.page.openDetail({ currentTarget: { dataset: { id: 7001 } } });
  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/detail/detail?id=7001' }]);
}

async function testIndexIgnoresStaleCategoryResult() {
  const h = createHarness(INDEX_MODULE);
  h.page.setData({ activeCategoryId: 0, articles: [{ articleId: 1, title: '旧文章' }] });
  h.requestQueue.push((options) => {
    setTimeout(() => options.success(ok({ list: [{ articleId: 1001, title: '全部旧结果' }], hasMore: false })), 30);
  });
  h.requestQueue.push(ok({ list: [{ articleId: 7002, title: 'Java 新结果' }], hasMore: false }));

  const firstLoad = h.page.loadArticles(true);
  await wait(5);
  h.page.onCategoryTap({ currentTarget: { dataset: { id: '7' } } });
  await firstLoad;
  await wait(40);

  assert.strictEqual(h.requestCalls.length, 2);
  assert.deepStrictEqual(h.requestCalls[0].data, { categoryId: 0, page: 1, size: 10 });
  assert.deepStrictEqual(h.requestCalls[1].data, { categoryId: 7, page: 1, size: 10 });
  assert.strictEqual(h.page.data.activeCategoryId, 7);
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [7002]);
  assert.strictEqual(h.page.data.loading, false);
}

async function testIndexPullDownRefreshReloadsCategoriesAndFirstPage() {
  const h = createHarness(INDEX_MODULE);
  h.page.setData({
    categories: [{ categoryId: 0, category: '旧全部' }],
    articles: [{ articleId: 1, title: '旧文章' }],
    activeCategoryId: 7,
    page: 3,
    hasMore: false
  });
  h.requestQueue.push(
    ok([{ categoryId: 0, category: '全部' }, { categoryId: 7, category: 'Java' }]),
    ok({ list: [{ articleId: 7003, title: '刷新文章' }], hasMore: true })
  );

  await h.page.onPullDownRefresh();

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/categories'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/articles'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { categoryId: 7, page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [7003]);
  assert.strictEqual(h.page.data.page, 2);
  assert.strictEqual(h.stopPullDownCalls.length, 1);
}

async function testIndexScrollViewRefreshSurvivesCategorySwitchRace() {
  const h = createHarness(INDEX_MODULE);
  h.page.setData({
    activeCategoryId: 0,
    articles: [{ articleId: 1, title: '旧文章' }],
    page: 3,
    hasMore: false
  });
  h.requestQueue.push(
    ok([{ categoryId: 0, category: '全部' }, { categoryId: 7, category: 'Java' }]),
    (options) => {
      setTimeout(() => options.success(ok({ list: [{ articleId: 1004, title: '过期刷新' }], hasMore: false })), 30);
    },
    ok({ list: [{ articleId: 7004, title: '切换后文章' }], hasMore: true })
  );

  const refreshPromise = h.page.refresh();
  await wait(5);
  h.page.onCategoryTap({ currentTarget: { dataset: { id: '7' } } });
  await refreshPromise;
  await wait(40);

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/categories'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { categoryId: 0, page: 1, size: 10 });
  assert.deepStrictEqual(h.requestCalls[2].data, { categoryId: 7, page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [7004]);
  assert.strictEqual(h.page.data.activeCategoryId, 7);
  assert.strictEqual(h.page.data.refreshing, false);
  assert.strictEqual(h.stopPullDownCalls.length, 0);
}

async function testSearchInputHintsAndResults() {
  const h = createHarness(SEARCH_MODULE);
  h.requestQueue.push(
    ok({ token: 'search-token', user: { userId: 31, nickName: 'search-user' } }),
    ok([{ articleId: 2001, title: 'Java 提示' }]),
    ok({ list: [{ articleId: 3001, title: 'Java 结果' }], hasMore: true }),
    ok({ list: [{ articleId: 3002, title: 'Java 第二页' }], hasMore: false })
  );

  await h.page.onLoad();
  await h.page.onInput({ detail: { value: 'Java' } });
  await wait(300);
  await h.page.doSearch();
  await h.page.loadMore(false);

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/search/hint'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { key: 'Java' });
  assert.strictEqual(h.page.data.hints.length, 0);
  assert.strictEqual(h.requestCalls[2].url.endsWith('/mini/api/search'), true);
  assert.deepStrictEqual(h.requestCalls[2].data, { key: 'Java', page: 1, size: 10 });
  assert.deepStrictEqual(h.requestCalls[3].data, { key: 'Java', page: 2, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [3001, 3002]);
  assert.strictEqual(h.page.data.hasMore, false);
  assert.deepStrictEqual(h.storage.PAICODING_SEARCH_HISTORY, ['Java']);
}

async function testSearchEmptyInputDoesNotRequestAndNavigation() {
  const h = createHarness(SEARCH_MODULE);
  await h.page.onInput({ detail: { value: '   ' } });
  await h.page.doSearch();

  assert.strictEqual(h.requestCalls.length, 0);
  assert.deepStrictEqual(h.page.data.hints, []);
  assert.strictEqual(h.page.data.searched, false);

  h.page.openDetail({ currentTarget: { dataset: { id: 3001 } } });
  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/detail/detail?id=3001' }]);
}

async function testSearchKeywordNormalization() {
  const h = createHarness(SEARCH_MODULE);
  const longKey = ` ${'a'.repeat(70)} `;
  h.requestQueue.push(
    ok({ token: 'search-token', user: { userId: 32, nickName: 'search-user' } }),
    ok({ list: [], hasMore: false })
  );

  await h.page.onLoad({ key: encodeURIComponent(longKey) });

  assert.strictEqual(h.page.data.key.length, 64);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/search'), true);
  assert.strictEqual(h.requestCalls[1].data.key.length, 64);
  assert.deepStrictEqual(h.storage.PAICODING_SEARCH_HISTORY, ['a'.repeat(64)]);

  await h.page.onInput({ detail: { value: ' Spring ' } });
  assert.strictEqual(h.page.data.key, 'Spring');
  const share = h.page.onShareAppMessage();
  assert.strictEqual(share.path, '/pages/search/search?key=Spring');
  h.page.onUnload();
  assert.strictEqual(h.page.data.hintTimer, null);
}

async function testSearchPullDownRefreshReloadsCurrentResults() {
  const h = createHarness(SEARCH_MODULE);
  h.page.setData({
    key: 'Java',
    searched: true,
    articles: [{ articleId: 1, title: '旧结果' }],
    page: 3,
    hasMore: false,
    history: ['Java']
  });
  h.requestQueue.push(ok({ list: [{ articleId: 3003, title: '刷新结果' }], hasMore: true }));

  await h.page.onPullDownRefresh();

  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/search'), true);
  assert.deepStrictEqual(h.requestCalls[0].data, { key: 'Java', page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [3003]);
  assert.strictEqual(h.page.data.page, 2);
  assert.strictEqual(h.stopPullDownCalls.length, 1);
}

async function testSearchHistoryReuseAndClear() {
  const h = createHarness(SEARCH_MODULE);
  h.storage.PAICODING_SEARCH_HISTORY = ['Java', 'Spring'];
  h.requestQueue.push(
    ok({ token: 'search-token', user: { userId: 33, nickName: 'history-user' } }),
    ok({ list: [{ articleId: 7001, title: 'Spring 结果' }], hasMore: false })
  );

  await h.page.onLoad();
  assert.deepStrictEqual(h.page.data.history, ['Java', 'Spring']);

  await h.page.chooseHistory({ currentTarget: { dataset: { key: 'Spring' } } });

  assert.deepStrictEqual(h.requestCalls[1].data, { key: 'Spring', page: 1, size: 10 });
  assert.deepStrictEqual(h.storage.PAICODING_SEARCH_HISTORY, ['Spring', 'Java']);
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [7001]);

  h.page.clearSearch();
  h.page.clearHistory();

  assert.deepStrictEqual(h.page.data.history, []);
  assert.strictEqual(h.storage.PAICODING_SEARCH_HISTORY, undefined);
}

async function testSearchHintOpensDetailDirectly() {
  const h = createHarness(SEARCH_MODULE);

  h.page.setData({ hints: [{ articleId: 8001, title: 'Java 提示' }] });
  h.page.chooseHint({ currentTarget: { dataset: { id: 8001, title: 'Java 提示' } } });

  assert.deepStrictEqual(h.navigateCalls, [{ url: '/pages/detail/detail?id=8001' }]);
  assert.deepStrictEqual(h.page.data.hints, []);
  assert.strictEqual(h.requestCalls.length, 0);
}

async function testSearchSubmitClearsPendingHintTimer() {
  const h = createHarness(SEARCH_MODULE);
  h.requestQueue.push(ok({ list: [{ articleId: 4001, title: 'Java 结果' }], hasMore: false }));

  await h.page.onInput({ detail: { value: 'Java' } });
  await h.page.doSearch();
  await wait(300);

  assert.strictEqual(h.requestCalls.length, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/search'), true);
  assert.deepStrictEqual(h.requestCalls[0].data, { key: 'Java', page: 1, size: 10 });
  assert.deepStrictEqual(h.page.data.hints, []);
  assert.strictEqual(h.page.data.hintTimer, null);
}

async function testSearchInputClearCancelsPendingHintTimer() {
  const h = createHarness(SEARCH_MODULE);

  await h.page.onInput({ detail: { value: 'Java' } });
  await h.page.onInput({ detail: { value: '   ' } });
  await wait(300);

  assert.strictEqual(h.requestCalls.length, 0);
  assert.deepStrictEqual(h.page.data.hints, []);
  assert.strictEqual(h.page.data.hintTimer, null);
}

async function testSearchIgnoresStaleHintResponseAfterSubmit() {
  const h = createHarness(SEARCH_MODULE);
  h.requestQueue.push((options) => {
    setTimeout(() => options.success(ok([{ articleId: 5001, title: '旧提示' }])), 30);
  });
  h.requestQueue.push(ok({ list: [{ articleId: 5002, title: 'Java 结果' }], hasMore: false }));

  await h.page.onInput({ detail: { value: 'Java' } });
  await wait(260);
  await h.page.doSearch();
  await wait(50);

  assert.strictEqual(h.requestCalls.length, 2);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/search/hint'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/search'), true);
  assert.deepStrictEqual(h.page.data.hints, []);
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [5002]);
}

async function testSearchIgnoresStaleResultWhenNewSearchStarts() {
  const h = createHarness(SEARCH_MODULE);
  h.page.setData({ key: 'Java' });
  h.requestQueue.push((options) => {
    setTimeout(() => options.success(ok({ list: [{ articleId: 6001, title: '旧结果' }], hasMore: false })), 30);
  });
  h.requestQueue.push(ok({ list: [{ articleId: 6002, title: '新结果' }], hasMore: false }));

  const firstSearch = h.page.doSearch();
  await wait(5);
  h.page.setData({ key: 'Spring' });
  await h.page.doSearch();
  await firstSearch;
  await wait(40);

  assert.strictEqual(h.requestCalls.length, 2);
  assert.deepStrictEqual(h.requestCalls[0].data, { key: 'Java', page: 1, size: 10 });
  assert.deepStrictEqual(h.requestCalls[1].data, { key: 'Spring', page: 1, size: 10 });
  assert.strictEqual(h.page.data.key, 'Spring');
  assert.deepStrictEqual(h.page.data.articles.map((item) => item.articleId), [6002]);
  assert.strictEqual(h.page.data.loading, false);
}

(async () => {
  await testIndexInitialLoadAndPagination();
  await testIndexCategoryAndDetailNavigation();
  await testIndexIgnoresStaleCategoryResult();
  await testIndexPullDownRefreshReloadsCategoriesAndFirstPage();
  await testIndexScrollViewRefreshSurvivesCategorySwitchRace();
  await testSearchInputHintsAndResults();
  await testSearchEmptyInputDoesNotRequestAndNavigation();
  await testSearchKeywordNormalization();
  await testSearchPullDownRefreshReloadsCurrentResults();
  await testSearchHistoryReuseAndClear();
  await testSearchHintOpensDetailDirectly();
  await testSearchSubmitClearsPendingHintTimer();
  await testSearchInputClearCancelsPendingHintTimer();
  await testSearchIgnoresStaleHintResponseAfterSubmit();
  await testSearchIgnoresStaleResultWhenNewSearchStarts();
  console.log('miniapp feed/search page tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
