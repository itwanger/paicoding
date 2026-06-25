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
  const envVersion = getEnvVersion();
  return API_BASE_URL[envVersion] || API_BASE_URL.develop;
}

function allowMockLogin() {
  return getEnvVersion() === 'develop';
}

module.exports = {
  API_BASE_URL,
  requestTimeout: 15000,
  uploadTimeout: 30000,
  forceMockLogin: false,
  getEnvVersion,
  getApiBaseUrl,
  allowMockLogin
};
