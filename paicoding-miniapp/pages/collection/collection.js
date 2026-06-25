const auth = require('../../utils/auth');

Page({
  data: {
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    refreshing: false,
    removingId: null,
    error: ''
  },

  async onLoad() {
    await this.loadCollections(true);
  },

  async loadCollections(reset = false) {
    if (this.data.loading && !reset) return;
    if (!reset && !this.data.hasMore) return;
    const page = reset ? 1 : this.data.page;
    this.setData({ loading: true, error: '' });
    try {
      const result = await auth.requestWithLogin({
        url: '/mini/api/user/collections',
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
      this.setData({ error: err.message || '收藏加载失败' });
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
      await this.loadCollections(true);
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
    return this.loadCollections(false);
  },

  openDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
    });
  },

  async removeCollection(e) {
    const articleId = Number(e.currentTarget.dataset.id);
    if (!articleId || this.data.removingId) return;
    this.setData({ removingId: articleId });
    try {
      await auth.requestWithLogin({
        url: `/mini/api/articles/${articleId}/favor?type=5`,
        method: 'POST'
      });
      this.setData({
        articles: this.data.articles.filter((item) => item.articleId !== articleId)
      });
      wx.showToast({ title: '已取消收藏', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ removingId: null });
    }
  },

  retry() {
    return this.loadCollections(true);
  }
});
