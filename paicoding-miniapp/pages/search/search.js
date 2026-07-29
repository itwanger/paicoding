const { request } = require('../../utils/request');

const MAX_SEARCH_KEY_LENGTH = 64;
const SEARCH_HISTORY_KEY = 'PAICODING_SEARCH_HISTORY';
const MAX_SEARCH_HISTORY_SIZE = 12;

Page({
  data: {
    key: '',
    focus: false,
    history: [],
    articles: [],
    page: 1,
    size: 10,
    hasMore: false,
    loading: false,
    firstLoaded: false,
    searched: false,
    searchRequestId: 0,
    refreshing: false,
    error: ''
  },

  async onLoad(options = {}) {
    // 搜索无需登录,匿名用户也可使用(不再自动登录)
    this.loadHistory();
    if (options.key) {
      try {
        const key = this.normalizeKey(decodeURIComponent(options.key));
        this.setData({ key, searched: Boolean(key) });
        if (!key) {
          this.setData({ focus: true });
          return;
        }
        this.saveHistory(key);
        await this.loadMore(true);
      } catch (err) {
        this.setData({ error: err.message || '搜索失败' });
      }
    } else {
      // 从首页点搜索入口进来时自动聚焦,方便用户直接输入
      this.setData({ focus: true });
    }
  },

  onUnload() {
    this.setData({
      searchRequestId: this.data.searchRequestId + 1
    });
  },

  onInputBlur() {
    // t-input 的 focus 为受控属性,失焦后需主动回落,避免页面重渲染时反复弹起键盘
    if (this.data.focus) {
      this.setData({ focus: false });
    }
  },

  onInput(e) {
    // t-input 的 bind:change 在点击自带清空按钮时 e.detail.value 可能为 undefined,这里做兜底
    // 仅同步输入内容,搜索统一由搜索按钮触发(doSearch)
    this.setData({ key: this.normalizeKey(e.detail && e.detail.value), error: '' });
  },

  async doSearch() {
    const key = this.normalizeKey(this.data.key);
    if (!key) {
      // 空关键词:聚焦输入框并轻提示,不发请求
      this.setData({ focus: true });
      wx.showToast({ title: '请输入搜索关键词', icon: 'none', duration: 1200 });
      return;
    }
    this.saveHistory(key);
    // 收起键盘,进入结果浏览
    this.setData({ focus: false });
    const searchRequestId = this.data.searchRequestId + 1;
    if (key !== this.data.key) {
      this.setData({ key });
    }
    this.setData({
      page: 1,
      articles: [],
      searched: true,
      searchRequestId,
      loading: false,
      error: ''
    });
    await this.loadMore(true, searchRequestId, key);
  },

  async loadMore(reset, requestId, requestKey) {
    if (this.data.loading && !reset) return;
    if (!reset && !this.data.hasMore) return;
    const page = reset ? 1 : this.data.page;
    const activeRequestId = requestId || this.data.searchRequestId + 1;
    const activeKey = requestKey || this.data.key;
    if (!requestId) {
      this.setData({ searchRequestId: activeRequestId });
    }
    this.setData({ loading: true, error: '' });
    try {
      const result = await request({
        url: '/mini/api/search',
        data: {
          key: activeKey,
          page,
          size: this.data.size
        }
      });
      if (this.data.searchRequestId !== activeRequestId || this.data.key !== activeKey) {
        return;
      }
      const list = (Array.isArray(result.list) ? result.list : []).map((item) => this.normalizeArticle(item));
      this.setData({
        articles: reset ? list : this.data.articles.concat(list),
        hasMore: Boolean(result.hasMore),
        page: page + 1,
        firstLoaded: true
      });
    } catch (err) {
      if (this.data.searchRequestId === activeRequestId) {
        this.setData({ error: err.message || '搜索失败' });
      }
    } finally {
      if (this.data.searchRequestId === activeRequestId) {
        this.setData({ loading: false, refreshing: false });
      }
    }
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh();
    }
  },

  normalizeKey(value) {
    return String(value || '').trim().slice(0, MAX_SEARCH_KEY_LENGTH);
  },

  // 将搜索结果字段映射为 article-card 组件约定的字段
  // 搜索接口会返回 shortTitle / searchHit 命中高亮字段,优先取用,回退到常规字段
  normalizeArticle(item) {
    if (!item || typeof item !== 'object') {
      return item;
    }
    return {
      ...item,
      title: item.shortTitle || item.title,
      summary: item.searchHit || item.summary,
      tags: Array.isArray(item.tags) ? item.tags : []
    };
  },

  loadHistory() {
    const history = wx.getStorageSync(SEARCH_HISTORY_KEY);
    this.setData({ history: Array.isArray(history) ? history.slice(0, MAX_SEARCH_HISTORY_SIZE) : [] });
  },

  saveHistory(key) {
    const value = this.normalizeKey(key);
    if (!value) return;
    const history = Array.isArray(this.data.history) ? this.data.history : [];
    const next = [value].concat(history.filter((item) => item !== value)).slice(0, MAX_SEARCH_HISTORY_SIZE);
    wx.setStorageSync(SEARCH_HISTORY_KEY, next);
    this.setData({ history: next });
  },

  clearHistory() {
    wx.removeStorageSync(SEARCH_HISTORY_KEY);
    this.setData({ history: [] });
  },

  chooseHistory(e) {
    const key = this.normalizeKey(e.currentTarget.dataset.key);
    if (!key) return;
    this.setData({ key, hints: [] });
    return this.doSearch();
  },

  retry() {
    this.loadMore(true);
  },

  async refresh(stopNative = false) {
    const shouldStopNative = stopNative === true;
    const key = this.normalizeKey(this.data.key);
    this.loadHistory();
    if (!key || !this.data.searched) {
      this.setData({ refreshing: false });
      if (shouldStopNative) {
        this.stopNativePullDownRefresh();
      }
      return;
    }
    const searchRequestId = this.data.searchRequestId + 1;
    this.setData({
      refreshing: true,
      searchRequestId,
      loading: false,
      error: ''
    });
    try {
      await this.loadMore(true, searchRequestId, key);
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

  clearSearch() {
    this.setData({
      key: '',
      articles: [],
      page: 1,
      hasMore: false,
      searched: false,
      firstLoaded: false,
      focus: true,
      searchRequestId: this.data.searchRequestId + 1,
      loading: false,
      error: ''
    });
  },

  onShareAppMessage() {
    const key = this.normalizeKey(this.data.key);
    return {
      title: key ? `技术派搜索：${key}` : '技术派文章搜索',
      path: key ? `/pages/search/search?key=${encodeURIComponent(key)}` : '/pages/search/search'
    };
  },

  onShareTimeline() {
    const key = this.normalizeKey(this.data.key);
    return {
      title: key ? `技术派搜索：${key}` : '技术派文章搜索',
      query: key ? `key=${encodeURIComponent(key)}` : ''
    };
  }
});
