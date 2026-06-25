const config = require('./config');

const TOKEN_KEY = 'PAICODING_TOKEN';
const USER_KEY = 'PAICODING_USER';
const LOGGED_OUT_KEY = 'PAICODING_LOGGED_OUT';
const DEVICE_ID_KEY = 'PAICODING_DEVICE_ID';
const NEED_PROFILE_KEY = 'PAICODING_NEED_PROFILE';
const PROFILE_PROMPTED_KEY = 'PAICODING_PROFILE_PROMPTED_USER_ID';
const DEVICE_ID_HEADER = 'X-Pai-Device-Id';

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function isValidDeviceId(value) {
  return typeof value === 'string'
    && value.length >= 12
    && value.length <= 80
    && /^fp-[A-Za-z0-9:_-]+$/.test(value);
}

function randomSegment() {
  return Math.random().toString(36).slice(2, 10);
}

function createDeviceId() {
  return `fp-mini-${Date.now().toString(36)}-${randomSegment()}${randomSegment()}`;
}

function getDeviceId() {
  let deviceId = wx.getStorageSync(DEVICE_ID_KEY) || '';
  if (!isValidDeviceId(deviceId)) {
    deviceId = createDeviceId();
    wx.setStorageSync(DEVICE_ID_KEY, deviceId);
  }
  return deviceId;
}

function buildHeader(extraHeader, jsonContentType) {
  const header = Object.assign(jsonContentType ? {
    'content-type': 'application/json'
  } : {}, extraHeader || {});
  header[DEVICE_ID_HEADER] = getDeviceId();
  const token = getToken();
  if (token) {
    header.Authorization = `Bearer ${token}`;
  }
  return header;
}

function clearLogin() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
  wx.removeStorageSync(NEED_PROFILE_KEY);
  wx.removeStorageSync(PROFILE_PROMPTED_KEY);
}

function markLoggedOut() {
  clearLogin();
  wx.setStorageSync(LOGGED_OUT_KEY, true);
}

function clearLoggedOut() {
  wx.removeStorageSync(LOGGED_OUT_KEY);
}

function isLoggedOut() {
  return Boolean(wx.getStorageSync(LOGGED_OUT_KEY));
}

function normalizeError(message, code, statusCode) {
  const err = new Error(message || '请求失败');
  err.code = code;
  err.statusCode = statusCode;
  return err;
}

function resolveBody(res) {
  const body = res.data || {};
  const status = body.status || {};
  if (res.statusCode < 200 || res.statusCode >= 300) {
    throw normalizeError(`网络请求失败(${res.statusCode})`, status.code, res.statusCode);
  }
  if (status.code === 0) {
    return body.result;
  }
  if (status.code === 100403003) {
    clearLogin();
  }
  throw normalizeError(status.msg || '请求失败', status.code, res.statusCode);
}

function parseUploadBody(data) {
  try {
    return JSON.parse(data || '{}');
  } catch (e) {
    throw normalizeError('上传响应格式错误');
  }
}

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${config.getApiBaseUrl()}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: buildHeader(options.header, true),
      timeout: options.timeout || config.requestTimeout,
      success(res) {
        try {
          resolve(resolveBody(res));
        } catch (e) {
          reject(e);
        }
      },
      fail(err) {
        reject(normalizeError(err.errMsg || '网络连接失败'));
      }
    });
  });
}

function upload(url, filePath, name) {
  const token = getToken();
  if (!token) {
    return Promise.reject(normalizeError('请先登录', 100403003));
  }
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${config.getApiBaseUrl()}${url}`,
      filePath,
      name: name || 'image',
      header: buildHeader({}, false),
      timeout: config.uploadTimeout,
      success(res) {
        try {
          resolve(resolveBody({
            statusCode: res.statusCode,
            data: parseUploadBody(res.data)
          }));
        } catch (e) {
          reject(e);
        }
      },
      fail(err) {
        reject(normalizeError(err.errMsg || '上传失败'));
      }
    });
  });
}

module.exports = {
  request,
  upload,
  getToken,
  getDeviceId,
  clearLogin,
  markLoggedOut,
  clearLoggedOut,
  isLoggedOut
};
