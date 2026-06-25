const auth = require('./utils/auth');
const privacy = require('./utils/privacy');

App({
  globalData: {
    user: null
  },

  onLaunch() {
    privacy.setupPrivacyAuthorization();
    if (wx.showShareMenu) {
      wx.showShareMenu({
        withShareTicket: false,
        menus: ['shareAppMessage', 'shareTimeline']
      });
    }
    this.globalData.user = auth.getStoredUser();
  }
});
