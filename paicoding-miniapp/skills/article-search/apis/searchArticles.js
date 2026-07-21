const { request } = require('../utils/request');

function normalizePositiveInteger(value, fallback, max) {
  const number = Number(value);
  if (!isFinite(number) || number < 1) {
    return fallback;
  }
  const integer = Math.floor(number);
  return max ? Math.min(integer, max) : integer;
}

module.exports = async function searchArticles(args) {
  console.info('[ai-mode] searchArticles enter');
  const keyword = args && args.keyword ? String(args.keyword).trim() : '';
  if (!keyword) {
    console.warn('[ai-mode] searchArticles invalid args keyword=empty');
    throw new Error('keyword is required');
  }
  if (keyword.length > 64) {
    console.warn('[ai-mode] searchArticles invalid args keyword=too-long');
    throw new Error('keyword length must be <= 64');
  }
  const page = normalizePositiveInteger(args && args.page, 1);
  const size = normalizePositiveInteger(args && args.size, 5, 10);
  console.info(`[ai-mode] searchArticles args keyword=${keyword} page=${page} size=${size}`);
  try {
    console.info('[ai-mode] searchArticles request start path=/mini/api/search');
    const result = await request({
      url: '/mini/api/search',
      data: { key: keyword, page, size }
    });
    const articles = (result.list || []).map((item) => ({
      articleId: item.articleId,
      title: item.shortTitle || item.title,
      summary: item.searchHit || item.summary || '',
      authorName: item.authorName || '',
      readCount: item.readCount || 0,
      praiseCount: item.praiseCount || 0,
      collectionCount: item.collectionCount || 0,
      commentCount: item.commentCount || 0,
      cover: item.cover || ''
    }));
    console.info(`[ai-mode] searchArticles request success count=${articles.length} hasMore=${Boolean(result.hasMore)}`);
    const text = articles.length > 0
      ? `已找到 ${articles.length} 篇与“${keyword}”相关的技术派文章。请优先展示文章卡片，并询问用户是否要查看某篇文章详情。`
      : `未找到与“${keyword}”相关的技术派文章。请告知用户没有匹配结果，并引导用户换一个更具体的 Java、Spring、面试或 AI 关键词；不要用同一个关键词反复调用本接口。`;
    console.info('[ai-mode] searchArticles exit');
    return {
      content: [
        {
          type: 'text',
          text
        }
      ],
      structuredContent: {
        articles: articles,
        hasMore: Boolean(result.hasMore)
      }
    };
  } catch (e) {
    console.warn('[ai-mode] searchArticles catch', e && e.message ? e.message : e);
    throw e;
  }
};
