const auth = require('../../utils/auth')
const privacy = require('../../utils/privacy')
const { request, markLoggedOut } = require('../../utils/request')

function createDefaultStatistics() {
  return {
    joinDayCount: 0,
    followCount: 0,
    fansCount: 0,
    articleCount: 0,
    praiseCount: 0,
    readCount: 0,
    collectionCount: 0,
  }
}

Page({
  data: {
    user: {},
    nickName: '',
    avatarUrl: '',
    profile: '',
    statistics: createDefaultStatistics(),
    loggedIn: false,
    needPrivacyAuthorization: false,
    loading: false,
    refreshing: false,
    loggingOut: false,
    profileIncomplete: false,
    error: '',
  },

  async onShow() {
    await this.loadUser()
  },

  onLoad() {
    privacy.setupPrivacyAuthorization()
    this.unsubscribePrivacyAuthorization = privacy.onPrivacyAuthorizationNeeded((needAuthorization) => {
      this.setData({ needPrivacyAuthorization: Boolean(needAuthorization) })
    })
  },

  onUnload() {
    if (this.unsubscribePrivacyAuthorization) {
      this.unsubscribePrivacyAuthorization()
      this.unsubscribePrivacyAuthorization = null
    }
  },

  async loadUser() {
    this.setData({ loading: true, error: '' })
    // 仅依据本地 token 判断登录态,不再自动登录(未登录则显示"立即登录"入口)
    const token = wx.getStorageSync('PAICODING_TOKEN')
    if (!token) {
      this.setData({
        user: {},
        nickName: '',
        avatarUrl: '',
        profile: '',
        loggedIn: false,
        profileIncomplete: false,
        statistics: createDefaultStatistics(),
        error: '',
      })
      this.setData({ loading: false, refreshing: false })
      return
    }
    try {
      const user = await request({ url: '/mini/api/user/me' })
      this.setData({
        user,
        nickName: user.nickName || '',
        avatarUrl: user.avatarUrl || '',
        profile: user.profile || '',
        loggedIn: true,
        profileIncomplete: auth.isProfileIncomplete(user),
      })
      await this.loadStatistics()
      await this.checkPrivacy()
    }
    catch (err) {
      // token 失效:清掉本地登录态,显示"立即登录"(不自动重登)
      markLoggedOut()
      this.setData({
        user: {},
        nickName: '',
        avatarUrl: '',
        profile: '',
        loggedIn: false,
        profileIncomplete: false,
        statistics: createDefaultStatistics(),
        error: '',
      })
    }
    finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  // 互动入口前的登录态校验:未登录则跳转登录页(不自动登录),并返回 false
  requireLogin() {
    if (wx.getStorageSync('PAICODING_TOKEN')) {
      return true
    }
    wx.navigateTo({ url: '/pages/login/login' })
    return false
  },

  async loadStatistics() {
    try {
      const statistics = await request({ url: '/mini/api/user/statistics' })
      this.setData({ statistics: Object.assign(createDefaultStatistics(), statistics || {}) })
    }
    catch (err) {
      this.setData({ statistics: createDefaultStatistics() })
    }
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh()
    }
  },

  async onPullDownRefresh() {
    this.setData({ refreshing: true })
    try {
      await this.loadUser()
    }
    finally {
      this.stopNativePullDownRefresh()
    }
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login?redirect=' + encodeURIComponent('/pages/profile/profile'),
      fail: () => wx.switchTab({ url: '/pages/profile/profile' })
    });
  },

  async checkPrivacy() {
    const setting = await privacy.getPrivacySetting()
    this.setData({ needPrivacyAuthorization: Boolean(setting.needAuthorization) })
  },

  onAgreePrivacyAuthorization() {
    privacy.agreePrivacyAuthorization('profile-privacy-agree')
    this.setData({ needPrivacyAuthorization: false })
  },

  openPrivacyContract() {
    privacy.openPrivacyContract()
  },

  showEdit() {
    if (!this.requireLogin()) {
      return
    }
    wx.navigateTo({ url: '/pages/profile-edit/profile-edit' })
  },

  async logout() {
    if (this.data.loggingOut) {
      return
    }
    this.setData({ loggingOut: true })
    try {
      await request({ url: '/mini/api/auth/logout', method: 'POST' })
    }
    finally {
      markLoggedOut()
      this.setData({
        user: {},
        nickName: '',
        avatarUrl: '',
        profile: '',
        statistics: createDefaultStatistics(),
        loggedIn: false,
        needPrivacyAuthorization: false,
        profileIncomplete: false,
        loggingOut: false,
      })
    }
  },

  openCollections() {
    if (!this.requireLogin()) return
    wx.navigateTo({ url: '/pages/collection/collection' })
  },

  openMyArticles() {
    if (!this.requireLogin()) return
    wx.navigateTo({ url: '/pages/my-articles/my-articles' })
  },

  openFollowing(event) {
    if (!this.requireLogin()) return
    const tab = event && event.currentTarget.dataset.tab === 'fans' ? 'fans' : 'follow'
    wx.navigateTo({ url: `/pages/following/following?tab=${tab}` })
  },

  openHistory() {
    if (!this.requireLogin()) return
    wx.navigateTo({ url: '/pages/history/history' })
  },

  onShareAppMessage() {
    return {
      title: '技术派 - Java 与 AI 实战社区',
      path: '/pages/index/index',
    }
  },

  onShareTimeline() {
    return {
      title: '技术派 - Java 与 AI 实战社区',
    }
  },
})
