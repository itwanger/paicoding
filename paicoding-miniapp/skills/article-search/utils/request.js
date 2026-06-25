const API_BASE_URL = {
  develop: 'http://127.0.0.1:8080',
  trial: 'https://paicoding.com',
  release: 'https://paicoding.com'
};

function getEnvVersion() {
  try {
    const account = wx.getAccountInfoSync();
    return account && account.miniProgram && account.miniProgram.envVersion
      ? account.miniProgram.envVersion
      : 'develop';
  } catch (e) {
    return 'develop';
  }
}

function getApiBaseUrl() {
  return API_BASE_URL[getEnvVersion()] || API_BASE_URL.develop;
}

function request(options) {
  return new Promise((resolve, reject) => {
    const url = `${getApiBaseUrl()}${options.url}`;
    console.info(`[ai-mode] skill request start path=${options.url}`);
    wx.request({
      url,
      method: 'GET',
      data: options.data || {},
      timeout: 15000,
      success(res) {
        const body = res.data || {};
        const status = body.status || {};
        if (res.statusCode < 200 || res.statusCode >= 300) {
          console.warn(`[ai-mode] skill request http error path=${options.url} statusCode=${res.statusCode}`);
          reject(new Error(`网络请求失败(${res.statusCode})`));
          return;
        }
        if (status.code === 0) {
          console.info(`[ai-mode] skill request success path=${options.url}`);
          resolve(body.result);
          return;
        }
        console.warn(`[ai-mode] skill request biz error path=${options.url} code=${status.code}`);
        reject(new Error(status.msg || '请求失败'));
      },
      fail(err) {
        console.warn(`[ai-mode] skill request fail path=${options.url}`, err && err.errMsg ? err.errMsg : err);
        reject(new Error(err.errMsg || '网络连接失败'));
      }
    });
  });
}

module.exports = {
  request
};
