const { request } = require('../../utils/request');
const auth = require('../../utils/auth');

Page({
  data: {
    id: null,
    fromAiSkill: false,
    article: null,
    currentUserId: null,
    sharePath: '',
    showBackTop: false,
    loading: false,
    comments: [],
    commentsHasMore: false,
    commentPage: 1,
    commentsLoading: false,
    commentSubmitting: false,
    commentDeletingId: null,
    commentPraisingId: null,
    replyLoadingTopId: null,
    commentDraft: '',
    commentReplyTarget: null,
    commentError: '',
    praiseSubmitting: false,
    collectSubmitting: false,
    error: ''
  },

  async onLoad(options = {}) {
    const articleId = this.resolveArticleId(options);
    if (!articleId) {
      this.setData({ error: '文章不存在' });
      return;
    }
    const fromAiSkill = options.from === 'ai-skill';
    this.setData({ id: articleId, fromAiSkill, currentUserId: this.getCurrentUserId() });
    try {
      if (!fromAiSkill) {
        const user = await auth.ensureLogin();
        this.setData({ currentUserId: user && user.userId });
      }
      await this.loadDetail();
      await this.loadComments(true);
    } catch (err) {
      this.setData({ error: err.message || '加载失败' });
    }
  },

  resolveArticleId(options = {}) {
    const direct = this.toPositiveId(options.id || options.articleId);
    if (direct) {
      return direct;
    }
    const scene = this.safeDecode(options.scene || '');
    if (!scene) {
      return '';
    }
    const sceneDirect = this.toPositiveId(scene);
    if (sceneDirect) {
      return sceneDirect;
    }
    const pairs = scene.split(/[&;]/);
    for (let i = 0; i < pairs.length; i += 1) {
      const parts = pairs[i].split('=');
      const key = String(parts[0] || '').trim();
      const value = parts.slice(1).join('=');
      if (key === 'id' || key === 'articleId' || key === 'a') {
        const parsed = this.toPositiveId(value);
        if (parsed) {
          return parsed;
        }
      }
    }
    return '';
  },

  safeDecode(value) {
    const text = String(value || '').trim();
    if (!text) {
      return '';
    }
    try {
      return decodeURIComponent(text);
    } catch (err) {
      return text;
    }
  },

  toPositiveId(value) {
    const text = String(value || '').trim();
    if (!/^\d+$/.test(text)) {
      return '';
    }
    return Number(text) > 0 ? text : '';
  },

  async loadDetail(showLoading = true) {
    if (showLoading) {
      this.setData({ loading: true, error: '' });
    } else {
      this.setData({ error: '' });
    }
    try {
      const article = this.normalizeArticle(await request({ url: `/mini/api/articles/${this.data.id}` }));
      this.setData({
        article,
        sharePath: `/pages/detail/detail?id=${article.articleId || this.data.id}`
      });
    } catch (err) {
      this.setData({ error: err.message || '加载失败' });
    } finally {
      if (showLoading) {
        this.setData({ loading: false });
      }
    }
  },

  async loadComments(reset = false) {
    if (!this.data.id || this.data.commentsLoading) {
      return;
    }
    const page = reset ? 1 : Number(this.data.commentPage || 1) + 1;
    this.setData({ commentsLoading: true, commentError: '' });
    try {
      const res = await request({
        url: `/mini/api/articles/${this.data.id}/comments`,
        data: { page, size: 10 }
      });
      const list = this.normalizeComments(res && res.list);
      this.setData({
        comments: reset ? list : this.data.comments.concat(list),
        commentsHasMore: !!(res && res.hasMore),
        commentPage: page
      });
    } catch (err) {
      this.setData({ commentError: err.message || '评论加载失败' });
    } finally {
      this.setData({ commentsLoading: false });
    }
  },

  loadMoreComments() {
    if (!this.data.commentsHasMore || this.data.commentsLoading) {
      return;
    }
    return this.loadComments(false);
  },

  stopNativePullDownRefresh() {
    if (wx.stopPullDownRefresh) {
      wx.stopPullDownRefresh();
    }
  },

  async onPullDownRefresh() {
    try {
      await this.loadDetail(false);
      await this.loadComments(true);
    } finally {
      this.stopNativePullDownRefresh();
    }
  },

  async loadMoreReplies(e) {
    const article = this.data.article;
    const topCommentId = Number(e.currentTarget.dataset.top || 0);
    if (!article || !topCommentId || this.data.replyLoadingTopId) {
      return;
    }
    const target = this.data.comments.find((item) => Number(item.commentId || 0) === topCommentId);
    if (!target || !target.hasMoreChild) {
      return;
    }
    const page = Number(target.childPage || 1) + 1;
    this.setData({ replyLoadingTopId: topCommentId, commentError: '' });
    try {
      const res = await request({
        url: `/mini/api/articles/${article.articleId}/comments/${topCommentId}/children`,
        data: { page, size: 10 }
      });
      const children = this.normalizeComments(res && res.list);
      const comments = this.data.comments.map((item) => {
        if (Number(item.commentId || 0) !== topCommentId) {
          return item;
        }
        return Object.assign({}, item, {
          childComments: item.childComments.concat(children),
          hasMoreChild: !!(res && res.hasMore),
          childPage: page
        });
      });
      this.setData({ comments });
    } catch (err) {
      this.setData({ commentError: err.message || '回复加载失败' });
    } finally {
      this.setData({ replyLoadingTopId: null });
    }
  },

  onCommentInput(e) {
    this.setData({ commentDraft: e.detail.value });
  },

  startReply(e) {
    const data = (e && e.currentTarget && e.currentTarget.dataset) || {};
    const parentCommentId = Number(data.id || 0);
    const topCommentId = Number(data.top || data.id || 0);
    if (!parentCommentId || !topCommentId) {
      return;
    }
    this.setData({
      commentReplyTarget: {
        parentCommentId,
        topCommentId,
        userName: data.name || '技术派用户'
      }
    });
  },

  cancelReply() {
    this.setData({ commentReplyTarget: null });
  },

  async submitComment() {
    const article = this.data.article;
    const content = String(this.data.commentDraft || '').trim();
    if (!article || this.data.commentSubmitting) return;
    if (!content) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }
    if (content.length > 1000) {
      wx.showToast({ title: '评论最多 1000 字', icon: 'none' });
      return;
    }
    this.setData({ commentSubmitting: true });
    try {
      const payload = { commentContent: content };
      if (this.data.commentReplyTarget) {
        payload.parentCommentId = this.data.commentReplyTarget.parentCommentId;
        payload.topCommentId = this.data.commentReplyTarget.topCommentId;
      }
      const res = await auth.requestWithLogin({
        url: `/mini/api/articles/${article.articleId}/comments`,
        method: 'POST',
        data: payload
      });
      const currentUserId = this.getCurrentUserId();
      const list = this.normalizeComments(res && res.list, currentUserId);
      this.setData({
        currentUserId,
        comments: list,
        commentsHasMore: !!(res && res.hasMore),
        commentPage: 1,
        commentDraft: '',
        commentReplyTarget: null,
        'article.commentCount': Number(article.commentCount || 0) + 1
      });
      wx.showToast({ title: '评论已发布', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: err.message || '发布失败', icon: 'none' });
    } finally {
      this.setData({ commentSubmitting: false });
    }
  },

  async deleteComment(e) {
    const article = this.data.article;
    const commentId = Number(e.currentTarget.dataset.id || 0);
    if (!article || !commentId || this.data.commentDeletingId) return;
    this.setData({ commentDeletingId: commentId });
    try {
      const res = await auth.requestWithLogin({
        url: `/mini/api/articles/${article.articleId}/comments/${commentId}/delete`,
        method: 'POST'
      });
      const currentUserId = this.getCurrentUserId();
      const list = this.normalizeComments(res && res.list, currentUserId);
      this.setData({
        currentUserId,
        comments: list,
        commentsHasMore: !!(res && res.hasMore),
        commentPage: 1,
        'article.commentCount': Math.max(0, Number(article.commentCount || 0) - 1)
      });
      wx.showToast({ title: '评论已删除', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: err.message || '删除失败', icon: 'none' });
    } finally {
      this.setData({ commentDeletingId: null });
    }
  },

  async toggleCommentPraise(e) {
    const article = this.data.article;
    const commentId = Number(e.currentTarget.dataset.id || 0);
    const praised = e.currentTarget.dataset.praised === true || e.currentTarget.dataset.praised === 'true';
    if (!article || !commentId || this.data.commentPraisingId) return;
    this.setData({ commentPraisingId: commentId });
    try {
      const res = await auth.requestWithLogin({
        url: `/mini/api/articles/${article.articleId}/comments/${commentId}/favor?type=${praised ? 4 : 2}`,
        method: 'POST'
      });
      const currentUserId = this.getCurrentUserId();
      const list = this.normalizeComments(res && res.list, currentUserId);
      this.setData({
        currentUserId,
        comments: list,
        commentsHasMore: !!(res && res.hasMore),
        commentPage: 1
      });
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ commentPraisingId: null });
    }
  },

  async togglePraise() {
    const article = this.data.article;
    if (!article || this.data.praiseSubmitting) return;
    const nextPraised = !article.praised;
    const nextCount = Math.max(0, Number(article.praiseCount || 0) + (nextPraised ? 1 : -1));
    this.setData({ praiseSubmitting: true });
    try {
      await auth.requestWithLogin({
        url: `/mini/api/articles/${article.articleId}/favor?type=${nextPraised ? 2 : 4}`,
        method: 'POST'
      });
      this.setData({
        'article.praised': nextPraised,
        'article.praiseCount': nextCount
      });
      await this.loadDetail(false);
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ praiseSubmitting: false });
    }
  },

  async toggleCollect() {
    const article = this.data.article;
    if (!article || this.data.collectSubmitting) return;
    const nextCollected = !article.collected;
    const nextCount = Math.max(0, Number(article.collectionCount || 0) + (nextCollected ? 1 : -1));
    this.setData({ collectSubmitting: true });
    try {
      await auth.requestWithLogin({
        url: `/mini/api/articles/${article.articleId}/favor?type=${nextCollected ? 3 : 5}`,
        method: 'POST'
      });
      this.setData({
        'article.collected': nextCollected,
        'article.collectionCount': nextCount
      });
      await this.loadDetail(false);
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    } finally {
      this.setData({ collectSubmitting: false });
    }
  },

  normalizeArticle(article) {
    const value = article || {};
    const sourceUrl = value.sourceUrl || '';
    value.imageUrls = this.extractImageUrls(value.contentHtml);
    value.authorInitial = (value.authorName || '派').slice(0, 1);
    value.readCount = Number(value.readCount || 0);
    value.praiseCount = Number(value.praiseCount || 0);
    value.collectionCount = Number(value.collectionCount || 0);
    value.commentCount = Number(value.commentCount || 0);
    value.sourceHost = this.extractHost(sourceUrl);
    value.canRead = value.canRead !== false;
    return value;
  },

  getCurrentUserId() {
    const user = auth.getStoredUser();
    return user && user.userId ? Number(user.userId) : null;
  },

  normalizeComments(comments, currentUserId) {
    const userId = currentUserId == null ? this.data.currentUserId : currentUserId;
    return Array.isArray(comments) ? comments.map((item) => this.normalizeComment(item, userId)) : [];
  },

  normalizeComment(comment, currentUserId) {
    const value = comment || {};
    const userId = currentUserId == null ? this.data.currentUserId : currentUserId;
    value.userInitial = (value.userName || '派').slice(0, 1);
    value.praiseCount = Number(value.praiseCount || 0);
    value.childCommentCount = Number(value.childCommentCount || 0);
    value.hasMoreChild = Boolean(value.hasMoreChild);
    value.childPage = Number(value.childPage || 1);
    value.canDelete = Boolean(userId && Number(value.userId || 0) === Number(userId));
    value.childComments = Array.isArray(value.childComments)
      ? value.childComments.map((child) => {
        const item = child || {};
        item.userInitial = (item.userName || '派').slice(0, 1);
        item.praiseCount = Number(item.praiseCount || 0);
        item.canDelete = Boolean(userId && Number(item.userId || 0) === Number(userId));
        return item;
      })
      : [];
    return value;
  },

  extractHost(url) {
    const matched = String(url || '').match(/^https?:\/\/([^/]+)/i);
    return matched ? matched[1] : '';
  },

  extractImageUrls(html) {
    const text = String(html || '');
    const urls = [];
    const seen = {};
    const imgReg = /<img\b[^>]*\bsrc\s*=\s*["']([^"']+)["'][^>]*>/ig;
    let match;
    while ((match = imgReg.exec(text))) {
      const url = String(match[1] || '').trim();
      if (!/^https?:\/\//i.test(url) || seen[url]) {
        continue;
      }
      seen[url] = true;
      urls.push(url);
    }
    return urls.slice(0, 30);
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    const urls = this.data.article && this.data.article.imageUrls;
    if (!url || !Array.isArray(urls) || urls.length === 0) {
      return;
    }
    wx.previewImage({
      current: url,
      urls
    });
  },

  copySourceUrl() {
    const sourceUrl = this.data.article && this.data.article.sourceUrl;
    if (!sourceUrl) return;
    wx.setClipboardData({
      data: sourceUrl,
      success: () => wx.showToast({ title: '已复制原文链接', icon: 'success' })
    });
  },

  goHome() {
    wx.switchTab({
      url: '/pages/index/index',
      fail: () => wx.reLaunch({ url: '/pages/index/index' })
    });
  },

  backTop() {
    wx.pageScrollTo({
      scrollTop: 0,
      duration: 220
    });
  },

  onPageScroll(e) {
    const next = Number(e && e.scrollTop) > 640;
    if (next !== this.data.showBackTop) {
      this.setData({ showBackTop: next });
    }
  },

  onShareAppMessage() {
    const article = this.data.article || {};
    return {
      title: article.shortTitle || article.title || '技术派文章',
      path: this.data.sharePath || `/pages/detail/detail?id=${this.data.id}`
    };
  },

  onShareTimeline() {
    const article = this.data.article || {};
    return {
      title: article.shortTitle || article.title || '技术派文章',
      query: `id=${article.articleId || this.data.id}`
    };
  }
});
