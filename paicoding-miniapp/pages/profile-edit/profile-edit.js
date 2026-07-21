const auth = require('../../utils/auth')
const { request, upload } = require('../../utils/request')

function isInvalidText(value, maxLength) {
  return value.length > maxLength || /[\n\r\t]/.test(value)
}

Page({
  data: {
    user: {},
    nickName: '',
    avatarUrl: '',
    profile: '',
    uploading: false,
    saving: false,
  },

  onLoad() {
    this.initProfile()
  },

  initProfile() {
    const user = auth.getStoredUser()
    this.setData({
      user,
      nickName: user.nickName || '',
      avatarUrl: user.avatarUrl || '',
      profile: user.profile || '',
    })
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value })
  },

  onProfileInput(e) {
    this.setData({ profile: e.detail.value })
  },

  async onChooseAvatar(e) {
    if (this.data.uploading || this.data.saving) {
      return
    }
    const oldAvatarUrl = this.data.avatarUrl
    const localAvatarUrl = e.detail.avatarUrl
    if (!localAvatarUrl) {
      wx.showToast({ title: '未选择头像', icon: 'none' })
      return
    }
    this.setData({ uploading: true, avatarUrl: localAvatarUrl })
    try {
      const user = await upload('/mini/api/user/avatar', localAvatarUrl, 'image')
      auth.persistUser(user)
      this.setData({
        user,
        avatarUrl: user.avatarUrl || localAvatarUrl,
      })
      wx.showToast({ title: '头像已更新', icon: 'success' })
    }
    catch (err) {
      this.setData({ avatarUrl: oldAvatarUrl })
      wx.showToast({ title: err.message || '头像上传失败', icon: 'none' })
    }
    finally {
      this.setData({ uploading: false })
    }
  },

  async saveProfile() {
    if (this.data.saving || this.data.uploading) {
      return
    }
    const nickName = (this.data.nickName || '').trim()
    if (!nickName) {
      wx.showToast({ title: '请填写昵称', icon: 'none' })
      return
    }
    if (isInvalidText(nickName, 50)) {
      wx.showToast({ title: '昵称格式不合法', icon: 'none' })
      return
    }
    const profile = (this.data.profile || '').trim()
    if (isInvalidText(profile, 225)) {
      wx.showToast({ title: '简介格式不合法', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      const user = await request({
        url: '/mini/api/user/profile',
        method: 'POST',
        data: {
          nickName,
          profile,
        },
      })
      auth.persistUser(user)
      this.setData({
        user,
        nickName: user.nickName || nickName,
        avatarUrl: user.avatarUrl || this.data.avatarUrl,
        profile: user.profile || profile,
      })
      wx.showToast({ title: '已保存', icon: 'success' })
      wx.navigateBack()
    }
    catch (err) {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }
    finally {
      this.setData({ saving: false })
    }
  },
})
