const auth = require('../../utils/auth')

function createListState() {
  return {
    users: [],
    page: 1,
    size: 10,
    hasMore: true,
    loaded: false,
    loading: false,
    refreshing: false,
    error: '',
  }
}

Page({
  data: {
    activeTab: 'follow',
    follow: createListState(),
    fans: createListState(),
  },

  async onLoad(options) {
    const activeTab = options && options.tab === 'fans' ? 'fans' : 'follow'
    this.setData({ activeTab })
    await this.loadUsers(activeTab, true)
  },

  async switchTab(event) {
    const type = event.detail.value
    if (type !== 'follow' && type !== 'fans') {
      return
    }
    this.setData({ activeTab: type })
    if (!this.data[type].loaded) {
      await this.loadUsers(type, true)
    }
  },

  async loadUsers(type, reset = false) {
    const state = this.data[type]
    if (!state || state.loading || (!reset && !state.hasMore)) {
      return
    }
    const page = reset ? 1 : state.page
    this.setData({
      [`${type}.loading`]: true,
      [`${type}.error`]: '',
    })
    try {
      const result = await auth.requestWithLogin({
        url: '/mini/api/user/follows',
        data: {
          type,
          page,
          size: state.size,
        },
      })
      const users = this.normalizeUsers(result && result.list)
      this.setData({
        [`${type}.users`]: reset ? users : state.users.concat(users),
        [`${type}.hasMore`]: Boolean(result && result.hasMore),
        [`${type}.page`]: page + 1,
        [`${type}.loaded`]: true,
      })
    }
    catch (err) {
      this.setData({ [`${type}.error`]: err.message || '用户列表加载失败' })
    }
    finally {
      this.setData({
        [`${type}.loading`]: false,
        [`${type}.refreshing`]: false,
      })
    }
  },

  normalizeUsers(users) {
    return Array.isArray(users)
      ? users.map(user => ({
          userId: user.userId,
          userName: user.userName || '技术派用户',
          avatar: user.avatar || '',
          userInitial: (user.userName || '派').slice(0, 1),
          followed: Boolean(user.followed),
          submitting: false,
        }))
      : []
  },

  refresh(event) {
    const type = event.currentTarget.dataset.type
    if (!this.data[type] || this.data[type].loading) {
      return
    }
    this.setData({ [`${type}.refreshing`]: true })
    return this.loadUsers(type, true)
  },

  loadMore(event) {
    return this.loadUsers(event.currentTarget.dataset.type, false)
  },

  retry(event) {
    return this.loadUsers(event.currentTarget.dataset.type, true)
  },

  confirmUnfollow() {
    return new Promise((resolve) => {
      wx.showModal({
        title: '取消关注',
        content: '确定不再关注该用户吗？',
        confirmText: '取消关注',
        confirmColor: '#e34d59',
        success: result => resolve(Boolean(result.confirm)),
        fail: () => resolve(false),
      })
    })
  },

  async toggleRelationship(event) {
    const { type, id } = event.currentTarget.dataset
    const userId = Number(id)
    const state = this.data[type]
    const currentUser = state && state.users.find(user => Number(user.userId) === userId)
    if (!userId || !currentUser || currentUser.submitting) {
      return
    }
    const isFollowed = currentUser.followed
    if (type === 'follow' && isFollowed && !(await this.confirmUnfollow())) {
      return
    }
    const nextFollowed = !isFollowed
    this.setData({
      [`${type}.users`]: this.data[type].users.map(user => Number(user.userId) === userId
        ? Object.assign({}, user, { submitting: true })
        : user),
    })
    try {
      await auth.requestWithLogin({
        url: `/mini/api/users/${userId}/follow`,
        method: 'POST',
        data: { followed: nextFollowed },
      })
      const users = type === 'follow' && !nextFollowed
        ? this.data.follow.users.filter(user => Number(user.userId) !== userId)
        : this.data[type].users.map(user => Number(user.userId) === userId
            ? Object.assign({}, user, { followed: nextFollowed })
            : user)
      this.setData({ [`${type}.users`]: users })
      wx.showToast({
        title: nextFollowed ? (type === 'fans' ? '已互关' : '已关注') : '已取消关注',
        icon: 'success',
      })
    }
    catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' })
    }
    finally {
      this.setData({
        [`${type}.users`]: this.data[type].users.map(user => Number(user.userId) === userId
          ? Object.assign({}, user, { submitting: false })
          : user),
      })
    }
  },
})
