const { request } = require('../../utils/request');
const auth = require('../../utils/auth');

const MAX_SEARCH_KEY_LENGTH = 64;
const SEARCH_HISTORY_KEY = 'PAICODING_SEARCH_HISTORY';
const MAX_SEARCH_HISTORY_SIZE = 8;

Page({
  data: {
    key: '',
    hints: [],
    history: [],
    articles: [],
    page: 1,
    size: 10,
    hasMore: false,
    loading: false,
    searched: false,
    hintTimer: null,
    hintRequestId: 0,
    searchRequestId: 0,
    refreshing: false,
    error: ''
  },

  async onLoad(options = {}) {
    try {
      await auth.ensureLogin();
      this.loadHistory();
      if (options.key) {
        const key = this.normalizeKey(decodeURIComponent(options.key));
        this.setData({ key, searched: Boolean(key) });
        if (!key) {
          return;
        }
        this.saveHistory(key);
        await this.loadMore(true);
      }
    } catch (err) {
      this.setData({ error: err.message || '登录失败' });
    }
  },

  onUnload() {
    if (this.data.hintTimer) {
      clearTimeout(this.data.hintTimer);
    }
    this.setData({
      hintTimer: null,
      hintRequestId: this.data.hintRequestId + 1,
      searchRequestId: this.data.searchRequestId + 1
    });
  },

  async onInput(e) {
    const key = this.normalizeKey(e.detail.value);
    if (this.data.hintTimer) {
      clearTimeout(this.data.hintTimer);
    }
    const hintRequestId = this.data.hintRequestId + 1;
    this.setData({ key, error: '', hintTimer: null, hintRequestId });
    if (!key) {
      this.setData({ hints: [] });
      return;
    }
    const hintTimer = setTimeout(async () => {
      try {
        const hints = await request({ url: '/mini/api/search/hint', data: { key } });
        if (this.data.key === key && this.data.hintRequestId === hintRequestId) {
          this.setData({ hints, hintTimer: null });
        }
      } catch (err) {
        if (this.data.hintRequestId === hintRequestId) {
          this.setData({ hints: [], hintTimer: null });
        }
      }
    }, 250);
    this.setData({ hintTimer });
  },

  async doSearch() {
    const key = this.normalizeKey(this.data.key);
    if (!key) return;
    this.saveHistory(key);
    if (this.data.hintTimer) {
      clearTimeout(this.data.hintTimer);
    }
    const hintRequestId = this.data.hintRequestId + 1;
    const searchRequestId = this.data.searchRequestId + 1;
    if (key !== this.data.key) {
      this.setData({ key });
    }
    this.setData({
      page: 1,
      articles: [],
      searched: true,
      hints: [],
      hintTimer: null,
      hintRequestId,
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
      const list = Array.isArray(result.list) ? result.list : [];
      this.setData({
        articles: reset ? list : this.data.articles.concat(list),
        hasMore: Boolean(result.hasMore),
        page: page + 1
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
    if (this.data.hintTimer) {
      clearTimeout(this.data.hintTimer);
    }
    this.setData({
      key: '',
      hints: [],
      articles: [],
      page: 1,
      hasMore: false,
      searched: false,
      hintTimer: null,
      hintRequestId: this.data.hintRequestId + 1,
      searchRequestId: this.data.searchRequestId + 1,
      loading: false,
      error: ''
    });
  },

  chooseHint(e) {
    const articleId = e.currentTarget.dataset.id;
    if (articleId) {
      this.setData({ hints: [] });
      wx.navigateTo({
        url: `/pages/detail/detail?id=${articleId}`
      });
      return;
    }
    const title = this.normalizeKey(e.currentTarget.dataset.title);
    this.setData({ key: title, hints: [] });
    return this.doSearch();
  },

  openDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
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
