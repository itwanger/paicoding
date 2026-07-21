Component({
  options: {
    virtualHost: true,
  },

  properties: {
    article: {
      type: Object,
      value: {},
    },
  },

  methods: {
    openDetail() {
      const articleId = this.data.article && this.data.article.articleId
      if (!articleId) {
        return
      }
      wx.navigateTo({
        url: `/pages/detail/detail?id=${articleId}`,
      })
    },
  },
})
