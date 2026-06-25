#!/usr/bin/env node
'use strict';

const assert = require('assert');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
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

function uploadOk(result) {
  return {
    statusCode: 200,
    data: JSON.stringify(ok(result).data)
  };
}

function uploadFail(message) {
  return {
    statusCode: 500,
    data: JSON.stringify({
      status: { code: 500, msg: message || 'upload failed' },
      result: null
    })
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

function clearModules() {
  [PROFILE_MODULE, AUTH_MODULE, REQUEST_MODULE, CONFIG_MODULE, PRIVACY_MODULE].forEach((mod) => {
    delete require.cache[mod];
  });
}

function createHarness(options = {}) {
  const storage = {};
  const requestQueue = [];
  const uploadQueue = [];
  const requestCalls = [];
  const uploadCalls = [];
  const toastCalls = [];
  const stopPullDownCalls = [];
  let openPrivacyContractCalls = 0;
  let needPrivacyAuthorizationCallback = null;
  const privacyResolverCalls = [];
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
    uploadFile(options) {
      uploadCalls.push(options);
      const handler = uploadQueue.shift();
      if (!handler) {
        nextTick(() => options.fail({ errMsg: `unexpected upload ${options.url}` }));
        return;
      }
      nextTick(() => options.success(typeof handler === 'function' ? handler(options) : handler));
    },
    getPrivacySetting(wxOptions) {
      nextTick(() => wxOptions.success({ needAuthorization: Boolean(options.needPrivacyAuthorization) }));
    },
    onNeedPrivacyAuthorization(callback) {
      needPrivacyAuthorizationCallback = callback;
    },
    openPrivacyContract() {
      openPrivacyContractCalls += 1;
    },
    showToast(options) {
      toastCalls.push(options);
    },
    stopPullDownRefresh() {
      stopPullDownCalls.push(true);
    }
  };

  clearModules();
  require(PROFILE_MODULE);

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
    uploadQueue,
    requestCalls,
    uploadCalls,
    toastCalls,
    stopPullDownCalls,
    get openPrivacyContractCalls() {
      return openPrivacyContractCalls;
    },
    get privacyResolverCalls() {
      return privacyResolverCalls;
    },
    triggerNeedPrivacyAuthorization() {
      assert(needPrivacyAuthorizationCallback, 'privacy authorization callback should be registered');
      needPrivacyAuthorizationCallback((result) => {
        privacyResolverCalls.push(result);
      });
    },
    get loginCalls() {
      return loginCalls;
    }
  };
}

async function testAvatarUploadStoresServerUrlOnly() {
  const h = createHarness();
  h.requestQueue.push(ok({
    token: 'avatar-token',
    user: { userId: 11, nickName: 'avatar-user', avatarUrl: '' }
  }));
  h.uploadQueue.push(uploadOk({
    userId: 11,
    nickName: 'avatar-user',
    avatarUrl: 'https://cdn.paicoding.com/avatar/server.png'
  }));

  await h.page.onChooseAvatar({ detail: { avatarUrl: 'wxfile://tmp-avatar' } });

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/auth/login'), true);
  assert.strictEqual(h.uploadCalls.length, 1);
  assert.strictEqual(h.uploadCalls[0].url.endsWith('/mini/api/user/avatar'), true);
  assert.strictEqual(h.uploadCalls[0].filePath, 'wxfile://tmp-avatar');
  assert.strictEqual(h.uploadCalls[0].header.Authorization, 'Bearer avatar-token');
  assert.strictEqual(h.page.data.avatarUrl, 'https://cdn.paicoding.com/avatar/server.png');
  assert.strictEqual(h.page.data.profileIncomplete, false);
  assert.strictEqual(h.storage.PAICODING_USER.avatarUrl, 'https://cdn.paicoding.com/avatar/server.png');
  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, undefined);
  assert.notStrictEqual(h.storage.PAICODING_USER.avatarUrl, 'wxfile://tmp-avatar');
}

