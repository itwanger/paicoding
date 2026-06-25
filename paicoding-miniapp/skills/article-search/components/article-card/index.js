Component({
  properties: {
    article: {
      type: Object,
      value: {}
    },
    articles: {
      type: Array,
      value: []
    }
  },
  data: {
    article: {},
    articles: [],
    emptyText: '暂无文章'
  },

  lifetimes: {
    created() {
      console.info('[ai-mode] article-card created');
      this.bindModelContext();
    },

    attached() {
      console.log('[ai-mode] article-card attached');
    }
  },

  methods: {
    bindModelContext() {
      if (!wx.modelContext || !wx.modelContext.getContext || !wx.modelContext.getViewContext) {
        return;
      }
      const { NotificationType } = wx.modelContext;
      if (!NotificationType) {
        return;
      }
      try {
        const modelCtx = wx.modelContext.getContext(this);
        if (modelCtx && modelCtx.on && NotificationType.Result) {
          modelCtx.on(NotificationType.Result, (data) => {
            const result = data && data.result && data.result.structuredContent
              ? data.result.structuredContent
              : data;
            console.info('[ai-mode] article-card result received', result);
            this.applyRenderData(result || {});
          });
        }

        const viewCtx = wx.modelContext.getViewContext(this);
        if (viewCtx && viewCtx.getDimensions) {
          const dimensions = viewCtx.getDimensions();
          console.info(
            `[ai-mode] article-card dimensions width=${dimensions.width} minHeight=${dimensions.minHeight} maxHeight=${dimensions.maxHeight}`
          );
        }
        if (viewCtx && viewCtx.on && NotificationType.Overflow) {
          viewCtx.on(NotificationType.Overflow, (data) => {
            const overflowed = Boolean(data && data.overflowHeight > 0);
            console.info(`[ai-mode] article-card overflow overflowed=${overflowed} data=${JSON.stringify(data)}`);
          });
          console.info('[ai-mode] article-card overflow monitor=on');
        }
      } catch (e) {
        console.warn('[ai-mode] article-card model context unavailable', e && e.message ? e.message : e);
      }
    },

    applyRenderData(data) {
      if (Array.isArray(data.articles)) {
        this.setData({
          articles: data.articles,
          article: {},
          emptyText: data.articles.length > 0 ? '' : '暂无匹配文章'
        });
        return;
      }
      if (data.article) {
        this.setData({
          articles: [],
          article: data.article,
          emptyText: ''
        });
      }
    },

    openDetail(e) {
      const articleId = e.currentTarget.dataset.id || this.data.article.articleId;
      if (!articleId) {
        return;
      }
      const url = `/pages/detail/detail?id=${articleId}&from=ai-skill`;
      if (wx.modelContext && wx.modelContext.getViewContext) {
        try {
          const viewCtx = wx.modelContext.getViewContext(this);
          if (viewCtx && viewCtx.openDetailPage) {
            console.info(`[ai-mode] article-card open detail page articleId=${articleId}`);
            viewCtx.openDetailPage({ url });
            return;
          }
        } catch (e) {
          console.warn('[ai-mode] article-card openDetailPage unavailable', e && e.message ? e.message : e);
        }
      }
      wx.navigateTo({
        url
      });
    }
  }
});
