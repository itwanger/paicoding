#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SKILL_DIR = path.join(ROOT, 'paicoding-miniapp/skills/article-search');
const SEARCH_MODULE = path.join(SKILL_DIR, 'apis/searchArticles.js');
const DETAIL_MODULE = path.join(SKILL_DIR, 'apis/getArticleDetail.js');
const REQUEST_MODULE = path.join(SKILL_DIR, 'utils/request.js');
const INDEX_MODULE = path.join(SKILL_DIR, 'index.js');

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

function clearSkillModules() {
  [SEARCH_MODULE, DETAIL_MODULE, REQUEST_MODULE, INDEX_MODULE].forEach((mod) => {
    delete require.cache[mod];
  });
}

function createHarness() {
  const requestQueue = [];
  const requestCalls = [];
  const registrations = {};

  global.wx = {
    getAccountInfoSync() {
      return { miniProgram: { envVersion: 'develop' } };
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
    modelContext: {
      registerAPI(name, fn) {
        registrations[name] = fn;
      }
    }
  };

  clearSkillModules();

  return {
    requestQueue,
    requestCalls,
    registrations,
    searchArticles: require(SEARCH_MODULE),
    getArticleDetail: require(DETAIL_MODULE),
    loadIndex() {
      require(INDEX_MODULE);
    }
  };
}

async function testSearchArticlesContract() {
  const h = createHarness();
  h.requestQueue.push(ok({
    list: [
      {
        articleId: 101,
        title: 'Java 并发',
        shortTitle: '并发',
        searchHit: '命中摘要',
        authorName: '二哥',
        readCount: 88,
        cover: 'https://cdn.paicoding.com/cover.png'
      }
    ],
    hasMore: true
  }));

  const res = await h.searchArticles({ keyword: 'Java', page: 2, size: 99 });

  assert.strictEqual(h.requestCalls.length, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/search'), true);
  assert.deepStrictEqual(h.requestCalls[0].data, { key: 'Java', page: 2, size: 10 });
  assert.strictEqual(res.content[0].type, 'text');
  assert.deepStrictEqual(res.structuredContent, {
    articles: [
      {
        articleId: 101,
        title: '并发',
        summary: '命中摘要',
        authorName: '二哥',
        readCount: 88,
        praiseCount: 0,
        collectionCount: 0,
        commentCount: 0,
        cover: 'https://cdn.paicoding.com/cover.png'
      }
    ],
    hasMore: true
  });
}

async function testSearchArticlesRequiresKeyword() {
  const h = createHarness();
  await assert.rejects(
    () => h.searchArticles({ keyword: '   ' }),
    /keyword is required/
  );
  assert.strictEqual(h.requestCalls.length, 0);
}

async function testSearchArticlesRejectsTooLongKeyword() {
  const h = createHarness();
  await assert.rejects(
    () => h.searchArticles({ keyword: 'a'.repeat(65) }),
    /keyword length must be <= 64/
  );
  assert.strictEqual(h.requestCalls.length, 0);
}

async function testSearchArticlesNormalizesPagination() {
  const h = createHarness();
  h.requestQueue.push(ok({
    list: [],
    hasMore: false
  }));

  await h.searchArticles({ keyword: 'Java', page: 'bad', size: -10 });

  assert.strictEqual(h.requestCalls.length, 1);
  assert.deepStrictEqual(h.requestCalls[0].data, { key: 'Java', page: 1, size: 5 });
}

async function testGetArticleDetailContract() {
  const h = createHarness();
  h.requestQueue.push(ok({
    articleId: 202,
    title: 'Spring Boot',
    summary: '详情摘要',
    authorName: '技术派',
    readCount: 123,
    tags: ['Spring', 'Java'],
    cover: ''
  }));

  const res = await h.getArticleDetail({ articleId: 202 });

  assert.strictEqual(h.requestCalls.length, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/articles/202'), true);
  assert.strictEqual(res.content[0].type, 'text');
  assert.deepStrictEqual(res.structuredContent.article, {
    articleId: 202,
    title: 'Spring Boot',
    summary: '详情摘要',
    authorName: '技术派',
    readCount: 123,
    praiseCount: 0,
    collectionCount: 0,
    commentCount: 0,
    canRead: true,
    tags: ['Spring', 'Java'],
    cover: ''
  });
}

async function testGetArticleDetailNormalizesStringId() {
  const h = createHarness();
  h.requestQueue.push(ok({
    articleId: 303,
    title: 'JDK8',
    tags: []
  }));

  await h.getArticleDetail({ articleId: '303' });

  assert.strictEqual(h.requestCalls.length, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/articles/303'), true);
}

async function testGetArticleDetailRequiresId() {
  const h = createHarness();
  await assert.rejects(
    () => h.getArticleDetail({}),
    /articleId is required/
  );
  await assert.rejects(
    () => h.getArticleDetail({ articleId: 'abc' }),
    /articleId is required/
  );
  assert.strictEqual(h.requestCalls.length, 0);
}

function testIndexRegistersApis() {
  const h = createHarness();
  h.loadIndex();
  assert.strictEqual(typeof h.registrations.searchArticles, 'function');
  assert.strictEqual(typeof h.registrations.getArticleDetail, 'function');
}

(async () => {
  await testSearchArticlesContract();
  await testSearchArticlesRequiresKeyword();
  await testSearchArticlesRejectsTooLongKeyword();
  await testSearchArticlesNormalizesPagination();
  await testGetArticleDetailContract();
  await testGetArticleDetailNormalizesStringId();
  await testGetArticleDetailRequiresId();
  testIndexRegistersApis();
  console.log('miniapp ai skill tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
