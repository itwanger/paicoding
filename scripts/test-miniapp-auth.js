#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
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

function expired() {
  return {
    statusCode: 200,
    data: {
      status: { code: 100403003, msg: 'token expired' },
      result: null
    }
  };
}

function serverError() {
  return {
    statusCode: 500,
    data: {
      status: { code: 500, msg: 'server error' },
      result: null
    }
  };
}

function uploadOk(result) {
  return {
    statusCode: 200,
    data: JSON.stringify(ok(result).data)
  };
}

function uploadExpired() {
  return {
    statusCode: 200,
    data: JSON.stringify(expired().data)
  };
}

function nextTick(fn) {
  setTimeout(fn, 0);
}

function createHarness() {
  const storage = {};
  const requestQueue = [];
  const uploadQueue = [];
  const requestCalls = [];
  const uploadCalls = [];
  let loginCalls = 0;

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
    uploadFile(options) {
      uploadCalls.push(options);
      const handler = uploadQueue.shift();
      if (!handler) {
        nextTick(() => options.fail({ errMsg: `unexpected upload ${options.url}` }));
        return;
      }
      nextTick(() => options.success(typeof handler === 'function' ? handler(options) : handler));
    },
    showModal(options) {
      nextTick(() => options.success({ confirm: true }));
    },
    switchTab(options) {
      storage.__lastSwitchTab = options.url;
    }
  };

  delete require.cache[AUTH_MODULE];
  delete require.cache[REQUEST_MODULE];
  delete require.cache[CONFIG_MODULE];
  const auth = require(AUTH_MODULE);
  const request = require(REQUEST_MODULE);

  return {
    auth,
    request,
    storage,
    requestQueue,
    uploadQueue,
    requestCalls,
    uploadCalls,
    get loginCalls() {
      return loginCalls;
    }
  };
}

async function testRequestRetriesAfterExpiredToken() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'stale-token';

  h.requestQueue.push(
    ok({ userId: 1, nickName: 'old-user' }),
    expired(),
    ok({ token: 'fresh-token', user: { userId: 1, nickName: 'new-user' } }),
    ok({ done: true })
  );

  const result = await h.auth.requestWithLogin({
    url: '/mini/api/articles/1/favor?type=2',
    method: 'POST'
  });

  assert.deepStrictEqual(result, { done: true });
  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.storage.PAICODING_TOKEN, 'fresh-token');
  assert.strictEqual(h.requestCalls.length, 4);
  assert.strictEqual(h.requestCalls[1].header.Authorization, 'Bearer stale-token');
  assert.strictEqual(h.requestCalls[2].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.requestCalls[3].header.Authorization, 'Bearer fresh-token');
}

async function testRequestDoesNotRetryNonAuthFailure() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'token';

  h.requestQueue.push(
    ok({ userId: 1, nickName: 'user' }),
    serverError()
  );

  await assert.rejects(
    () => h.auth.requestWithLogin({
      url: '/mini/api/articles/1/favor?type=2',
      method: 'POST'
    }),
    /网络请求失败/
  );
  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.requestCalls.length, 2);
}

async function testRequestRetriesAuthFailureOnlyOnce() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'stale-token';

  h.requestQueue.push(
    ok({ userId: 1, nickName: 'old-user' }),
    expired(),
    ok({ token: 'fresh-token', user: { userId: 1, nickName: 'new-user' } }),
    expired()
  );

  await assert.rejects(
    () => h.auth.requestWithLogin({
      url: '/mini/api/articles/1/favor?type=2',
      method: 'POST'
    }),
    /token expired/
  );
  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls.length, 4);
  assert.strictEqual(h.storage.PAICODING_TOKEN, undefined);
}

async function testUploadRetriesAfterExpiredToken() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'stale-token';

  h.requestQueue.push(
    ok({ userId: 2, nickName: 'old-user' }),
    ok({ token: 'fresh-upload-token', user: { userId: 2, nickName: 'new-user' } })
  );
  h.uploadQueue.push(
    uploadExpired(),
    uploadOk({ userId: 2, avatarUrl: 'https://cdn.paicoding.com/avatar.png' })
  );

  const result = await h.auth.uploadWithLogin('/mini/api/user/avatar', '/tmp/avatar.png', 'image');

  assert.deepStrictEqual(result, {
    userId: 2,
    avatarUrl: 'https://cdn.paicoding.com/avatar.png'
  });
  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.storage.PAICODING_TOKEN, 'fresh-upload-token');
  assert.strictEqual(h.uploadCalls.length, 2);
  assert.strictEqual(h.uploadCalls[0].header.Authorization, 'Bearer stale-token');
  assert.strictEqual(h.uploadCalls[1].header.Authorization, 'Bearer fresh-upload-token');
}

