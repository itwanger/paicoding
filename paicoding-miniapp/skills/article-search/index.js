const searchArticles = require('./apis/searchArticles');
const getArticleDetail = require('./apis/getArticleDetail');

if (wx.modelContext && wx.modelContext.registerAPI) {
  wx.modelContext.registerAPI('searchArticles', searchArticles);
  wx.modelContext.registerAPI('getArticleDetail', getArticleDetail);
  console.log('[ai-mode] article-search skill APIs registered');
}
