# Journal - zidou-kiyn (Part 1)

> AI development session journal
> Started: 2026-08-05

---

## 2026-08-05 Session: bootstrap 初始规范（00-bootstrap-guidelines）

完成静态扫描并产出第一版 specs（backend 8 篇 + 索引），任务 0（local-deploy-smoke）已创建（PRD-only，未激活）。

### 关键决策

1. **MongoDB 不引入**：上游 README 技术栈罗列了 MongoDB，但经全仓核实（所有 pom.xml 与 Java 源码）无任何 Mongo 依赖与代码；计数主链路实为 MySQL `user_foot` 表 + Redis（`CountServiceImpl` 注入 `UserFootDao` + `stringRedisTemplate`）。本地部署与后续开发均不含 Mongo，防 README 误导已记入 `spec/backend/integration-patterns.md`。

2. **fork 基线核实**：`git fetch upstream` 后比对，`main` 与 `upstream/main` 完全一致（ahead 0 / behind 0），**本 fork 零独有提交**。原以为是 fork 改动的 ES 搜索重构（`3ecd043e`、`5933d1cc`）与异常信息防泄露（`d0e25206`）均为上游提交。已打 tag `upstream-base` = `4b55260a`，今后所有增量以此为 diff 基线。

3. **RAG 增量边界收敛**：上游已完成 BM25 关键词检索（IK 分词+字段加权+高亮 snippet）、`ArticleMsgEvent` 异步索引同步、ES 失败/关闭回退 MySQL、`/admin/article/search/rebuild` 批量重建，且设计文档（`docs/article-elasticsearch-search-plan.md`）明确不引入向量字段。路线 2 的增量因此收敛为：向量索引与 Embedding 管道、kNN 检索、LLM 带引用回答；降级与回填直接复用上游模式。

### 其他约定

- MQ 管道（路线 1）：spring-boot-starter-amqp + `ai.` 前缀命名隔离，不动上游原生客户端实现；细则任务 1 PRD 定。
- admin 后台为独立仓库 `itwanger/paicoding-admin`，clone 上游原版到仓外运行，不修改不入库；主仓 CORS 反射式放行，预期无需改代码。
- spec 语言：中文（代码标识符英文）。
