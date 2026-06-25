const { request } = require('../../utils/request');
const auth = require('../../utils/auth');

Page({
  data: {
    categories: [],
    activeCategoryId: 0,
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    articleRequestId: 0,
    refreshing: false,
    error: ''
  },

  async onLoad() {
    try {
      await auth.ensureLogin();
      auth.promptProfileIfNeeded();
      await this.loadCategories();
      await this.loadArticles(true);
    } catch (err) {
      this.setData({ error: err.message || '加载失败' });
    }
  },

  async loadCategories() {
    try {
      const categories = await request({ url: '/mini/api/categories' });
      this.setData({ categories });
    } catch (err) {
      this.setData({ error: err.message || '分类加载失败' });
    }
  },

  async loadArticles(reset, requestId, categoryId) {
    if (this.data.loading && !reset) return;
    const page = reset ? 1 : this.data.page;
    const activeRequestId = requestId || this.data.articleRequestId + 1;
    const activeCategoryId = categoryId == null ? this.data.activeCategoryId : categoryId;
    if (!requestId) {
      this.setData({ articleRequestId: activeRequestId });
    }
    this.setData({ loading: true, error: '' });
    try {
      const result = await request({
        url: '/mini/api/articles',
        data: {
          categoryId: activeCategoryId,
          page,
          size: this.data.size
        }
      });
      if (this.data.articleRequestId !== activeRequestId || this.data.activeCategoryId !== activeCategoryId) {
        return;
      }
      const list = Array.isArray(result.list) ? result.list : [];
      this.setData({
        articles: reset ? list : this.data.articles.concat(list),
        hasMore: Boolean(result.hasMore),
        page: page + 1
      });
    } catch (err) {
      if (this.data.articleRequestId === activeRequestId) {
        this.setData({ error: err.message || '文章加载失败' });
      }
    } finally {
      if (this.data.articleRequestId === activeRequestId) {
        this.setData({ loading: false, refreshing: false });
      }
    }
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh();
    }
  },

  onCategoryTap(e) {
    const id = Number(e.currentTarget.dataset.id);
    const articleRequestId = this.data.articleRequestId + 1;
    this.setData({
      activeCategoryId: id,
      page: 1,
      hasMore: true,
      articles: [],
      articleRequestId,
      loading: false,
      refreshing: false,
      error: ''
    });
    this.loadArticles(true, articleRequestId, id);
  },

  async refresh(stopNative = false) {
    const shouldStopNative = stopNative === true;
    const articleRequestId = this.data.articleRequestId + 1;
    this.setData({ refreshing: true, articleRequestId, loading: false });
    try {
      await this.loadCategories();
      await this.loadArticles(true, articleRequestId, this.data.activeCategoryId);
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

  retry() {
    const articleRequestId = this.data.articleRequestId + 1;
    this.setData({ articleRequestId, loading: false });
    this.loadArticles(true, articleRequestId, this.data.activeCategoryId);
  },

  loadMore() {
    if (this.data.hasMore) {
      this.loadArticles(false);
    }
  },

  openDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
    });
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