async function testAvatarUploadFailureRestoresOldAvatar() {
  const h = createHarness();
  h.page.setData({
    avatarUrl: 'https://cdn.paicoding.com/avatar/old.png'
  });
  h.storage.PAICODING_TOKEN = 'old-token';
  h.requestQueue.push(ok({
    userId: 12,
    nickName: 'old-user',
    avatarUrl: 'https://cdn.paicoding.com/avatar/old.png'
  }));
  h.uploadQueue.push(uploadFail('bad avatar'));

  await h.page.onChooseAvatar({ detail: { avatarUrl: 'wxfile://bad-avatar' } });

  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.uploadCalls.length, 1);
  assert.strictEqual(h.page.data.avatarUrl, 'https://cdn.paicoding.com/avatar/old.png');
  assert.strictEqual(h.storage.PAICODING_USER.avatarUrl, 'https://cdn.paicoding.com/avatar/old.png');
  assert.notStrictEqual(h.storage.PAICODING_USER.avatarUrl, 'wxfile://bad-avatar');
  assert.strictEqual(h.toastCalls[h.toastCalls.length - 1].title, '头像上传失败');
}

async function testSaveProfileRejectsInvalidNicknameWithoutRequest() {
  const h = createHarness();
  h.page.setData({ nickName: '   ' });

  await h.page.saveProfile();

  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.requestCalls.length, 0);
  assert.strictEqual(h.toastCalls[0].title, '请填写昵称');

  h.page.setData({ nickName: 'bad\nname' });
  await h.page.saveProfile();

  assert.strictEqual(h.requestCalls.length, 0);
  assert.strictEqual(h.toastCalls[1].title, '昵称格式不合法');

  h.page.setData({ nickName: '正常昵称', profile: 'a'.repeat(226) });
  await h.page.saveProfile();

  assert.strictEqual(h.requestCalls.length, 0);
  assert.strictEqual(h.toastCalls[2].title, '简介格式不合法');

  h.page.setData({ nickName: '正常昵称', profile: 'bad\nprofile' });
  await h.page.saveProfile();

  assert.strictEqual(h.requestCalls.length, 0);
  assert.strictEqual(h.toastCalls[3].title, '简介格式不合法');
}

async function testSaveProfileLogsInAndStoresReturnedUser() {
  const h = createHarness();
  h.page.setData({ nickName: '新昵称', profile: 'Java 后端' });
  h.requestQueue.push(
    ok({ token: 'profile-token', user: { userId: 13, nickName: 'old-name', avatarUrl: '' } }),
    ok({ userId: 13, nickName: '新昵称', avatarUrl: 'https://cdn.paicoding.com/avatar/profile.png', profile: 'Java 后端' })
  );

  await h.page.saveProfile();

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.requestCalls.length, 2);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/profile'), true);
  assert.strictEqual(h.requestCalls[1].method, 'POST');
  assert.deepStrictEqual(h.requestCalls[1].data, { nickName: '新昵称', profile: 'Java 后端' });
  assert.strictEqual(h.requestCalls[1].header.Authorization, 'Bearer profile-token');
  assert.strictEqual(h.page.data.nickName, '新昵称');
  assert.strictEqual(h.page.data.profile, 'Java 后端');
  assert.strictEqual(h.page.data.profileIncomplete, false);
  assert.strictEqual(h.storage.PAICODING_USER.avatarUrl, 'https://cdn.paicoding.com/avatar/profile.png');
  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, undefined);
  assert.strictEqual(h.toastCalls[h.toastCalls.length - 1].title, '已保存');
}

async function testPrivacyAuthorizationStateAndContractEntry() {
  const h = createHarness({ needPrivacyAuthorization: true });
  h.requestQueue.push(ok({
    token: 'privacy-token',
    user: { userId: 16, nickName: 'privacy-user', avatarUrl: '' }
  }));

  await h.page.loadUser();

  assert.strictEqual(h.loginCalls, 1);
  assert.strictEqual(h.page.data.loggedIn, true);
  assert.strictEqual(h.page.data.needPrivacyAuthorization, true);

  h.page.openPrivacyContract();
  assert.strictEqual(h.openPrivacyContractCalls, 1);

  h.page.onAgreePrivacyAuthorization();
  assert.strictEqual(h.page.data.needPrivacyAuthorization, false);
}

