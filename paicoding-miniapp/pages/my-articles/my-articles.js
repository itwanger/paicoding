const auth = require('../../utils/auth')

const ONLINE_STATUS = 1
const REVIEW_STATUS = 2

function createListState() {
  return {
    articles: [],
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
    activeTab: 'article',
    article: createListState(),
    draft: createListState(),
  },

  async onLoad(options = {}) {
    const activeTab = options.tab === 'draft' ? 'draft' : 'article'
    this.setData({ activeTab })
    await this.loadArticles(activeTab, true)
  },

  async switchTab(event) {
    const type = event.detail.value
    if (type !== 'article' && type !== 'draft') {
      return
    }
    this.setData({ activeTab: type })
    if (!this.data[type].loaded) {
      await this.loadArticles(type, true)
    }
  },

  async loadArticles(type, reset = false) {
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
        url: '/mini/api/user/articles',
        data: {
          type,
          page,
          size: state.size,
        },
      })
      const articles = this.normalizeArticles(result && result.list)
      this.setData({
        [`${type}.articles`]: reset ? articles : state.articles.concat(articles),
        [`${type}.hasMore`]: Boolean(result && result.hasMore),
        [`${type}.page`]: page + 1,
        [`${type}.loaded`]: true,
      })
    }
    catch (err) {
      this.setData({ [`${type}.error`]: err.message || '文章列表加载失败' })
    }
    finally {
      this.setData({
        [`${type}.loading`]: false,
        [`${type}.refreshing`]: false,
      })
    }
  },

  normalizeArticles(articles) {
    return Array.isArray(articles)
      ? articles.map((article) => {
          const status = Number(article.status)
          return Object.assign({}, article, {
            status,
            statusText: article.statusText || (status === REVIEW_STATUS ? '审核中' : (status === ONLINE_STATUS ? '已发布' : '草稿')),
            isOnline: status === ONLINE_STATUS,
            isReview: status === REVIEW_STATUS,
            displayTime: article.lastUpdateTime || article.createTime || '',
          })
        })
      : []
  },

  refresh(event) {
    const type = event.currentTarget.dataset.type
    if (!this.data[type] || this.data[type].loading) {
      return
    }
    this.setData({ [`${type}.refreshing`]: true })
    return this.loadArticles(type, true)
  },

  loadMore(event) {
    return this.loadArticles(event.currentTarget.dataset.type, false)
  },

  retry(event) {
    return this.loadArticles(event.currentTarget.dataset.type, true)
  },

  openArticle(event) {
    const { id, status } = event.currentTarget.dataset
    const articleId = Number(id)
    if (!articleId) {
      return
    }
    if (Number(status) === ONLINE_STATUS || Number(status) === REVIEW_STATUS) {
      wx.navigateTo({ url: `/pages/detail/detail?id=${articleId}` })
    }
  },

  async publishDraft(event) {
    const articleId = Number(event.currentTarget.dataset.id)
    if (!articleId || this.isPublishing(articleId)) {
      return
    }
    this.setDraftPublishing(articleId, true)
    try {
      const result = await auth.requestWithLogin({
        url: `/mini/api/user/articles/${articleId}/publish`,
        method: 'POST',
      })
      wx.showToast({
        title: result && Number(result.status) === REVIEW_STATUS ? '已提交审核' : '已发布',
        icon: 'success',
      })
      await this.loadArticles('draft', true)
      if (this.data.article.loaded) {
        await this.loadArticles('article', true)
      }
    }
    catch (err) {
      wx.showToast({ title: err.message || '发布失败', icon: 'none' })
    }
    finally {
      this.setDraftPublishing(articleId, false)
    }
  },

  isPublishing(articleId) {
    const target = this.data.draft.articles.find(article => Number(article.articleId) === articleId)
    return Boolean(target && target.publishing)
  },

  setDraftPublishing(articleId, publishing) {
    this.setData({
      'draft.articles': this.data.draft.articles.map(article => Number(article.articleId) === articleId
        ? Object.assign({}, article, { publishing })
        : article),
    })
  },
})
