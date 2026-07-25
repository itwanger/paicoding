const auth = require('../../utils/auth');
const config = require('../../utils/config');

const TAB_BAR_PATHS = ['/pages/index/index', '/pages/profile/profile'];

Page({
  data: {
    username: '',
    password: '',
    error: '',
    loading: false,
    redirect: ''
  },

  onLoad(options) {
    this.setData({ redirect: (options && options.redirect) ? options.redirect : '' });
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value, error: '' });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value, error: '' });
  },

  async submit() {
    const username = (this.data.username || '').trim();
    const password = this.data.password || '';
    if (!username || !password) {
      this.setData({ error: '请输入用户名和密码' });
      return;
    }
    this.setData({ loading: true, error: '' });
    try {
      await auth.loginByPassword(username, password);
      wx.showToast({ title: '登录成功', icon: 'success' });
      this.redirectBack();
    } catch (err) {
      this.setData({ error: (err && err.message) ? err.message : '登录失败，请检查用户名和密码' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async loginByWx() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      await auth.login();
      wx.showToast({ title: '登录成功', icon: 'success' });
      this.redirectBack();
    } catch (err) {
      this.setData({ error: (err && err.message) ? err.message : '微信登录失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  redirectBack() {
    const redirect = this.data.redirect;
    if (redirect && TAB_BAR_PATHS.some((p) => redirect.startsWith(p))) {
      // tabBar 页只能用 switchTab，且不能带参数
      wx.switchTab({ url: redirect.split('?')[0], fail: () => wx.switchTab({ url: '/pages/index/index' }) });
      return;
    }
    if (redirect) {
      wx.redirectTo({
        url: redirect,
        fail: () => wx.switchTab({ url: '/pages/index/index' })
      });
      return;
    }
    wx.switchTab({ url: '/pages/index/index' });
  },

  copyRegisterUrl() {
    const baseUrl = (config.getApiBaseUrl() || '').replace(/\/+$/, '');
    wx.setClipboardData({
      data: `${baseUrl}/login`,
      success: () => wx.showToast({ title: '注册链接已复制', icon: 'none' })
    });
  }
});