async function testLoggedOutActionCanForceLogin() {
  const h = createHarness();
  h.request.markLoggedOut();
  h.requestQueue.push(
    ok({ token: 'fresh-token', user: { userId: 3, nickName: 'new-user' } }),
    ok({ done: true })
  );

  const result = await h.auth.requestWithLogin({
    url: '/mini/api/articles/1/favor?type=2',
    method: 'POST'
  });

  assert.deepStrictEqual(result, { done: true });
  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.storage.PAICODING_LOGGED_OUT, undefined);
}

async function testExplicitLogoutStillBlocksPassiveLogin() {
  const h = createHarness();
  h.storage.PAICODING_NEED_PROFILE = true;
  h.storage.PAICODING_PROFILE_PROMPTED_USER_ID = 7;
  h.request.markLoggedOut();

  await assert.rejects(
    () => h.auth.ensureLogin(),
    /请先登录/
  );
  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, undefined);
  assert.strictEqual(h.storage.PAICODING_PROFILE_PROMPTED_USER_ID, undefined);
}

async function testRequestAddsStableDeviceHeader() {
  const h = createHarness();
  h.storage.PAICODING_DEVICE_ID = 'bad<script>';
  h.requestQueue.push(
    ok({ first: true }),
    ok({ second: true })
  );

  await h.request.request({ url: '/mini/api/articles' });
  const deviceId = h.requestCalls[0].header['X-Pai-Device-Id'];

  assert.strictEqual(/^fp-[A-Za-z0-9:_-]+$/.test(deviceId), true);
  assert.strictEqual(deviceId.length <= 80, true);
  assert.strictEqual(h.storage.PAICODING_DEVICE_ID, deviceId);

  await h.request.request({ url: '/mini/api/categories' });

  assert.strictEqual(h.requestCalls[1].header['X-Pai-Device-Id'], deviceId);
}

async function testUploadAddsStableDeviceHeader() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'upload-token';
  h.storage.PAICODING_DEVICE_ID = 'fp-mini-stable-device-123456';
  h.uploadQueue.push(uploadOk({ ok: true }));

  await h.request.upload('/mini/api/user/avatar', '/tmp/avatar.png', 'image');

  assert.strictEqual(h.uploadCalls[0].header.Authorization, 'Bearer upload-token');
  assert.strictEqual(h.uploadCalls[0].header['X-Pai-Device-Id'], 'fp-mini-stable-device-123456');
  assert.strictEqual(h.uploadCalls[0].header['content-type'], undefined);
}

async function testLoginStoresNeedProfileFlag() {
  const h = createHarness();
  h.requestQueue.push(ok({
    token: 'profile-token',
    needProfile: true,
    user: { userId: 8, nickName: '用户8', avatarUrl: '' }
  }));

  await h.auth.login();

  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, true);
  assert.deepStrictEqual(h.storage.PAICODING_USER, { userId: 8, nickName: '用户8', avatarUrl: '' });
}

async function testPromptProfileSwitchesToProfileOnce() {
  const h = createHarness();
  h.storage.PAICODING_USER = { userId: 9, nickName: '用户9', avatarUrl: '' };
  h.storage.PAICODING_NEED_PROFILE = true;

  const prompted = await h.auth.promptProfileIfNeeded();
  const promptedAgain = await h.auth.promptProfileIfNeeded();

  assert.strictEqual(prompted, true);
  assert.strictEqual(promptedAgain, false);
  assert.strictEqual(h.storage.__lastSwitchTab, '/pages/profile/profile');
  assert.strictEqual(h.storage.PAICODING_PROFILE_PROMPTED_USER_ID, 9);
}

async function testPersistCompleteProfileClearsPrompt() {
  const h = createHarness();
  h.storage.PAICODING_NEED_PROFILE = true;
  h.storage.PAICODING_PROFILE_PROMPTED_USER_ID = 10;

  h.auth.persistUser({ userId: 10, nickName: '沉默王二', avatarUrl: 'https://cdn.paicoding.com/avatar.png' });

  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, undefined);
  assert.strictEqual(h.storage.PAICODING_PROFILE_PROMPTED_USER_ID, undefined);
}

(async () => {
  await testRequestRetriesAfterExpiredToken();
  await testRequestDoesNotRetryNonAuthFailure();
  await testRequestRetriesAuthFailureOnlyOnce();
  await testUploadRetriesAfterExpiredToken();
  await testLoggedOutActionCanForceLogin();
  await testExplicitLogoutStillBlocksPassiveLogin();
  await testRequestAddsStableDeviceHeader();
  await testUploadAddsStableDeviceHeader();
  await testLoginStoresNeedProfileFlag();
  await testPromptProfileSwitchesToProfileOnce();
  await testPersistCompleteProfileClearsPrompt();
  console.log('miniapp auth tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
