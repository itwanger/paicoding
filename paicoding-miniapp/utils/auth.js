const config = require('./config');
const { request, upload, clearLogin, clearLoggedOut, isLoggedOut } = require('./request');

const USER_KEY = 'PAICODING_USER';
const TOKEN_KEY = 'PAICODING_TOKEN';
const NEED_PROFILE_KEY = 'PAICODING_NEED_PROFILE';
const PROFILE_PROMPTED_KEY = 'PAICODING_PROFILE_PROMPTED_USER_ID';

let loginPromise = null;

function getMockCode() {
  return `mock-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function getLoginCode() {
  if (config.forceMockLogin && config.allowMockLogin()) {
    return Promise.resolve(getMockCode());
  }
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res.code);
          return;
        }
        reject(new Error('微信登录凭证为空'));
      },
      fail() {
        reject(new Error('微信登录失败'));
      }
    });
  });
}

async function login(profile) {
  const code = await getLoginCode();
  const result = await request({
    url: '/mini/api/auth/login',
    method: 'POST',
    data: Object.assign({ code }, profile || {})
  });
  clearLoggedOut();
  wx.setStorageSync(TOKEN_KEY, result.token);
  persistUser(result.user, result.needProfile);
  return result;
}

function loginOnce() {
  if (!loginPromise) {
    loginPromise = login().finally(() => {
      loginPromise = null;
    });
  }
  return loginPromise;
}

async function ensureLogin(options) {
  const force = Boolean(options && options.force);
  const token = wx.getStorageSync('PAICODING_TOKEN');
  if (token) {
    try {
      const user = await request({ url: '/mini/api/user/me' });
      persistUser(user);
      return user;
    } catch (e) {
      clearLogin();
    }
  }
  if (isLoggedOut() && !force) {
    throw new Error('请先登录');
  }
  const result = await loginOnce();
  return result.user;
}

async function retryAfterAuthExpired(action) {
  try {
    return await action();
  } catch (err) {
    if (err && err.code === 100403003) {
      await loginOnce();
      return action();
    }
    throw err;
  }
}

async function requestWithLogin(options) {
  await ensureLogin({ force: true });
  return retryAfterAuthExpired(() => request(options));
}

async function uploadWithLogin(url, filePath, name) {
  await ensureLogin({ force: true });
  return retryAfterAuthExpired(() => upload(url, filePath, name));
}

function isProfileIncomplete(user) {
  if (!user) {
    return true;
  }
  const nickName = String(user.nickName || '').trim();
  return !nickName
    || nickName.indexOf('用户') === 0
    || nickName.indexOf('user_') === 0
    || !String(user.avatarUrl || '').trim();
}

function persistUser(user, needProfile) {
  wx.setStorageSync(USER_KEY, user);
  const shouldCompleteProfile = needProfile == null ? isProfileIncomplete(user) : Boolean(needProfile);
  if (shouldCompleteProfile) {
    wx.setStorageSync(NEED_PROFILE_KEY, true);
  } else {
    wx.removeStorageSync(NEED_PROFILE_KEY);
    wx.removeStorageSync(PROFILE_PROMPTED_KEY);
  }
}

function getStoredUser() {
  return wx.getStorageSync(USER_KEY) || {};
}

function needsProfile() {
  return Boolean(wx.getStorageSync(NEED_PROFILE_KEY));
}

function promptProfileIfNeeded(options) {
  const force = Boolean(options && options.force);
  if (!needsProfile() || !wx.showModal) {
    return Promise.resolve(false);
  }
  const user = wx.getStorageSync(USER_KEY) || {};
  const userId = user.userId || 'unknown';
  if (!force && wx.getStorageSync(PROFILE_PROMPTED_KEY) === userId) {
    return Promise.resolve(false);
  }
  wx.setStorageSync(PROFILE_PROMPTED_KEY, userId);
  return new Promise((resolve) => {
    wx.showModal({
      title: '完善头像昵称',
      content: '使用微信头像和昵称后，点赞收藏等互动状态会更好识别。',
      confirmText: '去完善',
      cancelText: '稍后',
      success(res) {
        if (res.confirm) {
          wx.switchTab({ url: '/pages/profile/profile' });
        }
        resolve(Boolean(res.confirm));
      },
      fail() {
        resolve(false);
      }
    });
  });
}

module.exports = {
  login,
  ensureLogin,
  requestWithLogin,
  uploadWithLogin,
  persistUser,
  getStoredUser,
  isProfileIncomplete,
  needsProfile,
  promptProfileIfNeeded
};
