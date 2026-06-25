const { request, markLoggedOut } = require('../../utils/request');
const auth = require('../../utils/auth');
const privacy = require('../../utils/privacy');

Page({
  data: {
    user: {},
    nickName: '',
    avatarUrl: '',
    profile: '',
    loggedIn: false,
    needPrivacyAuthorization: false,
    loading: false,
    refreshing: false,
    uploading: false,
    saving: false,
    loggingOut: false,
    profileIncomplete: false,
    error: ''
  },

  async onShow() {
    await this.loadUser();
  },

  onLoad() {
    privacy.setupPrivacyAuthorization();
    this.unsubscribePrivacyAuthorization = privacy.onPrivacyAuthorizationNeeded((needAuthorization) => {
      this.setData({ needPrivacyAuthorization: Boolean(needAuthorization) });
    });
  },

  onUnload() {
    if (this.unsubscribePrivacyAuthorization) {
      this.unsubscribePrivacyAuthorization();
      this.unsubscribePrivacyAuthorization = null;
    }
  },

  async loadUser() {
    this.setData({ loading: true, error: '' });
    try {
      const user = await auth.ensureLogin();
      this.setData({
        user,
        nickName: user.nickName || '',
        avatarUrl: user.avatarUrl || '',
        profile: user.profile || '',
        loggedIn: true,
        profileIncomplete: auth.isProfileIncomplete(user)
      });
      await this.checkPrivacy();
    } catch (err) {
      this.setData({ loggedIn: false, profileIncomplete: false, error: err.message || '登录失败' });
    } finally {
      this.setData({ loading: false, refreshing: false });
    }
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh();
    }
  },

  async onPullDownRefresh() {
    this.setData({ refreshing: true });
    try {
      await this.loadUser();
    } finally {
      this.stopNativePullDownRefresh();
    }
  },

  async loginUser() {
    this.setData({ loading: true, error: '' });
    try {
      const result = await auth.login();
      const user = result.user || {};
      this.setData({
        user,
        nickName: user.nickName || '',
        avatarUrl: user.avatarUrl || '',
        profile: user.profile || '',
        loggedIn: true,
        profileIncomplete: auth.isProfileIncomplete(user)
      });
      await this.checkPrivacy();
    } catch (err) {
      this.setData({ loggedIn: false, profileIncomplete: false, error: err.message || '登录失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async checkPrivacy() {
    const setting = await privacy.getPrivacySetting();
    this.setData({ needPrivacyAuthorization: Boolean(setting.needAuthorization) });
  },

  onAgreePrivacyAuthorization() {
    privacy.agreePrivacyAuthorization('profile-privacy-agree');
    this.setData({ needPrivacyAuthorization: false });
  },

  openPrivacyContract() {
    privacy.openPrivacyContract();
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value });
  },

  onProfileInput(e) {
    this.setData({ profile: e.detail.value });
  },

  async onChooseAvatar(e) {
    if (this.data.uploading) return;
    const oldAvatarUrl = this.data.avatarUrl;
    const localAvatarUrl = e.detail.avatarUrl;
    if (!localAvatarUrl) {
      wx.showToast({ title: '未选择头像', icon: 'none' });
      return;
    }
    this.setData({ uploading: true });
    this.setData({ avatarUrl: localAvatarUrl });
    try {
      const user = await auth.uploadWithLogin('/mini/api/user/avatar', localAvatarUrl, 'image');
      auth.persistUser(user);
      this.setData({
        user,
        avatarUrl: user.avatarUrl,
        profileIncomplete: auth.isProfileIncomplete(user)
      });
    } catch (err) {
      this.setData({ avatarUrl: oldAvatarUrl });
      wx.showToast({ title: '头像上传失败', icon: 'none' });
    } finally {
      this.setData({ uploading: false });
    }
  },

  async saveProfile() {
    const nickName = (this.data.nickName || '').trim();
    if (!nickName) {
      wx.showToast({ title: '请填写昵称', icon: 'none' });
      return;
    }
    if (nickName.length > 50 || /[\n\r\t]/.test(nickName)) {
      wx.showToast({ title: '昵称格式不合法', icon: 'none' });
      return;
    }
    const profile = (this.data.profile || '').trim();
    if (profile.length > 225 || /[\n\r\t]/.test(profile)) {
      wx.showToast({ title: '简介格式不合法', icon: 'none' });
      return;
    }
    this.setData({ saving: true });
    try {
      const user = await auth.requestWithLogin({
        url: '/mini/api/user/profile',
        method: 'POST',
        data: {
          nickName,
          profile
        }
      });
      this.setData({
        user,
        nickName: user.nickName || nickName,
        avatarUrl: user.avatarUrl,
        profile: user.profile || profile,
        profileIncomplete: auth.isProfileIncomplete(user)
      });
      auth.persistUser(user);
      wx.showToast({ title: '已保存', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ saving: false });
    }
  },

  async logout() {
    if (this.data.loggingOut) return;
    this.setData({ loggingOut: true });
    try {
      await request({ url: '/mini/api/auth/logout', method: 'POST' });
    } finally {
      markLoggedOut();
      this.setData({
        user: {},
        nickName: '',
        avatarUrl: '',
        profile: '',
        loggedIn: false,
        needPrivacyAuthorization: false,
        profileIncomplete: false,
        loggingOut: false
      });
    }
  },

  openCollections() {
    wx.navigateTo({ url: '/pages/collection/collection' });
  },

  openHistory() {
    wx.navigateTo({ url: '/pages/history/history' });
  },

  onShareAppMessage() {
    return {
      title: '技术派 - Java 与 AI 实战社区',
      path: '/pages/index/index'
    };
  },

  onShareTimeline() {
    return {
      title: '技术派 - Java 与 AI 实战社区'
    };
  }
});
