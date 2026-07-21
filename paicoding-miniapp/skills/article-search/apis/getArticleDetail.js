const { request } = require('../utils/request');

function normalizeArticleId(value) {
  const number = Number(value);
  if (!isFinite(number) || number < 1) {
    return null;
  }
  return Math.floor(number);
}

module.exports = async function getArticleDetail(args) {
  console.info('[ai-mode] getArticleDetail enter');
  const articleId = normalizeArticleId(args && args.articleId);
  if (!articleId) {
    console.warn('[ai-mode] getArticleDetail invalid args articleId=empty');
    throw new Error('articleId is required');
  }
  console.info(`[ai-mode] getArticleDetail args articleId=${articleId}`);
  try {
    console.info(`[ai-mode] getArticleDetail request start path=/mini/api/articles/${articleId}`);
    const article = await request({
      url: `/mini/api/articles/${articleId}`
    });
    const detail = {
      articleId: article.articleId,
      title: article.title,
      summary: article.summary || '',
      authorName: article.authorName || '',
      readCount: article.readCount || 0,
      praiseCount: article.praiseCount || 0,
      collectionCount: article.collectionCount || 0,
      commentCount: article.commentCount || 0,
      canRead: article.canRead !== false,
      tags: article.tags || [],
      cover: article.cover || ''
    };
    console.info(`[ai-mode] getArticleDetail request success articleId=${detail.articleId} canRead=${detail.canRead}`);
    console.info('[ai-mode] getArticleDetail exit');
    return {
      content: [
        {
          type: 'text',
          text: `已获取文章《${detail.title}》的详情摘要。请展示文章卡片；如果用户要阅读全文，引导用户打开小程序文章详情页，不要代替用户执行点赞、收藏或评论。`
        }
      ],
      structuredContent: {
        article: detail
      }
    };
  } catch (e) {
    console.warn('[ai-mode] getArticleDetail catch', e && e.message ? e.message : e);
    throw e;
  }
};
