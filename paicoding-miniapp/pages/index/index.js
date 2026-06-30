const { request } = require('../../utils/request');
const auth = require('../../utils/auth');

Page({
  data: {
    categories: [],
    activeCategoryId: "0",
    articles: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    articleRequestId: 0,
    refreshing: false,
    error: '',
    activeBarLeft: 20,
    categoryRowWidth: 375,
    categoryStickyHeight: 64,
    categoryOffsetTop: 0,
    categoryStuck: false,
    canRefresh: true  // 控制是否可以下拉刷新
  },

  async onLoad() {
    try {
      this.initCategoryStickyLayout();
      await auth.ensureLogin();
      auth.promptProfileIfNeeded();
      await this.loadCategories();
      await this.loadArticles(true);
      // 延迟更新 active-bar 位置，确保 DOM 已渲染
      setTimeout(() => {
        this.updateCategoryStickyOffset();
        this.observeCategorySticky();
        this.updateActiveBar();
      }, 100);
    } catch (err) {
      this.setData({ error: err.message || '加载失败' });
    }
  },

  initCategoryStickyLayout() {
    let menuButtonRect = null;
    let systemInfo = {};

    try {
      systemInfo = wx.getSystemInfoSync ? wx.getSystemInfoSync() : {};
      menuButtonRect = wx.getMenuButtonBoundingClientRect ? wx.getMenuButtonBoundingClientRect() : null;
    } catch (err) {
      menuButtonRect = null;
    }

    const windowWidth = systemInfo.windowWidth || 375;
    const menuBottom = menuButtonRect?.bottom || ((systemInfo.statusBarHeight || 20) + 44);
    const safeRight = menuButtonRect?.left ? (windowWidth - menuButtonRect.left + 12) : 0;

    this.setData({
      categoryRowWidth: Math.ceil(windowWidth - safeRight),
      categoryStickyHeight: Math.ceil(menuBottom + 12),
    });
  },

  onPageScroll(e) {
    const canRefresh = e.scrollTop <= 0;
    const categoryStuck = this.data.categoryOffsetTop > 0 && e.scrollTop >= this.data.categoryOffsetTop;
    const nextData = {};

    if (this.data.canRefresh !== canRefresh) {
      nextData.canRefresh = canRefresh;
    }
    if (this.data.categoryStuck !== categoryStuck) {
      nextData.categoryStuck = categoryStuck;
    }
    if (Object.keys(nextData).length > 0) {
      this.setData(nextData);
    }
  },

  async loadCategories() {
    try {
      const categories = await request({ url: '/mini/api/categories' });
      this.setData({ categories });
      if (categories?.length > 0)
        this.setData({
          activeCategoryId: categories[0].categoryId
        })
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
    console.log(e)
    const id = e.currentTarget.dataset.id;
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
    }, () => {
      this.updateActiveBar();
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

  updateActiveBar() {
    const query = wx.createSelectorQuery();
    query.select('.category-track').boundingClientRect();
    query.select(`#category-${this.data.activeCategoryId}`).boundingClientRect();
    query.exec((res) => {
      console.log(res);
      const trackRect = res?.[0];
      const pillRect = res?.[1];
      if (trackRect && pillRect) {
        const barLeft = pillRect.left - trackRect.left + ((pillRect.width / 2) - 8);
        this.setData({
          activeBarLeft: barLeft
        });
      }
    });
  },

  updateCategoryStickyOffset() {
    const query = wx.createSelectorQuery();
    query.select('.category-sticky').boundingClientRect();
    query.selectViewport().scrollOffset();
    query.exec((res) => {
      const rect = res?.[0];
      const scroll = res?.[1];
      if (!rect) return;

      this.setData({
        categoryOffsetTop: Math.max(0, Math.round(rect.top + (scroll?.scrollTop || 0)))
      });
    });
  },

  observeCategorySticky() {
    if (this.categoryStickyObserver) {
      this.categoryStickyObserver.disconnect();
    }

    this.categoryStickyObserver = this.createIntersectionObserver();
    this.categoryStickyObserver.relativeToViewport().observe('.category-sticky-sentinel', (res) => {
      const categoryStuck = res.boundingClientRect.top <= 0;
      if (this.data.categoryStuck !== categoryStuck) {
        this.setData({ categoryStuck });
      }
    });
  },

  onUnload() {
    if (this.categoryStickyObserver) {
      this.categoryStickyObserver.disconnect();
      this.categoryStickyObserver = null;
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
