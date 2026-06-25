let installed = false;
let pendingResolve = null;
const listeners = [];

function notify(needAuthorization) {
  listeners.slice().forEach((listener) => {
    try {
      listener(Boolean(needAuthorization));
    } catch (e) {
      // Listener failures should not break the WeChat privacy callback.
    }
  });
}

function setupPrivacyAuthorization() {
  if (installed) {
    return;
  }
  installed = true;
  if (!wx.onNeedPrivacyAuthorization) {
    return;
  }
  wx.onNeedPrivacyAuthorization((resolve) => {
    pendingResolve = typeof resolve === 'function' ? resolve : null;
    notify(true);
  });
}

function onPrivacyAuthorizationNeeded(listener) {
  if (typeof listener !== 'function') {
    return function noop() {};
  }
  listeners.push(listener);
  return function unsubscribe() {
    const index = listeners.indexOf(listener);
    if (index >= 0) {
      listeners.splice(index, 1);
    }
  };
}

function completePrivacyAuthorization(event, buttonId) {
  const resolver = pendingResolve;
  pendingResolve = null;
  if (resolver) {
    resolver({
      event: event || 'agree',
      buttonId: buttonId || 'privacy-agree-btn'
    });
  }
  notify(false);
}

function agreePrivacyAuthorization(buttonId) {
  completePrivacyAuthorization('agree', buttonId);
}

function disagreePrivacyAuthorization() {
  completePrivacyAuthorization('disagree');
}

function getPrivacySetting() {
  if (!wx.getPrivacySetting) {
    return Promise.resolve({ needAuthorization: false });
  }
  return new Promise((resolve) => {
    wx.getPrivacySetting({
      success(res) {
        resolve(res || { needAuthorization: false });
      },
      fail() {
        resolve({ needAuthorization: false });
      }
    });
  });
}

function openPrivacyContract() {
  if (!wx.openPrivacyContract) {
    return;
  }
  wx.openPrivacyContract({
    fail() {
      wx.showToast({ title: '隐私协议暂不可用', icon: 'none' });
    }
  });
}

module.exports = {
  setupPrivacyAuthorization,
  onPrivacyAuthorizationNeeded,
  agreePrivacyAuthorization,
  disagreePrivacyAuthorization,
  getPrivacySetting,
  openPrivacyContract
};