async function testNeedPrivacyAuthorizationCallbackReachesProfilePage() {
  const h = createHarness();

  h.page.onLoad();
  h.triggerNeedPrivacyAuthorization();

  assert.strictEqual(h.page.data.needPrivacyAuthorization, true);

  h.page.onAgreePrivacyAuthorization();

  assert.strictEqual(h.page.data.needPrivacyAuthorization, false);
  assert.deepStrictEqual(h.privacyResolverCalls, [{
    event: 'agree',
    buttonId: 'profile-privacy-agree'
  }]);

  h.page.onUnload();
  h.triggerNeedPrivacyAuthorization();

  assert.strictEqual(h.page.data.needPrivacyAuthorization, false);
}

async function testSaveProfileCanClearProfile() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'profile-token';
  h.page.setData({ nickName: '新昵称', profile: '   ' });
  h.requestQueue.push(
    ok({ userId: 14, nickName: '新昵称', avatarUrl: '', profile: '旧简介' }),
    ok({ userId: 14, nickName: '新昵称', avatarUrl: '', profile: '' })
  );

  await h.page.saveProfile();

  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.requestCalls.length, 2);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/user/me'), true);
  assert.strictEqual(h.requestCalls[1].url.endsWith('/mini/api/user/profile'), true);
  assert.deepStrictEqual(h.requestCalls[1].data, { nickName: '新昵称', profile: '' });
  assert.strictEqual(h.page.data.profile, '');
  assert.strictEqual(h.page.data.profileIncomplete, true);
  assert.strictEqual(h.storage.PAICODING_USER.profile, '');
  assert.strictEqual(h.storage.PAICODING_NEED_PROFILE, true);
  assert.strictEqual(h.toastCalls[h.toastCalls.length - 1].title, '已保存');
}

async function testLogoutPreventsPassiveReloginOnShow() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'logout-token';
  h.requestQueue.push(ok({ done: true }));

  await h.page.logout();
  await h.page.onShow();

  assert.strictEqual(h.storage.PAICODING_LOGGED_OUT, true);
  assert.strictEqual(h.storage.PAICODING_TOKEN, undefined);
  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.page.data.loggedIn, false);
  assert.match(h.page.data.error, /请先登录/);
}

async function testProfilePullDownRefreshReloadsCurrentUser() {
  const h = createHarness();
  h.storage.PAICODING_TOKEN = 'profile-token';
  h.requestQueue.push(ok({
    userId: 17,
    nickName: '刷新昵称',
    avatarUrl: 'https://cdn.paicoding.com/avatar/refresh.png',
    profile: '刷新简介'
  }));

  await h.page.onPullDownRefresh();

  assert.strictEqual(h.loginCalls, 0);
  assert.strictEqual(h.requestCalls[0].url.endsWith('/mini/api/user/me'), true);
  assert.strictEqual(h.requestCalls[0].header.Authorization, 'Bearer profile-token');
  assert.strictEqual(h.page.data.nickName, '刷新昵称');
  assert.strictEqual(h.page.data.profile, '刷新简介');
  assert.strictEqual(h.page.data.loggedIn, true);
  assert.strictEqual(h.stopPullDownCalls.length, 1);
}

(async () => {
  await testAvatarUploadStoresServerUrlOnly();
  await testAvatarUploadFailureRestoresOldAvatar();
  await testSaveProfileRejectsInvalidNicknameWithoutRequest();
  await testSaveProfileLogsInAndStoresReturnedUser();
  await testPrivacyAuthorizationStateAndContractEntry();
  await testNeedPrivacyAuthorizationCallbackReachesProfilePage();
  await testSaveProfileCanClearProfile();
  await testLogoutPreventsPassiveReloginOnShow();
  await testProfilePullDownRefreshReloadsCurrentUser();
  console.log('miniapp profile page tests: ok');
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
