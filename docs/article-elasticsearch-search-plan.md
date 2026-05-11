# 文章全文搜索 Elasticsearch 接入计划

## 目标

- admin 文章管理支持按正文全文检索，解决只能按标题、作者等字段筛选的问题。
- 用户端首页搜索接口继续复用现有 `/search/api/hint` 和 `/search/api/list`，在 ES 开启后自动走全文检索。
- 和 PaiSmart 共用同一套 Elasticsearch 服务时，通过独立索引、轻量 mapping、分页限制和失败回退降低互相影响。

## 索引设计

- 索引名：`paicoding_article_v1`
- 文档 ID：文章 ID
- 主要字段：
  - `articleId`
  - `title`
  - `shortTitle`
  - `summary`
  - `content`
  - `authorId`
  - `authorName`
  - `status`
  - `deleted`
  - `columnIds`
  - `updateTime`

正文和标题使用 IK 分词，标题权重大于摘要，摘要权重大于正文。当前不引入向量字段，避免和 PaiSmart RAG 的 embedding / dense_vector 资源竞争。

## 查询路径

1. 用户端搜索建议 `/search/api/hint`
   - ES 开启：只查 `title`、`shortTitle`。
   - ES 未开启或异常：回退原 MySQL 标题模糊查询。

2. 用户端搜索列表 `/search/api/list`
   - ES 开启：查 `title`、`shortTitle`、`summary`、`content`，过滤已发布且未删除文章。
   - ES 未开启或异常：回退原 MySQL 标题、短标题、摘要模糊查询。

3. admin 文章列表 `/admin/article/list`
   - 新增 `keyword` 参数。
   - `keyword` 非空且 ES 开启：走 ES 全文检索，同时保留状态、推荐、置顶、专栏、作者、标题等筛选条件。
   - ES 未开启或异常：回退 MySQL，`keyword` 退化为标题搜索，保证页面可用。

## 索引同步

- 新增文章、更新文章、状态变更：监听 `ArticleMsgEvent` 后异步 upsert 文档。
- 删除文章：监听删除事件后删除 ES 文档。
- 历史数据：提供 admin 重建接口 `/admin/article/search/rebuild`，按 ID 批量扫描 MySQL 后写入 ES。

## 服务压力控制

- Paicoding 使用独立索引 `paicoding_article_v1`，不复用 PaiSmart 的知识库索引。
- 不写入向量字段，不调用 embedding 服务，只做普通 BM25 文本检索。
- 搜索接口沿用现有分页大小，默认只取当前页。
- 批量重建按小批次滚动写入，避免一次性大 bulk。
- ES 查询失败时降级 MySQL，避免 ES 短时抖动拖垮核心页面。

## 上线步骤

1. 部署代码但保持 `elasticsearch.open=false`，验证 MySQL 搜索不受影响。
2. 配置 ES 连接信息和索引名，开启 `elasticsearch.open=true`。
3. 调用 `/admin/article/search/rebuild` 初始化索引。
4. 验证 admin 关键词搜索、首页搜索建议、首页搜索列表。
5. 观察 ES CPU、heap、search latency；如 PaiSmart 高峰期压力明显，再单独限流或拆独立 ES 节点。
