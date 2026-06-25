const auth = require('../../utils/auth');

Page({
  data: {
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    refreshing: false,
    error: ''
  },

  async onLoad() {
    await this.loadHistory(true);
  },

  async loadHistory(reset = false) {
    if (this.data.loading && !reset) return;
    if (!reset && !this.data.hasMore) return;
    const page = reset ? 1 : this.data.page;
    this.setData({ loading: true, error: '' });
    try {
      const result = await auth.requestWithLogin({
        url: '/mini/api/user/reads',
        data: {
          page,
          size: this.data.size
        }
      });
      const list = Array.isArray(result.list) ? result.list : [];
      this.setData({
        articles: reset ? list : this.data.articles.concat(list),
        hasMore: Boolean(result.hasMore),
        page: page + 1
      });
    } catch (err) {
      this.setData({ error: err.message || '浏览历史加载失败' });
    } finally {
      this.setData({ loading: false, refreshing: false });
    }
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh();
    }
  },

  async refresh(stopNative = false) {
    const shouldStopNative = stopNative === true;
    this.setData({ refreshing: true, hasMore: true });
    try {
      await this.loadHistory(true);
    } finally {
      this.setData({ refreshing: false });
      if (shouldStopNative) {
        this.stopNativePullDownRefresh();
      }
    }
  },

  onPullDownRefresh() {
    return this.refresh(true);
  },

  loadMore() {
    return this.loadHistory(false);
  },

  openDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
    });
  },

  retry() {
    return this.loadHistory(true);
  }
});
