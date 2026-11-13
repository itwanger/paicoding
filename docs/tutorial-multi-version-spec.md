# 技术派教程多版本管理、展示与路由实施规格书

> 文档状态：架构定稿（供编码 Agent 参照施工）
> 适用仓库：主站 `/Users/itwanger/Documents/GitHub/paicoding`、管理后台 `/Users/itwanger/Documents/GitHub/paicoding-admin`
> 关联文档：保留同目录初稿 `docs/tutorial-version-design.md` 作为前期调研参考，实际开发以本规格书为准。

> **2026-11-13 实施修正（优先于下文原评估稿）**
> - 用户明确确认：**地址栏完全不变，页内切版**。不实施下文的 `?v=` 直链、URL 同步和复制历史版链接；刷新仍读默认版。`edition_code` 仅作版本展示编码。
> - 采用单文章身份、正式版公开指针、追加快照、受众引导、评论快照和 admin 编辑器入口。二期 `column_version` 本次不实施。
> - 首次启用只能把当时线上正文命名为“启用时存档”，不能推断为纯净初版，不能伪造历史发布时间。既有评论保持“早期讨论”，人工核实前不迁移归属。
> - **禁止按第 2.5.1 节步骤八清空 `current_edition_id` 回滚**：恢复旧读取逻辑会读取最大 `version`，可能暴露草稿。应切回一个已公开版本；保留所有新快照、评论和版本指针。
> - 本次页内切版复用原 SSR 路径，以请求头携带选择，仅替换惰性解析的正文与评论片段；不新增另一套可绕过权限的正文读取 API。后台详情、草稿接口要求 ADMIN。
> - 实际接口、验收结果、灰度步骤见 [tutorial-version-implementation.md](tutorial-version-implementation.md)。


---

## 1. 背景目标与架构选型结论

### 1.1 业务痛点与 `/react-vs-toolcall` 路由现状

技术派线上教程《ReAct vs ToolCall》（访问地址 `https://paicoding.com/react-vs-toolcall`）近期基于最新仓库源码完成了大版本重构。重构后的新版（v2）贴合最新工程结构与实战代码，而初版（v1）从零推导概念，对零基础读者建立直觉更为友好。当前两版内容堆叠在同一篇正文中，篇幅冗长、新旧代码混杂，既干扰老读者查阅最新实现，又提高了零基础读者的阅读门槛。

经源码核实，`https://paicoding.com/react-vs-toolcall` 并非专栏首页，而是绑定在专栏下的一篇具有自定义根短链（`article.url_slug = 'react-vs-toolcall'`）的教程章节，其服务端请求分发路径如下：

1. `ArticleSlugViewController#article`（`paicoding-web/src/main/java/com/github/paicoding/forum/web/front/article/view/ArticleSlugViewController.java:35-52`）拦截根路径 `/{urlSlug:[a-z0-9][a-z0-9-]*}`。
2. 先调用 `columnViewController.columnByRootSlug(urlSlug, model)` 检查是否命中 `column_info.url_slug`（若命中则 301 跳转至 `/column/{slug}`）；未命中时调用 `articleDao.getByUrlSlug(urlSlug)` 查询文章。
3. 查询 `columnService.getColumnArticleRelation(article.getId())`，检测到该文章属于某专栏章节后，转交 `ColumnViewController#articleByRootArticleSlug`（`paicoding-web/src/main/java/com/github/paicoding/forum/web/front/article/view/ColumnViewController.java:241-253`），在浏览器地址栏保持 `/react-vs-toolcall` 不变的前提下，原地渲染专栏阅读页模板 `views/column-detail/index`。
4. 其余入口如 `/article/detail/{articleId}`、`/article/detail/{articleId}/{urlSlug}`（`ArticleViewController.java:145-223`）以及 `/column/{columnId}/{section}`（`ColumnViewController.java:154-171`），检测到目标专栏章节配置了 `article.url_slug` 时，均通过 301 跳转归并到 `/react-vs-toolcall`。

### 1.2 与初稿 `docs/tutorial-version-design.md` 的对照及两项关键补齐

同目录已有初稿 `docs/tutorial-version-design.md` 提出了多项合理设计，本规格书予以保留并继承：

1. **区分“内部修订快照”与“读者可见正式版本”**：复用 `article_detail` 存储每次编辑与修订的正文快照，新增 `article_edition` 表管理面向读者公开的正式版本（如“v1 初版”“v2 重构版”），避免修错别字产生的内部 `version=18, 19, 20` 暴露到前台版本菜单。
2. **保持单篇文章身份连续**：同一教程章节共用同一个 `article.id` 与 `article.url_slug`，不拆分为多条 `article` 记录。由此保证 `user_foot`（点赞、收藏、阅读足迹）、`article_pay_record`（单章付费解锁权益）、`column_article`（专栏章节排序与试看比例）保持延续，无需迁移用户资产。
3. **评论、划线与 AI 上下文按版本隔离**：为 `comment` 增加 `edition_id` 与 `article_detail_id`，使普通讨论、热门置顶、划线锚点以及 AI 流式/异步回复严格对齐读者发言时阅读的正文快照。

在此基础上，针对教程分发与读者引导的实际场景，本规格书补齐初稿缺失的两项核心能力：

| 维度 | 初稿 `docs/tutorial-version-design.md` 的局限 | 本规格书定稿方案 |
| --- | --- | --- |
| **历史版本直链与 SEO 权重** | 初稿第 3.2、3.4 节规定切版不修改地址栏、不加查询参数、第一期不支持历史版分享链接；导致知识星球答疑、社群交流时无法给零基础球友发送“打开即是初版”的专属链接 | **支持 `?v={editionCode}` 历史版直链 + Canonical 归一无参主 URL**：默认访问 `/react-vs-toolcall` 由 SSR 渲染最新正式版（v2）；访问 `/react-vs-toolcall?v=v1` 由 SSR 首屏渲染初版（v1），前端切版通过 `history.replaceState` 同步 `?v=`（切回默认版自动移除 `?v=`）；`SeoInjectService` 将 `<link rel="canonical">`、`og:url` 与 JSON-LD 固定为无参主地址 `https://paicoding.com/react-vs-toolcall`，兼顾答疑精准分发与搜索引擎权重集中 |
| **受众分流引导模型** | 初稿 `article_edition` 仅有 `edition_no`、`name`、`change_note`，缺少 URL 语义编码与面向不同基础读者的学习路径引导字段 | **补齐 `edition_code`、`audience_tag`、`guide_tip` 三元引导模型**：支持语义化短编码（如 `v1`、`v2`）、受众定位标签（如“适合零基础建立直觉”“基于最新重构源码实战”）与标题下方双态分流卡片——读新版时引导零基础读者先看 v1 建立直觉，读旧版时醒目提示当前为历史教学版并提供一键返回最新 v2 的主入口 |

### 1.3 架构路线对比与分阶段演进结论

针对技术派现有架构，三种可选路线的工程对比如下：

| 对比项 | 路线 C：仅用 `column_article_group` 二级目录分组模拟 | 路线 B：专栏组 / 复制整本新专栏 | 路线 A1（一期）：单章正式版本 `article_edition` | 路线 A2（二期扩展）：整本专栏目录多版本 `column_version` |
| --- | --- | --- | --- | --- |
| 目录与上下篇隔离 | 同一目录树堆放新旧分组，章节数翻倍，`ColumnArticleFlipDTO` 在新旧组交界处跨版串页 | 两本独立专栏，收藏数、阅读量、教程列表卡片分裂为两份 | 专栏目录保持单一章节条目，上下篇翻页保持连续，章内承载多正式版 | 同一专栏按 `version_id` 维护两棵独立目录树，章节与上下篇按所选专栏版本隔离 |
| `/react-vs-toolcall` 短链兼容 | 新旧两篇文章争抢唯一 `url_slug`，旧文被迫改名断链 | 新旧专栏下两篇文章同样争抢全局唯一 `article.url_slug` | 原短链保留不动，`/react-vs-toolcall` 默认展示 v2，`?v=v1` 直达初版 | 专栏级支持 `/column/{slug}/v/{ver}`，单章仍可复用 `article_edition` 共享根短链 |
| 实施复杂度与改动半径 | 无需改表，但体验缺陷无法靠前端弥补 | 需新建专栏关联表，破坏现有单专栏统计与购买权益 | 聚焦文章正文、评论与编辑器，不影响存量单版本专栏的分组与排序逻辑 | 需改造 `ColumnArticleDao` 与 `ColumnSettingServiceImpl` 多处无版本查询 |

**选型结论**：采用**“一期落地单章正式版本（`article_edition`）+ 二期预留整本专栏目录多版本（`column_version`）”**的分阶段架构：

1. **一期（当前优先实施）**：落地第 2 章定义的 `article_edition` 单章正式版本体系，解决 `/react-vs-toolcall` 及同类重构章节的新旧版本拆分、后台草稿与发版管理、`?v=v1` 答疑直链、受众分流引导、评论/划线/AI 上下文隔离。
2. **二期（预留扩展）**：仅当未来某套教程在版本升级时发生整本专栏级别的章节拆分、合并与大纲重排（单章多版本不足以表达两棵不同目录树）时，按第 3 章规格扩展 `column_version`；二期目录节点下的具体文章依然复用一期的 `article_edition` 模型，两阶段数据结构正交兼容、无推翻返工成本。

---

## 2. 一期完整实施方案（单章正式版本 `article_edition`，推荐优先实施）

### 2.1 Liquibase 数据库变更草案

#### 2.1.1 变更集注册位置

1. 主配置文件：`paicoding-web/src/main/resources/liquibase/changelog/000_initial_schema.xml`（当前最后一个 `changeSet` 为第 262-268 行的 `id="20261109_1"`）。
2. 新建增量 SQL 文件：`paicoding-web/src/main/resources/liquibase/data/update_schema_261113_article_edition.sql`。
3. **源码核验注意**：`paicoding-web/src/main/resources/liquibase/data/init_data_230415.sql` 内虽可检索到 `CREATE TABLE` 字样，但那是插入到演示文章正文里的教学 SQL 文本，并非真实表结构定义。真实基准结构以 `init_schema_221209.sql` 及后续增量脚本（特别是 `update_schema_231018.sql` 将业务主键与关联 ID 统一扩充为 `bigint unsigned`、`update_schema_251103.sql` 新增 `comment.highlight_info`、`update_schema_251110.sql` 新增 `article.url_slug`）为准。

在 `000_initial_schema.xml` 末尾 `</databaseChangeLog>` 前追加：

```xml
    <!-- 教程文章正式版本管理与评论版本归属 -->
    <changeSet id="20261113_1" author="paicoding">
        <sqlFile dbms="mysql" endDelimiter=";" encoding="UTF-8"
                 path="liquibase/data/update_schema_261113_article_edition.sql"/>
    </changeSet>
```

#### 2.1.2 SQL 脚本内容（`update_schema_261113_article_edition.sql`）

```sql
-- 1. article 新增当前默认正式版本指针（NULL 表示未启用多版本管理，兼容全部存量文章）
ALTER TABLE `article`
    ADD COLUMN `current_edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '当前默认公开的正式版本ID(关联article_edition.id，NULL表示未启用多版本)' AFTER `url_slug`,
    ADD INDEX `idx_current_edition_id` (`current_edition_id`);

-- 2. article_detail 新增正式版归属、元数据快照与首次公开时间
ALTER TABLE `article_detail`
    ADD COLUMN `edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '归属正式版本ID(关联article_edition.id)' AFTER `article_id`,
    ADD COLUMN `metadata_snapshot` text NULL COMMENT '该快照保存时的标题/短标题/摘要/封面JSON快照' AFTER `content`,
    ADD COLUMN `first_published_at` datetime NULL DEFAULT NULL COMMENT '首次公开发布时间(NULL表示内部草稿，禁止读者端接口读取)' AFTER `metadata_snapshot`,
    ADD INDEX `idx_article_edition_detail` (`article_id`, `edition_id`, `deleted`);

-- 3. 新建面向读者的文章正式版本表 article_edition
CREATE TABLE IF NOT EXISTS `article_edition` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` bigint unsigned NOT NULL COMMENT '文章ID(关联article.id)',
    `edition_no` int unsigned NOT NULL COMMENT '文章内单调递增正式版序号(1, 2, ...)',
    `edition_code` varchar(32) NOT NULL COMMENT 'URL查询参数短编码(如 v1、v2，限小写字母、数字、连字符)',
    `edition_name` varchar(80) NOT NULL COMMENT '版本展示名称(如 v1 初版·零基础直觉版、v2 重构实战版)',
    `audience_tag` varchar(120) NOT NULL DEFAULT '' COMMENT '受众定位标签(如 适合零基础建立直觉)',
    `guide_tip` varchar(500) NOT NULL DEFAULT '' COMMENT '标题下方分流引导提示语',
    `change_note` text NULL COMMENT '版本更新说明',
    `published_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '当前对外公开的article_detail.id',
    `draft_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '待发布或待审核的草稿article_detail.id',
    `draft_status` tinyint NOT NULL DEFAULT '0' COMMENT '草稿状态：0-无草稿，1-草稿编辑中，2-待审核，3-审核驳回',
    `state` tinyint NOT NULL DEFAULT '0' COMMENT '正式版状态：0-未发布，1-已公开发布，2-已撤回',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-正常，1-已删除',
    `first_published_at` datetime NULL DEFAULT NULL COMMENT '该正式版首次公开发布时间',
    `last_published_at` datetime NULL DEFAULT NULL COMMENT '该正式版最近一次修订发布时间',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_edition_no` (`article_id`, `edition_no`),
    UNIQUE KEY `uk_article_edition_code` (`article_id`, `edition_code`),
    KEY `idx_article_state` (`article_id`, `state`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章正式版本表';

-- 4. comment 新增正式版本归属与原始公开正文快照关联
ALTER TABLE `comment`
    ADD COLUMN `edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '所属正式版本ID(关联article_edition.id，早期未归属讨论为NULL)' AFTER `article_id`,
    ADD COLUMN `article_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '发言或划线时所依据的公开article_detail.id' AFTER `edition_id`,
    ADD INDEX `idx_article_edition_top` (`article_id`, `edition_id`, `deleted`, `top_comment_id`, `id`),
    ADD INDEX `idx_top_comment_deleted` (`top_comment_id`, `deleted`, `id`);
```

#### 2.1.3 数据一致性约束（服务层事务保证，不建物理外键以保持仓库风格一致）

1. 当 `article.current_edition_id IS NOT NULL` 时，目标 `article_edition` 记录必须满足 `article_id = article.id`、`state = 1 (PUBLISHED)`、`deleted = 0` 且 `published_detail_id IS NOT NULL`。
2. 凡被 `article_edition.published_detail_id` 指向的 `article_detail` 行，其 `first_published_at` 必须非空；任何 `first_published_at IS NULL` 的记录均为内部草稿，读者端一切接口（含评论上下文反查、AI 问答、切版预览）拒绝读取。
3. 启用多版本的文章（`current_edition_id IS NOT NULL`），保存草稿或发布修订一律在 `article_detail` 追加新行，严禁原地覆盖已有 `article_detail.content`。
4. `HighlightDto`（`paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/comment/dto/HighlightDto.java`）新增可选字段 `private Long anchoredDetailId;`（配合 `@JsonSerialize(using = ToStringSerializer.class)`），记录同正式版小修订后重定位到的新快照 ID；`comment.article_detail_id` 永久保留最初划线时的原始快照 ID。

---

### 2.2 后端核心改造清单（`paicoding-api` / `paicoding-service` / `paicoding-web`）

#### 2.2.1 新增实体、枚举与上下文模型（`paicoding-api` & `paicoding-service`）

1. **枚举类（`paicoding-api/src/main/java/com/github/paicoding/forum/api/model/enums/article/`）**：
   - `ArticleEditionStateEnum`：`UNPUBLISHED(0)`、`PUBLISHED(1)`、`WITHDRAWN(2)`。
   - `ArticleEditionDraftStatusEnum`：`NONE(0)`、`DRAFT(1)`、`REVIEW(2)`、`REJECTED(3)`。
   - `ArticlePublishModeEnum`：`LEGACY(0, "普通保存")`、`REVISE_EDITION(1, "修订指定正式版")`、`NEW_EDITION(2, "发布新正式版")`、`SAVE_DRAFT(3, "仅存版本草稿")`。
   - `CommentEditionScopeEnum`：`CURRENT("current")`、`HISTORY("history")`、`ALL("all")`。
2. **数据实体与 Mapper（`paicoding-service/.../article/repository/`）**：
   - 新增 `ArticleEditionDO`（映射 `article_edition` 表）；扩展 `ArticleDO`（增加 `currentEditionId`）、`ArticleDetailDO`（增加 `editionId`、`metadataSnapshot`、`firstPublishedAt`）、`CommentDO`（增加 `editionId`、`articleDetailId`）。
   - 新增 `ArticleEditionMapper extends BaseMapper<ArticleEditionDO>` 与 `ArticleEditionDao`。
   - 在 `ArticleMapper.java` 新增行级锁查询 `@Select("SELECT * FROM article WHERE id = #{articleId} FOR UPDATE") ArticleDO selectForUpdateById(@Param("articleId") Long articleId);`，供递增 `article_detail.version` 与 `article_edition.edition_no` 时串行化并发写入。
3. **前端交互 DTO（所有 `Long` 型 ID 字段统一加 `@JsonSerialize(using = ToStringSerializer.class)`，防止 64 位雪花/大整数在浏览器端精度丢失）**：
   - `ArticleReadingContextDTO`：包含 `articleId`、`versioned`（`boolean`）、`currentEditionId`、`currentEditionCode`、`selectedEditionId`、`selectedEditionCode`、`selectedDetailId`、`viewingHistoryEdition`（`boolean`）、`invalidParamFallback`（`boolean`，当 URL 传入不存在或未发布的 `?v=xxx` 回退默认版时为 `true`）、`editions`（`List<ArticleEditionSummaryDTO>`）、`guideCard`（`EditionGuideCardDTO`）。
   - `EditionGuideCardDTO`：包含 `mode`（`"CURRENT_DEFAULT"` / `"HISTORY_EDITION"`）、`currentEditionCode`、`currentEditionName`、`selectedEditionCode`、`selectedEditionName`、`audienceTag`、`guideTip`、`recommendedSwitchCode`（在默认版 v2 下指向最早或推荐的基础版如 `v1`；在历史版 `v1` 下指向 `currentEditionCode` 如 `v2`）、`recommendedSwitchLabel`。
   - `ArticleMetadataSnapshotDTO`：`title`、`shortTitle`、`summary`、`cover`（JSON 序列化存入 `article_detail.metadata_snapshot`）。
   - 扩展 `ArticlePostReq`（`paicoding-api/.../vo/article/ArticlePostReq.java`）：新增 `publishMode`、`editionId`、`editionCode`、`editionName`、`audienceTag`、`guideTip`、`changeNote`、`setAsDefault`（`Boolean`）、`expectedPublishedDetailId`（`Long`，防多人同时修订覆盖）。
   - 扩展 `SimpleArticleDTO`（`paicoding-api/.../vo/article/dto/SimpleArticleDTO.java`，当前第 46-47 行已有 `Timestamp updateTime`）：继续复用 `updateTime` 承载章节最新正式版发布时间，新增 `String editionCode`、`Integer editionCount`。

#### 2.2.2 阅读上下文统一解析器 `ArticleReadingContextResolver`

在 `paicoding-service/.../article/service/` 下新建 `ArticleReadingContextResolver`，统一收口 SSR 首屏、页内切版 API、评论列表与 AI 上下文的版本选择逻辑：

1. **未启用多版本（`article.current_edition_id == null`）**：返回 `versioned = false`，忽略任何 `?v=` 参数（若请求携带 `?v=` 则标记 `invalidParamFallback = true` 供前端清理地址栏），正文继续读取 `ArticleDao#findLatestDetail(articleId)`，确保全站数千篇存量文章零行为变化。
2. **已启用多版本（`article.current_edition_id != null`）**：
   - 查询 `article_edition` 中 `article_id = articleId AND state = 1 AND deleted = 0` 的全部正式版，按 `edition_no DESC` 排序；启用多版本后**严禁回退调用 `findLatestDetail(articleId)`**，防止未发布的 `draft_detail_id` 泄露给读者。
   - 若 `requestedEditionCode` 为空：选中 `id == article.current_edition_id` 的正式版，`viewingHistoryEdition = false`。
   - 若 `requestedEditionCode` 非空（先按 `^[a-z0-9][a-z0-9_-]{0,31}$` 校验格式并转小写）：在已发布正式版列表中匹配 `edition_code`：
     - 命中且对应 `article_detail.first_published_at != null`：选中该正式版，当 `selected.id != article.current_edition_id` 时置 `viewingHistoryEdition = true`。
     - 未命中：SSR 首屏回退至 `article.current_edition_id` 并置 `invalidParamFallback = true`；页内切版 REST 接口即刻返回 `StatusEnum.ILLEGAL_ARGUMENTS_MIXED`（提示“目标版本不存在或尚未发布”），保证前端不发生半切换。
   - 读取选中 `article_detail.metadata_snapshot`：若历史版本快照存有当时的 `title` / `summary`，阅读页正文标题采用快照标题（若快照为空则回退 `article.title`）。

#### 2.2.3 路由控制器、权限截断共享组件与切版接口（`paicoding-web`）

1. **`ArticleSlugViewController.java`（第 35-52 行）**：
   - 将 `article` 方法签名扩展为接收 `@RequestParam(value = "v", required = false) String editionCode`：
     - 专栏文章分支调用 `columnViewController.articleByRootArticleSlug(article.getId(), editionCode, model)`；
     - 普通文章分支调用 `articleViewController.detailByArticleId(article.getId(), editionCode, model)`。
2. **`ColumnViewController.java`（第 154-253 行、第 370-437 行、第 525-588 行）**：
   - 扩展 `articleBySection`（154-171）、`articleBySlugUnderColumn`（181-186）、`articleByArticleId`（197-210）、`articleByArticleSlug`（220-232）、`articleByRootArticleSlug`（241-253）接收可选 `@RequestParam(value = "v", required = false) String editionCode`。当这些旧路径触发向规范短链 `/{articleSlug}` 的 301 跳转时，若 `editionCode` 通过正则白名单校验（`^[a-z0-9][a-z0-9_-]{0,31}$`），将其保留在重定向目标上（如 `/react-vs-toolcall?v=v1`）。
   - **抽取专栏访问控制与试看截断共享组件（必做项）**：当前专栏免费/登录阅读/星球专属/单章付费判断及按 `preview_percent` 截断 Markdown 的逻辑位于 `ColumnViewController` 私有方法 `updateReadType`（第 525-555 行）和 `trimContent`（第 564-581 行，及第 583-588 行 `previewLengthConfig`）。将其提取为独立 Spring 组件 `ColumnArticleAccessEvaluator`（位于 `paicoding-web/.../front/article/helper/` 或 `paicoding-service`），输入 `(ArticleDTO, ColumnDTO, ColumnArticleDO, Long currentUserId)`，输出截断后的正文 HTML、`readType`、`canReadFull` 标记。SSR 首屏与切版 REST 接口必须调用同一个 `ColumnArticleAccessEvaluator`，杜绝切版接口绕过专栏付费墙返回完整 Markdown。
   - 改造 `buildColumnArticleView`（第 370-437 行）：
     - 先解析 `ArticleReadingContextDTO context = readingContextResolver.resolveForSsr(articleId, editionCode)`；
     - 若 `context.isVersioned()`，用 `context.getSelectedDetailId()` 加载对应 `ArticleDetailDO` 替换默认 `articleDTO.content` 及快照标题，再送入 `ColumnArticleAccessEvaluator` 截断并渲染 Markdown；
     - 第 378-384 行的四路 `CommentReadService` 调用（第 378 行 `getArticleComments`、第 379 行 `queryTopCommentCount`、第 382 行 `queryHotComment`、第 384 行 `queryHighlightComments`）全部传入 `context.getSelectedEditionId()` 及 `CommentEditionScopeEnum.CURRENT`，并额外查询 `historyTopCommentCount`（该文章下非本版的顶层讨论数，含 `edition_id IS NULL` 的早期讨论）一并放入 `ColumnArticlesDTO`（或 `model.addAttribute("readingContext", context)`）。
3. **新增读者端版本 REST 接口（置于 `ArticleRestController.java` 或新建 `ArticleEditionRestController.java`）**：
   - `GET /article/api/{articleId}/editions`：校验文章可见性，返回已发布版本列表与 `currentEditionCode`。
   - `GET /article/api/{articleId}/edition-view?v={editionCode}`：
     - 校验文章已上线且未删除，调用 `readingContextResolver.resolveStrict(articleId, editionCode)`；
     - 若该文章属于专栏（`columnService.getColumnArticleRelation(articleId) != null`），调用 `ColumnArticleAccessEvaluator` 执行权限校验与试看截断；若为普通独立文章，复用 `ArticleReadViewServiceExtend` 权限校验；
     - 查询该版本的首屏顶层评论、本版顶层评论数、历史版本顶层评论数、本版热门评论及本版划线数据，复用现有 `CommentRestController`（第 135、560 行）使用的 `templateEngineHelper.render("views/article-detail/comment/index", vo)` 渲染评论区 HTML（其内部引用 `components/comment/comment-list :: comment_list`，见 `views/article-detail/comment/index.html:2`）；
     - 返回 `ResVo<ArticleEditionViewVO>`（含 `readingContext`、`title`、`articleContentHtml`、`needPay`、`readType`、`commentsHtml`、`topCommentCount`、`historyTopCommentCount`、`highlights`）。
     - **副作用禁令**：该接口**严禁调用** `ArticleReadServiceImpl#queryFullArticleInfo`（`ArticleReadServiceImpl.java:139-166`），因其在第 143 行调用 `countService.incrArticleReadCount` 增加阅读量、第 148-149 行写入足迹；页内切版只做内容读取，不重复累加阅读计数。

#### 2.2.4 专用版本草稿与正式版发布事务（`ArticleDao` / `ArticleEditionWriteService`）

1. **现有写入隐患定位**：
   - `ArticleDao#updateArticleContent`（`paicoding-service/.../ArticleDao.java:161-171`）：当 `update=true` 时调用 `ArticleDetailMapper#updateContent`（`ArticleDetailMapper.java:23-24`）执行 `UPDATE article_detail SET content = #{content}, version = version + 1 ... ORDER BY version DESC LIMIT 1`，原地覆写最后一条正文；当 `update=false` 时复用 `latest` 对象且未重置 `createTime` / `updateTime`，也无行锁保护 `version + 1`。
   - `ArticleSettingRestController#save`（`paicoding-web/.../admin/rest/ArticleSettingRestController.java:57-66`）：当 `req.getArticleId() != null` 时传入 `author = null`，交给 `ArticleWriteServiceImpl#saveArticle`（第 92-117 行）。
2. **新增 `ArticleEditionWriteServiceImpl` 事务规范（`@Transactional(rollbackFor = Exception.class)`）**：
   - **权限与原作者保留**：任何针对已有 `articleId` 的草稿保存或版本发布，先通过 `articleMapper.selectForUpdateById(articleId)` 锁定主记录，校验当前登录后台用户为文章原作者或具备后台管理员角色；全流程保持 `article.user_id` 原值不变。
   - **追加式快照创建 `appendDetailSnapshot`**：在持有 `article` 行锁的前提下查询 `SELECT COALESCE(MAX(version), 0) FROM article_detail WHERE article_id = #{articleId}`，新建 `ArticleDetailDO`（显式赋当前时间，`version = maxVersion + 1`，序列化 `metadataSnapshot`）。若为保存草稿则 `firstPublishedAt = null`；若免审直发则 `firstPublishedAt = now`。
   - **保存版本草稿 `saveEditionDraft`**：
     - 若针对已有 `editionId` 保存草稿：生成新草稿 `detail`（或仅更新该版本尚未公开过的 `draft_detail_id` 行——前提是 `first_published_at IS NULL` 且未被任何评论引用），更新 `article_edition.draft_detail_id` 并将 `draft_status` 置为 `DRAFT(1)`（若此前为 `REVIEW` 或已审核，编辑后重置为 `DRAFT`）。
     - **严禁修改** `article.title`、`article.summary`、`article.status`、`article.update_time` 及 `article_edition.published_detail_id`，确保保存草稿期间线上阅读、专栏目录时间与 ES 搜索结果毫发无损。
   - **修订已有正式版 `publishEditionRevision`**：
     - 校验 `expectedPublishedDetailId`（若传入且与库内当前 `edition.publishedDetailId` 不一致，返回并发冲突提示刷新）；
     - 复用现有免审白名单判断：若需审核，将 `draft_detail_id` 指向本次快照、`draft_status = REVIEW(2)`，不切换 `published_detail_id`；若免审，将本次快照 `first_published_at = now` 落库，更新 `edition.published_detail_id = newDetailId`、`edition.last_published_at = now`、`edition.draft_detail_id = null`、`edition.draft_status = NONE(0)`；
     - **默认版与历史版元数据区分**：仅当 `edition.id.equals(article.getCurrentEditionId())` 时，才将本次标题、短标题、摘要、封面与 `update_time` 同步写回 `article` 主表并触发 ES 同步事件；若作者仅修订历史版 `v1` 的错别字，只更新 `v1` 自身快照与 `v1.last_published_at`，不覆盖线上 `v2` 的主标题与文章列表排序时间。
   - **发布新正式大版本 `publishNewEdition`**：
     - 若该文章此前未启用多版本（`article.current_edition_id == null`），在同一事务内先为当前线上的旧正文自动归档补建 `edition_no = 1`、`edition_code = "v1"`（或后台指定编码）的首个正式版记录，将其 `published_detail_id` 指向原最新有效 `article_detail`（补写 `first_published_at = article.create_time`），再创建 `edition_no = 2` 的新版本，避免首次升版时丢失原线上版本！
     - 为新大版本校验 `edition_code` 唯一性，插入 `article_edition`（`state = PUBLISHED(1)`，写入 `edition_name`、`audience_tag`、`guide_tip`、`change_note`、`first_published_at = now`、`last_published_at = now`）；
     - 当 `setAsDefault != false`（默认 `true`）时，更新 `article.current_edition_id = newEdition.id`，同步更新 `article` 主表标题、短标题、摘要、封面、`status = PUBLISHED` 与 `update_time`，发布事务提交后触发缓存清理与 ES 索引更新。
   - **撤回正式版 `withdrawEdition`**：将目标 `edition.state` 置为 `WITHDRAWN(2)`；若目标恰好等于 `article.current_edition_id`，请求参数必须显式携带仍在发布态的 `fallbackEditionId` 同步接任 `article.current_edition_id` 并回填该版的 `metadata_snapshot` 到 `article` 表；若已无任何可公开版本，则同步将 `article.status` 下线。

#### 2.2.5 评论四路查询隔离、划线同版重定位与通知跳转（`CommentDao` / `CommentWriteServiceImpl` / `CommentRestController`）

1. **`CommentDao.java` 与 `CommentMapper.xml` 四路隔离改造**：
   - 在 `CommentDao.java` 中为以下查询补充带 `(Long editionId, CommentEditionScopeEnum scope)` 的重载（当文章未启用多版本即 `editionId == null && scope == ALL` 时维持原 SQL）：
     - `listTopCommentList`（第 59-66 行）：`CURRENT` 条件为 `edition_id = #{editionId}`；`HISTORY` 条件为 `(edition_id <> #{editionId} OR edition_id IS NULL)`。
     - `topCommentCount`（第 96-103 行）：同上，分别统计“本版讨论数”与“历史版本讨论数”。
     - `getHotComment`（第 105-115 行）及 `CommentMapper.xml#getHotTopCommentId`（`paicoding-service/src/main/resources/mapper/CommentMapper.xml:30-40`）：原 SQL 为子回复表与顶层评论表的自连接（`from comment c inner join comment tc on c.top_comment_id = tc.id where c.article_id = #{articleId} and c.top_comment_id > 0 ...`），新增 `getHotTopCommentIdByEdition` 时保留该自连接结构，并在 `WHERE` 子句明确带上顶层评论表别名追加 `AND tc.edition_id = #{editionId}`（避免列名歧义报错），确保旧版高赞/多回复评论不会置顶在最新版的“热门评论”槽位。
     - `listHighlightCommentList`（第 32-40 行）：严格限定 `edition_id = #{editionId}`，历史版的划线绝不渲染到新版正文 DOM 上。
2. **`CommentWriteServiceImpl.java` 写入与重定位校验**：
   - `saveComment`（第 58-68 行）/ `addComment`（第 102-133 行）：
     - 当 `parentCommentId != null && parentCommentId > 0`（回复已有评论）时：从库内读取根评论（`topCommentId` 或 `parentComment`），强制校验 `parent.getArticleId().equals(req.getArticleId())`，服务端令子回复继承 `parent.getEditionId()` 与 `parent.getArticleDetailId()`，忽略前端传来的版本字段。
     - 当发表顶层评论或新建划线评论时：若文章已启用多版本，必须校验传入的 `editionId` 属于该文章且 `state == PUBLISHED`，`articleDetailId` 属于该 `editionId` 且 `first_published_at != null`（若前端未传 `articleDetailId`，默认绑定该 `edition` 当前的 `published_detail_id`）。
   - `relocateHighlight`（第 71-100 行）：校验待重定位评论所属的 `edition_id` 必须与作者当前在页面打开的 `editionId` 一致（仅允许在同一正式版小修订后重定位失效划线，禁止将 `v1` 划线跨大版本搬移到 `v2`）；重定位成功时保留 `comment.article_detail_id` 原始值不变，将当前版本最新的 `published_detail_id` 写入 `HighlightDto.anchoredDetailId` 并递增 `HighlightDto.revision`。
3. **`CommentRestController.java` 接口扩展与通知上下文接口**：
   - `list`（90-99）、`save`（108-137）、`highlightComment`（147-168）、`relocateHighlight`（171-176）、`articleCommentsHtml`（405-416）、`listPageHtml`（589-615）：补充接收 `editionId`、`articleDetailId`、`scope`（默认 `current`），在返回评论 VO（`BaseCommentDTO` / `TopCommentDTO`）中补充 `editionId`、`editionCode`、`editionName`（供“历史版本讨论”列表渲染 `[来自 v1 初版]` 或 `[早期讨论]` 标签）。
   - 新增 `GET /comment/api/context?commentId={commentId}`：校验目标评论对应的文章阅读权限，返回目标评论所在的顶层线程 `TopCommentDTO`、所属 `editionId`、`editionCode`（若 `editionId == null` 则标记 `unassignedHistory = true`），使站内通知跳入或历史评论跨页定位无需顺序翻页即可拉取目标线程。
   - **修复评论通知遗漏 `commentId` 与前台评论通知链接**：`NotifyMsgListener.java` 中 `saveReplyNotify`（第 128-144 行）已调用 `.setCommentId(comment.getId())`（对应前台 `notify-reply-item.html:36` 的 `#comment-${msg.commentId}`），但 `saveCommentNotify`（第 106-121 行）遗漏了 `setCommentId`，且前台文章评论通知模板 `paicoding-ui/src/main/resources/templates/components/notice/notify-comment-item.html` 第 22 行评论内容目前为无链接纯文本 `<div>`、第 34 行仅跳往 `#commentList`。需在 `saveCommentNotify` 补充 `.setCommentId(comment.getId())`，并同步将 `notify-comment-item.html` 第 22 行与第 34 行升级为当 `msg.commentId != null` 时拼接 `'#comment-' + ${msg.commentId}`，实现评论与回复通知一致定位。

#### 2.2.6 AI 问答与流式解释快照冻结（`ArticleReadServiceImpl` / `CommentWriteServiceImpl` / `CommentRestController`）

1. **改造 `ArticleReadServiceImpl#queryArticleContentForAI`（`paicoding-service/.../ArticleReadServiceImpl.java:111-114`）**：
   - 保留原单参数方法供非版本化场景调用（但内部改为：若 `article.current_edition_id != null`，读取当前默认版的 `published_detail_id` 而非 `findLatestDetail`）。
   - 新增重载方法 `String queryArticleContentForAISnapshot(Long articleId, Long editionId, Long articleDetailId)`：
     - 若 `articleDetailId != null`：查询 `article_detail` 并校验 `detail.articleId.equals(articleId) && detail.firstPublishedAt != null`，返回该历史快照的 `content`；
     - 若 `articleDetailId == null && editionId != null`：查询该 `edition` 的 `published_detail_id` 正文；
     - 若文章已启用多版本，但目标为存量未分版早期评论（`editionId == null && articleDetailId == null`）：返回 `null`，调用方在构造 System Prompt 时明确告知模型“该评论产生于文章早期历史阶段，未关联精确正文快照，请仅依据用户选中的引用片段与评论线程上下文作答，不要编造当前新版代码细节”。
2. **改造三处调用点，请求入口即刻冻结快照 ID**：
   - `CommentWriteServiceImpl#initQABotSystemPrompt`（第 261-268 行）：传入触发评论自身的 `(comment.getArticleId(), comment.getEditionId(), comment.getArticleDetailId())`，异步机器人生成的回帖继承同一组 `editionId` 与 `articleDetailId`。
   - `CommentRestController#highlightAiStream`（第 182-293 行）与 `buildHighlightAiSystemPrompt`（第 456-462 行）：在流式请求入口校验并冻结 `(req.getArticleId(), req.getEditionId(), req.getArticleDetailId())`。
   - `CommentRestController#commentAiStream`（第 299-381 行）与 `buildCommentAiSystemPrompt`（第 545-551 行）：同上冻结快照，确保流式生成期间作者即使在后台发布新版，也不会改变本次 AI 回答依据的原文。

#### 2.2.7 SEO Canonical 归一、ES 搜索索引与专栏目录更新时间修正

1. **`SeoInjectService.java`（`paicoding-web/.../global/SeoInjectService.java:148-229`）**：
   - `initColumnSeo` 第 220-226 行调用的 `applyCanonicalUrl` 继续生成**不带任何查询参数**的规范 URL（例如 `https://paicoding.com/react-vs-toolcall`）。无论请求是 `/react-vs-toolcall` 还是 `/react-vs-toolcall?v=v1`，页面 `<link rel="canonical">`、`og:url` 以及 JSON-LD 结构化数据中的 `url` / `mainEntityOfPage` 始终指向无参主地址 `https://paicoding.com/react-vs-toolcall`。
   - 当 `readingContext.isViewingHistoryEdition()` 为 `true` 时，页面 `<title>` 拼接历史版本名称（如 `ReAct vs ToolCall（v1 初版） - 技术派`）便于用户识别多标签页，而 SEO `dateModified` 在默认版下取当前正式版 `last_published_at`。
   - `SitemapServiceImpl.java`（`paicoding-service/.../sitemap/service/impl/SitemapServiceImpl.java` 第 72-146 行、第 253-305 行）：维持每篇文章仅输出一条无参主 URL，不把 `?v=v1` 写入 `sitemap.xml` 或 `llms.txt`。
2. **`ArticleMapper.xml#articleSearchDocumentSelect` 与 ES 事务提交后同步**：
   - 修改 `paicoding-service/src/main/resources/mapper/ArticleMapper.xml` 第 135-168 行的 `articleSearchDocumentSelect`：
     - 原第 152-162 行通过子查询 `select article_id, max(version) as version from article_detail where deleted = 0 group by article_id` 获取正文，一旦存在未发布的版本草稿快照（`version` 更大），ES 重建或增量同步就会把未公开草稿索引出去！
     - 改为：`LEFT JOIN article_edition ae ON a.current_edition_id = ae.id AND ae.deleted = 0`，正文关联条件使用 `LEFT JOIN article_detail ad ON ad.id = CASE WHEN a.current_edition_id IS NOT NULL THEN ae.published_detail_id ELSE latest_legacy.detail_id END`（未启用多版本的文章继续取最大 `version` 的有效记录，已启用多版本的文章严格只取 `ae.published_detail_id`），并将第 164 行子查询内的 `group_concat(column_id) as column_ids` 改为 `group_concat(distinct column_id) as column_ids`。
   - 修改 `ArticleSearchSyncListener.java`（`paicoding-service/.../article/service/search/ArticleSearchSyncListener.java:22-39`）：保留第 22 行的 `@Async`，将第 23 行的 `@EventListener(ArticleMsgEvent.class)`（修饰第 24 行 `onArticleEvent` 方法）替换为 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)`，杜绝发布事务尚未提交时异步线程提前查库读到旧 `current_edition_id` 导致 ES 索引滞后。
3. **`ColumnArticleMapper.xml#listColumnArticles` 更新时间与多版本徽标扩展**：
   - 修改 `paicoding-service/src/main/resources/mapper/ColumnArticleMapper.xml` 第 11-31 行（当前工作区第 21 行已有 `coalesce(a.update_time, a.create_time) as updateTime`）：
     - 在第 28 行 `where ca.column_id = #{columnId}` 后补齐 `AND a.deleted = 0` 过滤条件；
     - 增加 `LEFT JOIN article_edition ae ON a.current_edition_id = ae.id AND ae.deleted = 0`；
     - 将第 21 行扩展为 `COALESCE(ae.last_published_at, a.update_time, a.create_time) AS updateTime`（如此保存草稿或修订历史版不改变目录时间，仅默认版正式发布时推进目录更新时间），并新增返回列 `ae.edition_code AS editionCode` 与子查询 `(SELECT COUNT(1) FROM article_edition e2 WHERE e2.article_id = a.id AND e2.state = 1 AND e2.deleted = 0) AS editionCount`，映射到 `SimpleArticleDTO`。

---

### 2.3 后台管理端（`/Users/itwanger/Documents/GitHub/paicoding-admin`）改造清单

#### 2.3.1 后端管理端接口（`ArticleSettingRestController.java`）

在 `paicoding-web/src/main/java/com/github/paicoding/forum/web/admin/rest/ArticleSettingRestController.java`（第 41-151 行，类上已映射 `/api/admin/article/` 与 `/admin/article/` 双前缀）新增以下接口：

1. `GET edition/list?articleId={articleId}`：返回该文章的版本管理信息（是否已启用多版本、`currentEditionId`、全部 `article_edition` 列表及其 `editionCode`、`editionName`、`audienceTag`、`guideTip`、`changeNote`、`state`、`draftStatus`、`publishedDetailId`、`draftDetailId`、`firstPublishedAt`、`lastPublishedAt`）。
2. `GET edition/detail?articleId={articleId}&editionId={editionId}&loadDraft={true|false}`：加载指定正式版的草稿正文（`loadDraft=true` 且存在 `draft_detail_id` 时优先返回草稿，否则返回 `published_detail_id` 正文）及 `metadata_snapshot`，供作者切换版本编辑或只读预览旧版。
3. `POST edition/draft/save`：调用 `ArticleEditionWriteService#saveEditionDraft`，保存服务端版本草稿，返回最新 `draftDetailId` 与保存时间戳。
4. `POST edition/publish`：调用 `ArticleEditionWriteService` 执行“修订指定版本（`REVISE_EDITION`）”或“发布新正式版（`NEW_EDITION`）”。
5. `POST edition/default`：切换默认展示版本（更新 `article.current_edition_id`，同步该版元数据快照至 `article` 表并触发 ES 更新）。
6. `POST edition/withdraw`：撤回指定版本并指定回退默认版。
7. 兼容加固原 `POST save`（第 57-66 行）与 `POST update`（列表快捷改标题/摘要）：当检测到目标文章 `current_edition_id != null` 时，自动转发至 `ArticleEditionWriteService` 的当前默认版修订逻辑，防止走旧 `saveArticle` 绕过版本指针。

#### 2.3.2 管理端 API 定义（`src/api/modules/article.ts`）

在 `/Users/itwanger/Documents/GitHub/paicoding-admin/src/api/modules/article.ts`（第 1-73 行）新增类型与请求函数（注意保留现有 `saveArticleApi` 对应 `/article/save`、`updateArticleApi` 对应 `/article/update` 的语义边界，不要混用）：

```ts
export interface ArticleEditionItem {
	id: string;
	articleId: string;
	editionNo: number;
	editionCode: string;
	editionName: string;
	audienceTag: string;
	guideTip: string;
	changeNote?: string;
	publishedDetailId?: string;
	draftDetailId?: string;
	draftStatus: number; // 0-无草稿 1-编辑中 2-待审核 3-驳回
	state: number; // 0-未发布 1-已发布 2-已撤回
	isDefault: boolean;
	firstPublishedAt?: string;
	lastPublishedAt?: string;
}

export const getArticleEditionListApi = (articleId: string) =>
	http.get<ArticleEditionItem[]>(`${PORT1}/article/edition/list`, { articleId });

export const getArticleEditionDetailApi = (params: { articleId: string; editionId: string; loadDraft?: boolean }) =>
	http.get<any>(`${PORT1}/article/edition/detail`, params);

export const saveArticleEditionDraftApi = (params: any) =>
	http.post<any>(`${PORT1}/article/edition/draft/save`, params);

export const publishArticleEditionApi = (params: any) =>
	http.post<any>(`${PORT1}/article/edition/publish`, params);

export const setDefaultArticleEditionApi = (params: { articleId: string; editionId: string }) =>
	http.post<any>(`${PORT1}/article/edition/default`, params);

export const withdrawArticleEditionApi = (params: { articleId: string; editionId: string; fallbackEditionId?: string }) =>
	http.post<any>(`${PORT1}/article/edition/withdraw`, params);
```

#### 2.3.3 顶部工具栏改造（`src/views/article/edit/search/index.tsx`）

定位 `/Users/itwanger/Documents/GitHub/paicoding-admin/src/views/article/edit/search/index.tsx`（第 11-116 行）：

1. 保留原有的“导入”“覆盖源文件”“上传视频”“转链”及右侧主按钮（触发 `handleSave`，即 `index.tsx:1260` 的 `handleSaveOrUpdate` 打开抽屉）。
2. 当处于已有文章编辑态（`articleId > 0`）时，在工具栏插入紧凑的**版本工作区控件**：
   - 版本下拉选择器：展示当前正在编辑的目标（如 `当前默认 · v2 重构实战版`、`历史版 · v1 初版` 或 `新版本草稿（未发布）`），标注是否存在未发布草稿徽标。切换下拉项时，若当前编辑器有未保存改动，弹出确认框提示先保存草稿再加载目标版本内容。
   - 快捷按钮 `保存草稿`：调用 `saveArticleEditionDraftApi` 将当前编辑器内容存入服务端草稿（不打开抽屉、不影响线上已公开正文），并在工具栏显示“本地自动保存时间 / 服务端草稿保存时间”双状态提示。
   - 快捷按钮 `版本管理`：打开轻量侧边列表弹窗，支持查看全部历史版本发布记录、一键切换默认阅读版本、撤回异常版本、只读预览旧版 Markdown。

#### 2.3.4 编辑器主体、抽屉发布模式与本地草稿隔离（`src/views/article/edit/index.tsx`）

定位 `/Users/itwanger/Documents/GitHub/paicoding-admin/src/views/article/edit/index.tsx`：

1. **本地自动保存 `draftKey` 按版本隔离（第 613 行、第 619-621 行、第 664-704 行）**：
   - 现状：第 613 行解构 `const { articleId, status } = location.state || {};`，第 619-621 行定义 `const draftKey = useMemo(() => { return articleId ? `ARTICLE_DRAFT_${articleId}` : "ARTICLE_DRAFT_NEW"; }, [articleId]);`。
   - 改造：引入当前编辑目标状态 `editionTarget`（取值：`LEGACY` | `EDITION_${editionId}` | `NEW_EDITION`），将 `draftKey` 改为：
     ```ts
     const draftKey = useMemo(() => {
     	if (!articleId) return "ARTICLE_DRAFT_NEW";
     	if (!editionTarget || editionTarget === "LEGACY") return `ARTICLE_DRAFT_${articleId}`;
     	return `ARTICLE_DRAFT_${articleId}_${editionTarget}`;
     }, [articleId, editionTarget]);
     ```
   - 仅当对应版本正式发布成功或主动放弃草稿时清理该 `draftKey`；服务端保存草稿成功时不清空未发布的编辑态恢复能力，但同步基准哈希消除未保存离开警告。
2. **右侧发布抽屉 `Drawer` 增加“修订当前版 vs 发布新大版本”（第 1260-1266 行、第 2727-2797 行、第 3224-3243 行）**：
   - 在 `Drawer` 表单最上方新增 `publishMode` 分段选择器（`Radio.Group`）：
     - 选项一（默认）：**修订当前所选版本**（文案说明：“修错字、补充段落或小幅更新，更新后留在当前版本，不增加读者端版本菜单项”）。展示可选字段 `changeNote`（修订备注）。
     - 选项二：**发布为新正式大版本**（文案说明：“大版本重构或重写，保留旧版本供读者切换阅读，当前编辑器正文将作为新大版本发布”）。展开以下表单项：
       - `editionCode`（必填，默认按 `v${maxEditionNo + 1}` 自动预填，校验 `^[a-z0-9][a-z0-9_-]{0,31}$`）；
       - `editionName`（必填，如 `v2 重构实战版`）；
       - `audienceTag`（必填，提供快捷预设标签点击填入：`适合零基础建立直觉`、`基于最新重构源码实战`、`进阶原理与源码剖析`）；
       - `guideTip`（选填，填写后展示在读者端标题下方引导卡片中）；
       - `changeNote`（选填，版本更新说明）；
       - `setAsDefault`（开关，默认开启：“发布后设为默认展示版本”；若首次从单版本文章升级为双版本，同时折叠展示“原线上版本归档信息”默认预填 `v1` / `初版（零基础直觉版）`，允许作者一并确认初版引导语）。
   - **关键状态保护**：用户在编辑器内写好新版全文后打开抽屉，在“修订当前版”和“发布为新正式大版本”之间来回切换单选项时，**只切换抽屉表单字段，绝不触发 `getArticle` 重新拉取线上旧正文覆盖 `contentRef.current`**。
   - **修复 `articleType` 硬编码（第 2769 行）**：现状 `handleSubmit` 在第 2769 行提交载荷中硬编码 `articleType: "BLOG"`；改为保留 `getArticle` 加载到的原 `articleType || "BLOG"`，并在启用多版本或选择“发布为新正式大版本”时调用 `publishArticleEditionApi`（携带 `expectedPublishedDetailId`）。
3. **三处覆盖线上 `urlSlug` 与 `status` 的高危入口保护（必改项）**：
   - **入口一：`handleImportMarkdown`（第 1628-1825 行）**：
     - 现状问题：第 1682 行（在 `if (fmData)` 分支内）及第 1806 行将状态设为 `PushStatusEnum.Published`（见第 23 行导入的 `PushStatusEnum`）；第 1678 行与第 1761-1781 行根据 FrontMatter、文件名或调用 AI 接口重写 `urlSlug`。若作者为 `/react-vs-toolcall` 导入本地写好的 `react-vs-toolcall-v2.md`，会把线上固定短链篡改为 `react-vs-toolcall-v2` 造成线上原链接 404！
     - 保护措施：当 `Boolean(articleId) && Boolean(lockedOnlineSlug)`（即编辑已有文章且库内已有非空 `urlSlug`）时，导入 Markdown **禁止修改 `urlSlug`**，也**禁止自动覆盖 `status`**；仅更新编辑器正文 `content`（标题仅在用户勾选或当前标题为空时更新）。
   - **入口二：`handleImportWord`（第 2004-2605 行）**：
     - 现状问题：第 2579-2584 行用提取出的 H1 标题调用 `handleGenerateUrlSlug({ shortTitle: articleTitle }, true)` 覆盖表单 `urlSlug`。
     - 保护措施：注意源码第 432、436 行中 Antd 表单实例名为 `formRef`（`form` 为普通 React state 对象，无 `getFieldValue` 方法），须判断 `Boolean(articleId) && Boolean(formRef.getFieldValue("urlSlug") || form.urlSlug)`，命中时跳过 `handleGenerateUrlSlug`，保留原固定短链。
   - **入口三：`handleAiInit`（第 2608-2724 行，抽屉内“AI 一键初始化/生成”）**：
     - 现状问题：第 2649-2654 行与第 2700-2707 行将表单 `status` 强制写回 `0`（未发布），并用 AI 新生成的 `urlSlug` 覆盖已有 `urlSlug`。
     - 保护措施：在已有文章编辑态下，`urlSlug` 输入框默认设为只读锁定态（右侧配“解锁修改”小按钮并弹二次确认警告：“修改短链会导致已分发的外部链接失效”）；未手动解锁时，`handleAiInit` 只回填摘要、标签、受众提示语等非标识字段，**保留原 `urlSlug` 和原 `status` 不变**。

---

### 2.4 用户端（`paicoding-ui`）展示与交互清单

#### 2.4.1 标题下方双态受众分流引导卡片与桌面版本选择器

涉及文件：`paicoding-ui/src/main/resources/templates/components/article/article-detail.html`（第 11-88 行）、`paicoding-ui/src/main/resources/static/css/views/column-detail.css` 与 `paicoding-ui/src/main/resources/static/css/views/article-detail.css`。

1. **展示条件与挂载位置陷阱规避（关键项）**：
   - 仅当 `readingContext != null && readingContext.versioned && #lists.size(readingContext.editions) > 1` 时渲染版本选择器与引导卡片；未启用多版本或仅有 1 个正式版的文章不输出多余节点。
   - **挂载位置要求**：核查 `article-detail.html` 第 20-75 行可见，原有 `.detail-content-title-other-wrap` 带有 `th:if="${author != null}"` 条件，而专栏阅读页 `views/column-detail/index.html` 第 81 行调用 `article_info(${vo.article}, null, ${vo.other}, ${vo.payUsers})` 时传入的 `author` 恒为 `null`，导致第 20-75 行在 `/react-vs-toolcall` 等专栏章节页不会渲染！因此桌面端版本下拉胶囊与 `#editionGuideBanner` **必须挂载在 `th:if="${author != null}"` 块之外的独立容器内**（位于第 18 行 `#postsTitle` 标题结束之后、第 78 行 `#highlightRelocation` 与第 82 行 `#articleContent` 之前）。
   - **双详情页样式覆盖**：普通文章详情页 `views/article-detail/index.html` 加载 `article-detail.css` 而非 `column-detail.css`，共用片段 `article-detail.html` 新增的版本胶囊与 `#editionGuideBanner` 样式需同步写入两套样式文件（或抽入公共组件样式）。
2. **桌面端版本下拉胶囊**：
   - 展示紧凑胶囊按钮：`[v2 重构实战版 · 最新 ▾]`（浏览历史版时显示 `[v1 初版 · 历史版本 ▾]`），附带当前所选版本的发布/修订日期（取 `lastPublishedAt`）。
   - 点击展开版本浮层列表：每项包含 `editionName`、`默认最新` 徽标、`audienceTag` 标签、发布日期与简短 `changeNote`，当前选中项高亮勾选。
3. **标题下方双态受众分流卡片 `#editionGuideBanner`**：
   - **默认版态（`!readingContext.viewingHistoryEdition`，如正在读 `v2`）**：
     - 视觉样式：清爽浅灰蓝/品牌浅绿左边框信息卡片（不喧宾夺主）。
     - 内容结构：左侧显示受众徽标 `${readingContext.guideCard.audienceTag}` 与引导文案 `${readingContext.guideCard.guideTip}`（若作者未填自定义 `guideTip` 且存在历史版 `v1`，展示默认引导文案：“当前为最新正式版。若你是零基础初学者，可先阅读初版建立核心直觉后再对照新版源码实操”）；右侧放置轻量切换按钮 `阅读 ${recommendedSwitchLabel} →`（绑定 `data-switch-edition="v1"`）。
   - **历史版态（`readingContext.viewingHistoryEdition`，如通过 `?v=v1` 进入或切到 `v1`）**：
     - 视觉样式：醒目的暖琥珀色提示条（`background: #fffbeb; border: 1px solid #f59e0b;`），在长文阅读时亦可在正文顶部清晰识别当前所处版本。
     - 内容结构：左侧显示“你正在阅读历史版本：**v1 初版（零基础直觉版）** · ${guideTip}”；右侧提供两个操作按钮——次要按钮 `复制本版直链`（一键复制 `https://paicoding.com/react-vs-toolcall?v=v1` 供答疑分享）与高对比主按钮 `返回最新 v2 版`。

#### 2.4.2 移动端吸顶胶囊适配

涉及文件：`paicoding-ui/src/main/resources/templates/views/column-detail/index.html`（第 43-77 行）与 `paicoding-ui/src/main/resources/static/css/views/column-detail.css`（第 1315-1317 行）。

1. `.for-menu` 工具条在 `>= 992px` 桌面端为 `display: none`，在 `< 992px` 移动端为顶部吸顶栏（当前包含搜索框、“专栏”抽屉按钮与“目录”抽屉按钮；“只看我的划线”开关 `components/comment/highlight-filter :: filter` 位于第 101 行 `#contentMenuCollapse` 抽屉内部）。
2. 当文章存在多个已发布版本时，在 `.for-menu` 右侧操作组新增移动端版本胶囊按钮 `#mobileEditionTrigger`（显示当前 `editionCode`，历史版态下胶囊带琥珀色圆点提示），点击拉起底部半屏选择面板（复用同一份 `readingContext.editions` 数据源），选择后关闭面板并调用统一的 `switchArticleEdition(editionCode)`。

#### 2.4.3 左侧专栏目录更新时间修正、多版徽标与上下篇导航

涉及文件：`paicoding-ui/src/main/resources/templates/views/column-detail/column-menu/index.html`（第 1-79 行）与 `paicoding-ui/src/main/resources/templates/components/article/article-detail.html`（第 90-157 行）。

1. **复用并对齐目录更新时间字段（`column-menu/index.html:35`）**：
   - 当前工作区第 35 行已改为 `'更新时间: ' + ${T(com.github.paicoding.forum.core.util.DateUtil).time2date(menu.updateTime)}`。配合 2.2.7 节 `ColumnArticleMapper.xml` 将 `updateTime` 口径升级为 `COALESCE(ae.last_published_at, a.update_time, a.create_time)`，前端模板无需新增时间字段即可展示当前正式版最近一次发布时间。
2. **多版本轻量标识**：当 `menu.editionCount != null && menu.editionCount > 1` 时，在目录章节标题右侧渲染一枚极简胶囊（如 `双版本`），告知读者该章同时提供初版与重构版；目录树依然只有这一条章节记录，不重复渲染两条目录。
3. **上下篇翻页与浮动按钮清理**：
   - `ColumnViewController#buildColumnArticleView` 第 409-421 行构造的 `ColumnArticleFlipDTO` 已有前后章 URL，可在 `#articleContent` 底部、评论区上方渲染清晰的“上一章 / 下一章”双栏卡片（跨章跳转始终进入目标章节默认最新版，不透传当前章节私有的 `?v=` 参数）；
   - 顺手清理 `article-detail.html` 第 90-157 行悬浮按钮样式中残留的旧 `other.flip.nextShow` 动态偏移判断，统一固定悬浮间距。

#### 2.4.4 页内切版、`?v=` 同步、滚动隔离与 DOM 监听器安全重建

涉及文件：`paicoding-ui/src/main/resources/templates/views/column-detail/index.html`（第 300-360 行、第 890-1110 行）与 `paicoding-ui/src/main/resources/static/js/biz/highlightcomment.js`（第 1-20 行、第 673-805 行、第 1103-1259 行）。

1. **可变阅读上下文初始化（`views/column-detail/index.html`）**：
   - 在内联脚本初始化 `window.articleReadingContext`（由 Thymeleaf 输出 JSON，所有 `articleId`、`selectedEditionId`、`selectedDetailId`、`currentEditionId` 均为字符串）：
     - 若 SSR 首屏返回 `invalidParamFallback === true`（例如用户手输了不存在的 `?v=v99`），页面初始化时用 `history.replaceState` 移除非法的 `v` 参数并弹出轻提示“未找到指定历史版本，已展示当前最新版”。
2. **滚动位置按 `articleId + editionCode` 隔离（`views/column-detail/index.html:305-315`）**：
   - 现状：`const articleScrollProgressKey = columnProgressKey + ':scroll:' + String(articleId);`，导致初版读到 80% 切到重构版时按旧高度/比例跳到重构版后半截。
   - 改造：改为函数 `getArticleScrollProgressKey()`：当 `window.articleReadingContext.versioned` 为真时返回 `${columnProgressKey}:scroll:${articleId}:${window.articleReadingContext.selectedEditionCode}`，未启用多版本时保持原 key 兼容。
   - 切版前先保存旧版本的滚动偏移与未提交的评论框草稿（按 `editionCode` 暂存于页面内存 Map）；切版渲染完成后，若目标版本已有保存的滚动记录则恢复，否则平滑定位至 `#postsTitle`。
3. **带序号防竞态的切版函数 `switchArticleEdition(targetEditionCode, options)`**：
   - 递增 `const seq = ++window.articleReadingContext.requestSeq;`；按钮置加载态，请求 `GET /article/api/${articleId}/edition-view?v=${encodeURIComponent(targetEditionCode)}`。
   - 响应返回后检查 `if (seq !== window.articleReadingContext.requestSeq) return;`，确保用户快速连点 `v1 -> v2 -> v1` 时只应用最后一次响应。
   - 请求失败时保留原正文、原引导卡与原 URL 不变，弹出 `toastr.error` 提示。
   - 请求成功时按以下固定顺序执行**卸载与重挂载**：
     1. **清理划线与两路流式 AI 状态（`highlightcomment.js` 与 `ai-comment.js`）**：调用 `hideQuoteCommentSidebar()`（`highlightcomment.js` 第 673-681 行）关闭侧栏，清空 `selectionRange`、`toSaveSelection`、`relocatingComment`；调用 `invalidateHighlightAiRequest()`（`highlightcomment.js` 第 786-802 行）中止进行中的划线 AI 流式请求，并为评论区流式 AI 脚本 `paicoding-ui/src/main/resources/static/js/biz/ai-comment.js`（第 174-234 行 `startStream`）补充 `AbortController` 中止逻辑，防止旧版流式回调写入新版界面。
     2. **修复 `highlightcomment.js` 三处重复绑定隐患（必改项）**：核实 `highlightcomment.js` 源码发现三处缺少单次绑定守卫（不同于第 11 行的 `quoteSidebarGlobalEventsBound`）：一是第 189-215 行 `initUnderlineToSelection` 内第 206 行的 `smallScreen.addEventListener('change', placeFilter)`；二是第 1103-1141 行移动端 `initQuoteModalEvent()`（绑定 `#quoteCommentInputModal` 的 `'input'` 与 `#submitQuoteCommentModal` 的 `'click'`）；三是第 1142-1248 行桌面端 `initQuoteEvent()`（第 1197 行绑定 `#quoteCommentInput` 的 `'input'`，第 1209 行绑定 `#submitQuoteComment` 的 `'click'`）。必须为这三处分别增加幂等布尔守卫，使切版后重跑高亮挂载函数 `remountArticleHighlights(newHighlights)` 时只重新渲染正文中的 `<span data-highlight-segment class="selected-text-highlight">` 高亮节点（见第 277-280 行 `wrapHighlightSegment`）与正文容器选区监听，不重复叠加输入与提交监听器。
     3. **更新上下文与地址栏**：更新 `window.articleReadingContext`；若 `!options?.fromPopState`，构造新 URL（当 `targetEditionCode === currentEditionCode` 时删除 `searchParams` 中的 `v`，否则设置 `searchParams.set('v', targetEditionCode)`），调用 `window.history.replaceState`（或用户主动点切换时使用 `pushState` 配合 `window.addEventListener('popstate', ...)` 解析地址栏 `v` 参数回切）。
     4. **替换正文、引导卡、评论区并重建右侧 TOC 与代码高亮**：更新 `#postsTitle`、渲染 `#editionGuideBanner` 与移动端 `#mobileEditionTrigger`、更新 `#articleContent` HTML（若 `needPay` 为真同步更新付费遮罩）、重跑代码高亮与图片预览初始化、调用 `generateTocMenu()`（`index.html:1029-1110`，先清理旧 TOC 节点与旧 scroll 监听再挂载）重建 `#contentMenuCollapse`，替换 `#commentDiv` 首屏并重置评论分页状态。
     5. **修正 `updatePageTitle` 标题更新逻辑（`index.html:1009-1026`）**：现状第 1011 行读取 `$('.section.active .title-text').text()`（对应 `column-menu/index.html:28` 的 `.title-text`），但第 1014-1016 行误把首个分组标题 `.group-header .group-name` 当作专栏名称拼接且该函数未被调用；切版时改为依据 `readingContext` 返回的当前版本标题、历史版标识与真实专栏名更新 `document.title`。

#### 2.4.5 评论区“本版讨论 / 历史版本讨论”切换交互

涉及文件：`paicoding-ui/src/main/resources/templates/components/comment/comment-list.html`（第 4-160 行、第 255-275 行）、`paicoding-ui/src/main/resources/templates/views/column-detail/index.html`（第 783-824 行、第 892-941 行）与 `paicoding-ui/src/main/resources/static/js/biz/ai-comment.js`（第 174-234 行）。

1. **讨论范围切换栏**：当 `readingContext.versioned` 为真时，在评论输入框下方、评论列表上方渲染范围切换器：
   - `本版讨论 (${topCommentCount})`（默认选中，`scope = current`）
   - `历史版本讨论 (${historyTopCommentCount})`（始终展示入口，`scope = history`；当新版刚发布 `topCommentCount == 0 && historyTopCommentCount > 0` 时，在空列表处展示引导块：“本版本刚刚发布暂无留言，可点击查看历史版本的 ${historyTopCommentCount} 条讨论”）。
2. **全入口发言、分页与 AI 参数透传**：
   - 顶层评论提交 `bindCommentInputEvent()`（`index.html:783-824` `#commentBtn` 点击提交；注意第 815 行在函数体内执行了 `$(document).on("input propertychange", "#commentContent", ...)`，切版重绑前必须先 `.off` 或移至顶层单次绑定，防止监听器叠加）与子回复提交 `commentSubmit`（`index.html:892-941`）：请求体均附带 `editionId: window.articleReadingContext.selectedEditionId` 与 `articleDetailId: window.articleReadingContext.selectedDetailId`。回复历史讨论区里的已有旧线程时，后端按根评论自动继承原版本归属，提交后留在当前列表刷新该线程。
   - 评论分页加载 `loadTopCommentPage`（`comment-list.html:255-275`，请求 `/comment/api/listPageHtml`）：透传当前 `editionId` 与当前选中的讨论范围 `scope`（`current` / `history`）。
   - 划线评论提交（`highlightcomment.js`）及评论区流式 AI 追问 `startStream`（`ai-comment.js:174-234`，请求 `/comment/api/commentAiStream`）：统一透传 `editionId` 与 `articleDetailId`。
3. **历史讨论列表增强**：在 `scope = history` 模式下，`comment-list.html` 为每条顶层评论渲染来源徽章（有 `editionName` 显示 `[${comment.editionName}]`，`editionId == null` 显示 `[早期讨论]`）；若某条评论关联的 `editionCode` 非空且不等于当前阅读的 `selectedEditionCode`，渲染操作链接 `查看对应版本正文`，点击后调用 `switchArticleEdition(comment.editionCode)` 并在切版完成后滚动高亮 `#comment-${comment.commentId}`。
4. **`#comment-{id}` 锚点自动识别**：页面初次加载若 URL hash 匹配 `#comment-(\d+)`，且首屏本版 DOM 中未找到对应节点，自动请求 `GET /comment/api/context?commentId=${id}`：
   - 若该评论属于另一已发布正式版（如用户在 `v2` 打开了指向 `v1` 评论的通知链接），提示或自动切换至对应版本并挂载该评论线程后滚动高亮；
   - 若该评论属于 `editionId == null` 的早期讨论，则自动切换到“历史版本讨论”Tab 插入该线程并滚动高亮。

---

### 2.5 存量 `/react-vs-toolcall` 混合正文拆分迁移 SOP 与验收用例清单

#### 2.5.1 `/react-vs-toolcall` 存量拆分八步安全迁移 SOP

**重要前提**：只针对需要拆分版本的目标文章按篇执行灰度启用，**严禁编写全站批处理脚本把所有存量文章的历史 `article_detail` 行自动批量转为公开版本**。

1. **步骤一：只读盘点线上数据**
   - 在生产从库或只读连接执行查询，确认 `url_slug = 'react-vs-toolcall'` 的唯一 `article.id`、`user_id`、`status`、`column_article` 关联信息。
   - 查询 `SELECT id, version, deleted, LENGTH(content) AS len, MD5(content) AS md5, create_time, update_time FROM article_detail WHERE article_id = ? ORDER BY version ASC;`，核查库内是否仍保留混合前的纯净初版快照。
   - 查询 `SELECT id, top_comment_id, parent_comment_id, highlight_info, create_time, LEFT(content, 60) FROM comment WHERE article_id = ? AND deleted = 0 ORDER BY id ASC;`，导出顶层评论与划线样本。
2. **步骤二：备份当前线上混合态**
   - 将当前线上正在展示的混合正文快照导出为离线 `.md` 备份文件，同时记录原 `article` 行全部字段值，确保随时可无损还原。
3. **步骤三：定稿 v1 与 v2 两份独立干净的 Markdown 正文与引导元数据**
   - 由作者确认两份独立文稿（消除新旧混排段落）：
     - **v1**：`edition_code = 'v1'`，`edition_name = 'v1 初版（零基础直觉版）'`，`audience_tag = '适合零基础建立直觉'`，`guide_tip = '本版从零推导 ReAct 与 ToolCall 的核心概念与最小闭环，适合初学者先建立直觉；若需对照最新仓库工程代码实操，请切换至 v2 重构实战版。'`
     - **v2**：`edition_code = 'v2'`，`edition_name = 'v2 重构实战版'`，`audience_tag = '基于最新重构源码实战'`，`guide_tip = '本版基于最新仓库代码结构重写。零基础同学若觉得工程跨度较大，建议先切换到 v1 初版建立核心直觉后再回来实操。'`
4. **步骤四：存量评论与划线归属分拣（人工复核，拒绝盲猜）**
   - 以顶层评论（`top_comment_id = 0`）为单位分拣，同一线程下的子回复统一跟随顶层评论：
     - **划线评论（`highlight_info` 非空）**：用与前台一致的 Markdown 渲染文本对 `v1`、`v2` 两份独立文稿匹配 `selectedText` 与 `context`；仅在 `v1` 命中的归入 `v1`，仅在 `v2` 命中的归入 `v2`。
     - **普通评论**：有明确版本代码特征或确切重构上线时间分界的归入对应版本；仅凭文字和时间无法确切判断归属的旧留言，保持 `edition_id = NULL, article_detail_id = NULL`（前台展示为“早期讨论”），**禁止使用大模型猜测后静默改写归属**。
5. **步骤五：单篇事务初始化双版本与切换默认指针**
   - 在后台冻结该文章的旧编辑入口（或通过管理端专用迁移接口在 `article` 行锁事务内执行）：
     1. 追加插入干净的 `v1` 正文快照 `detail_v1`（写入 `first_published_at` 与 `metadata_snapshot`）和 `v2` 正文快照 `detail_v2`（原混合快照保留在 `article_detail` 表中不物理删除，但其 `first_published_at` 置空，不绑定任何 `article_edition.published_detail_id`）；
     2. 插入 `article_edition` 记录 `ed_v1`（`edition_no = 1, edition_code = 'v1', state = 1, published_detail_id = detail_v1.id`）与 `ed_v2`（`edition_no = 2, edition_code = 'v2', state = 1, published_detail_id = detail_v2.id`）；
     3. 按步骤四确认的映射表批量更新既有 `comment` 的 `edition_id` 与 `article_detail_id`（对于 `v1` 上由原片段继承的划线，若文本偏移变化，同步重算或通过作者重定位补齐 `anchoredDetailId`）；
     4. 更新 `article.current_edition_id = ed_v2.id`，同步 `v2` 标题与摘要至 `article` 主表。
6. **步骤六：触发单篇缓存失效与 ES 索引刷新**
   - 事务提交后刷新该文章缓存，触发 `ArticleSearchService#syncArticle(articleId)`，确认 ES 文档内容已更新为纯净的 `v2` 正文（不含已剥离的 `v1` 混合段落）。
7. **步骤七：线上双路径冒烟核验**
   - 分别用匿名隐身窗口、普通登录账号、知识星球会员账号访问 `https://paicoding.com/react-vs-toolcall` 与 `https://paicoding.com/react-vs-toolcall?v=v1`，核验正文、试看截断、canonical 标签、切版按钮与讨论区计数。
8. **步骤八：应急回滚预案**
   - 若上线初期发现正文或权限异常，只需单条 SQL 将 `UPDATE article SET current_edition_id = NULL WHERE id = ?;`（并恢复原混合快照为最大 `version` 或将 `current_edition_id` 指回备份快照版本）即可秒级回退；若上线后已产生新评论，不做删行回滚，采用前向修订发布修复。

#### 2.5.2 一期工程验收用例清单

| 编号 | 验收模块 | 操作步骤与输入条件 | 预期通过标准 |
| --- | --- | --- | --- |
| T01 | 默认短链与历史直链 SSR | 1. 访问 `/react-vs-toolcall`<br>2. 新开无痕窗口访问 `/react-vs-toolcall?v=v1`<br>3. 访问 `/react-vs-toolcall?v=not-exist` | 1. 首屏服务端渲染 `v2` 正文与默认版引导卡<br>2. 首屏服务端渲染 `v1` 正文与琥珀色历史版提示条，可复制该链接分发给球友<br>3. 自动回退展示 `v2` 正文，前端清理非法 `?v=not-exist` 并给出轻提示 |
| T02 | SEO Canonical 归一 | 检查 `/react-vs-toolcall` 与 `/react-vs-toolcall?v=v1` 页面源码中的 `<link rel="canonical">`、`og:url`、JSON-LD 及 `/sitemap.xml` | 两个地址的 canonical、`og:url`、JSON-LD 均严格为无参 `https://paicoding.com/react-vs-toolcall`；`sitemap.xml` 不出现 `?v=v1` |
| T03 | 页内切版与前端生命周期 | 在阅读页连续点击 `v2 -> v1 -> v2`；切版前在 `v1` 滚动至 70% 并打开划线侧栏或发起 AI 流式提问 | 地址栏在 `/react-vs-toolcall?v=v1` 与 `/react-vs-toolcall` 间准确同步；旧划线侧栏关闭、旧 AI 流中止；右侧 TOC 与代码高亮正常重建；点一次提交划线只发一次请求；`v1` 的 70% 滚动位置不会带偏 `v2`；阅读量不因切版递增 |
| T04 | 专栏阅读权限与试看截断 | 对设为“星球免费/付费试看 30%”的专栏章节，用未登录账号调用 `/react-vs-toolcall?v=v1` 及 `GET /article/api/{id}/edition-view?v=v1` | SSR 与切版 REST 接口均严格执行 30% 试看截断并返回解锁提示，无法通过切版 API 拿取全文，也无法通过枚举草稿 ID 读取未公开草稿 |
| T05 | 后台草稿隔离与发布事务 | 在 `paicoding-admin` 为 `v2` 修改正文点击“保存草稿”；随后再打开抽屉选择“发布为新正式大版本 v3”并发布 | 保存草稿后线上读者看到的标题、正文、左侧目录更新时间、ES 搜索结果均不变；正式发布 `v3` 后，线上默认版、目录时间与 ES 搜索结果一致更新为 `v3` |
| T06 | 后台固定 `url_slug` 防误覆写 | 在 `paicoding-admin` 编辑 `/react-vs-toolcall` 时依次执行：导入 `v2-new.md`、导入 Word 文档、点击抽屉“AI 初始化” | 表单中的 `urlSlug` 始终锁定保持 `react-vs-toolcall`，线上 `status` 不被重置为 `0` |
| T07 | 评论四路隔离与通知定位 | 在 `v1` 下发表普通评论与划线评论，切换到 `v2` 查看评论区、热门评论、正文划线；点击指向 `v1` 评论的通知链接 `#comment-{id}` | `v2` 默认本版讨论、热门评论与正文划线均不混入 `v1` 内容；切到“历史版本讨论”可看到 `[v1 初版]` 与 `[早期讨论]` 线程并可一键切到 `v1` 正文；`#comment-{id}` 能准确加载对应版本或历史线程并高亮定位 |
| T08 | AI 上下文历史快照冻结 | 在 `v1` 正文发起划线 AI 解释及 `@派聪明` 评论提问，流式返回中途在后台更新 `v2` 正文 | AI 回答依据 `v1` 的历史公开快照内容，生成的机器人回帖归属在 `v1` 线程下 |
| T09 | 存量普通文章回归兼容 | 访问、编辑、搜索、评论任意未启用多版本（`current_edition_id IS NULL`）的存量专栏文章与普通博文 | 不展示版本切换器与受众卡片，原有保存、搜索、划线、评论行为保持一致 |

---

## 3. 二期扩展参考（整本专栏目录拆分为多棵目录树时的 `column_version` 方案）

> **启用边界说明**：一期单章 `article_edition` 已解决同一目录结构下章节正文重构与新旧共存问题。仅当未来某本教程升级时，整本专栏的章节数量、分组层级、先后顺序发生大规模重排（需要为 v1 和 v2 分别展示两棵不同的左侧目录树）时，再按本章规格实施二期扩展。

### 3.1 二期表结构演进草案

1. **新建专栏版本表 `column_version`**：
   - 字段：`id bigint unsigned PK`、`column_id bigint unsigned NOT NULL`、`version_code varchar(32) NOT NULL`（如 `v1`、`v2`）、`version_name varchar(64) NOT NULL`、`audience_tag varchar(120)`、`guide_tip varchar(500)`、`readme_content longtext NULL`（各版本独立的专栏导读）、`state tinyint NOT NULL DEFAULT 0`（`0-草稿 1-已发布 2-归档`）、`deleted tinyint NOT NULL DEFAULT 0`、`published_time datetime NULL`、`create_time`、`update_time`。
   - 唯一约束：`uk_column_version_code (column_id, version_code)`。
2. **扩展 `column_info`**：新增 `default_version_id bigint unsigned NULL DEFAULT NULL COMMENT '当前默认公开的专栏版本ID'`。
3. **扩展 `column_article_group`**：新增 `version_id bigint unsigned NOT NULL DEFAULT 0` 及索引 `idx_column_version_group (column_id, version_id, parent_group_id, section)`。**特别注意**：`update_schema_241217.sql` 预置了全局 `id = 0` 的“未分组”哨兵行（`column_id = 0`），查询与克隆分组时必须保持 `id = 0` 哨兵兼容，不能将其误归属到单一版本。
4. **扩展 `column_article`**：
   - 新增 `version_id bigint unsigned NOT NULL DEFAULT 0 COMMENT '所属专栏版本ID'`；
   - 新增 `short_title varchar(120) NOT NULL DEFAULT '' COMMENT '该专栏版本下的目录覆盖短标题'`（解决共用同一基础 `article` 时不同版本目录标题互踩 `article.short_title` 的问题）；
   - 新增 `chapter_key varchar(64) NOT NULL DEFAULT '' COMMENT '跨版本同源章节逻辑映射键，用于同章跨专栏版本切换跳转'`；
   - 新增唯一约束 `uk_col_ver_article (column_id, version_id, article_id)` 与排序索引 `idx_col_ver_group_sec (column_id, version_id, group_id, section)`。

### 3.2 二期路由与后台发版规则

1. **路由规则**：
   - 专栏概览页：`/column/{columnSlug}` 渲染 `column_info.default_version_id` 对应的目录树与导读；`/column/{columnSlug}/v/{versionCode}` 渲染指定历史版本目录树。
   - 章节阅读页：无版本前缀章节按默认专栏版本装配左侧目录树与 `ColumnArticleFlipDTO` 上下篇；带版本上下文 `/column/{columnSlug}/v/{versionCode}/{articleSlug}`（或根短链携带专栏版本参数）按指定 `version_id` 装配该版本的目录树与上下篇，杜绝跨版本串页。
   - **重定向状态码约束**：凡涉及随 `default_version_id` 动态切换而变化的目标地址跳转，一律使用 **302 Found**，不使用浏览器永久缓存的 301，防止日后发布 v3 切换默认版后老读者浏览器仍跳往旧版。
2. **后台发版三步工作流**：
   - **一键克隆目录快照（`POST /admin/column/version/clone`）**：选定源版本（如 `v1`）创建草稿新版本（`v2`），在单事务中按父子拓扑顺序深拷贝 `column_article_group`（维护 `oldGroupId -> newGroupId` 映射表，`group_id = 0` 保持为 `0`），再拷贝 `column_article` 行指向新 `version_id` 与映射后的 `newGroupId`，保留原 `article_id` 与 `chapter_key`。如此未改动的公共前置章节（如环境搭建）在两棵目录树中共用同一篇基础文章，作者无需维护双份副本。
   - **按需派生新版文章或启用单章多版本**：对目录结构与前后衔接整章重写的章节，提供 `POST /admin/column/article/forkForVersion` 一键派生独立新文章（自动生成不冲突的新 `url_slug` 并保持相同 `chapter_key` 供跨版同章切换）；对仍需保留固定根短链（如 `/react-vs-toolcall`）的核心单章，复用一期已落地的 `article_edition`。
   - **一键切换默认专栏版本（`POST /admin/column/version/setDefault`）**：校验目标版本至少含 1 篇已发布章节，更新 `column_info.default_version_id`，重算专栏总章节数缓存。

### 3.3 二期必须排雷的源码高危点清单（经源码逐行核实）

若启动二期 `column_version` 改造，同一 `article_id` 将合法出现在 `column_article` 的多个 `version_id` 行中。以下源码位置存在无版本过滤或单行假设，实施二期时必须逐一改造，否则会引发线上异常或数据串改：

| 序号 | 文件路径与精确行号 | 现状实现缺陷 | 二期排雷改造要求 |
| --- | --- | --- | --- |
| 1 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ColumnSettingServiceImpl.java:550-573`<br>`saveColumnArticle(Long articleId, Long columnId)` | 被 `ArticleWriteServiceImpl#saveArticle`（第 110-113 行）在每次保存文章时调用；使用 `Wrappers.<ColumnArticleDO>lambdaQuery().eq(ColumnArticleDO::getArticleId, articleId)` 调 `getOne`，一旦同一文章挂载在 v1、v2 两个版本目录中，MyBatis-Plus 会抛出 `TooManyResultsException` 阻断文章保存 | 传入明确 `versionId`（未传时定位该专栏 `default_version_id`）；已存在于多版本目录中的共享文章编辑正文时，不盲目改动既有目录绑定关系 |
| 2 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ColumnSettingServiceImpl.java:577-609`<br>`saveColumnArticle(ColumnArticleReq req)` | 第 601-606 行将后台教程目录编辑的 `req.getShortTitle()` 覆写回全局 `article.short_title`，导致修改 v2 目录短标题会篡改 v1 目录展示 | 优先写入 `column_article.short_title`，`ColumnArticleMapper.xml#listColumnArticles` 按 `NULLIF(ca.short_title, '') -> NULLIF(a.short_title, '') -> a.title` 三级优先级取值 |
| 3 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/dao/ColumnArticleDao.java:40-48`<br>`selectColumnArticleByArticleId(Long articleId)` | 源码自留 `fixme` 注释，盲取 `list.get(0)` 第一条记录；多版本下无法确定命中草稿版还是默认正式版 | 关联 `column_info.default_version_id` 优先返回默认公开版本的 `ColumnArticleDO`，并提供带 `(articleId, versionId)` 的精确查询 |
| 4 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/dao/ColumnArticleDao.java:77-82`<br>`selectColumnArticle(Long columnId, Long articleId)` | 使用 `.eq(columnId).eq(articleId).one()`，同一专栏两个版本共用同一篇文章时必抛 `TooManyResultsException` | 全面替换为 `selectColumnArticle(Long columnId, Long versionId, Long articleId)` |
| 5 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/dao/ColumnArticleDao.java:98-105`<br>`updateColumnArticleSection(...)` | 按 `WHERE column_id = ? AND article_id = ?` 更新 `group_id` 和 `section`，缺少 `version_id` 条件；在后台拖拽 v2 章节顺序时会把线上 v1 同名共享章节的分组和序号一并改乱 | `WHERE` 子句强制增加 `AND version_id = #{versionId}` |
| 6 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ColumnSettingServiceImpl.java`<br>`batchPublishColumnArticles`（375-405）、`firstAddGroup`（429-442）、`moveColumnArticle`（876-918，第 899 行调 `selectColumnArticle`）、`autoUpdateColumnArticleSections`（925-934） | 分组初始化、拖拽排序与序号重排均只按 `columnId` 维度操作 | 全部透传 `versionId`，使各版本的分组树与 `section` 序号在各自版本空间内独立维护 |
| 7 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ArticleSettingServiceImpl.java:142-168`<br>`updateArticleUrlSlug` | 第 151 行调用无版本参数的 `columnArticleDao.selectColumnArticle(columnId, articleId)` | 改为按默认版本或 `list` 判存校验专栏归属，避免后台修改短链报错 |
| 8 | `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ColumnServiceImpl.java:116-133`<br>`buildColumnInfo`（第 126 行调用 `ColumnDao.java:54-58` 的 `columnDao.countColumnArticles`） | `columnDao.countColumnArticles(dto.getColumnId())` 未过滤版本，会把 v1 与 v2 的章节总数相加（如 20 章变成 40 章） | 专栏卡片与详情页统计默认按 `column_info.default_version_id` 计数 |
| 9 | `paicoding-web/src/main/java/com/github/paicoding/forum/web/front/article/rest/ColumnRestController.java:57-62`<br>`columnMenus` | 引用了模板名 `biz/column/menus`，但 `paicoding-ui` 下无 `biz` 目录、`paicoding-web/src/main/resources/templates/biz/column/` 下仅有 `list.html`，两模块均不存在 `biz/column/menus.html` | 二期若提供专栏目录局部切换接口，需复用真实存在的 `views/column-detail/column-menu/index` 片段或补齐对应模板，不可调用现存死接口 |
| 10 | 专栏卡片双模板重复定义：<br>`paicoding-ui/src/main/resources/templates/components/column/column-card.html`<br>`paicoding-web/src/main/resources/templates/biz/column/list.html` | 首屏与下拉分页加载分别维护了一套专栏卡片 HTML | 若在专栏列表卡片展示最新版本标识或章节统计文案，两处模板必须同步修改 |

---

## 4. 构建与验证工程约束

在本仓库实施上述 Java / Mapper / Thymeleaf 代码改动与编写 JUnit / Spock 测试时，必须遵守以下构建铁律（详见根目录 `AGENTS.md`）：

1. **强制使用 JDK 8 编译与测试**：本仓库基于 Spring Boot 2.7.1 与旧版 Lombok 注解处理器构建，当前机器默认环境变量可能指向 Java 21（会导致 Lombok `javac` 编译报错），且 Homebrew 包装的 `mvn` 启动器可能受 `jenv` 干扰回落至 Java 21。每次执行编译或单测时，必须显式将 JDK 1.8 置于 `PATH` 首位并调用 Maven `libexec` 入口：
   ```bash
   bash -lc 'JAVA8_HOME=$(/usr/libexec/java_home -v 1.8); export JAVA_HOME="$JAVA8_HOME"; export PATH="$JAVA8_HOME/bin:$PATH"; /opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn -pl paicoding-api,paicoding-core,paicoding-service,paicoding-ui,paicoding-web -DskipTests compile'
   ```
2. **禁止执行 `mvn clean`**：除非用户显式要求，严禁运行 `mvn clean`，以免触发全量清理后在错误 JDK 环境下破坏增量编译产物；优先按改动模块执行定向编译与定向单测，例如：
   ```bash
   bash -lc 'JAVA8_HOME=$(/usr/libexec/java_home -v 1.8); export JAVA_HOME="$JAVA8_HOME"; export PATH="$JAVA8_HOME/bin:$PATH"; /opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn -pl paicoding-web -Dtest=ArticleReadingContextTest,ArticleEditionWriteServiceTest test'
   ```
3. **管理后台前端仓库验证（`/Users/itwanger/Documents/GitHub/paicoding-admin`）**：修改 TSX 与 API 模块后，在 `paicoding-admin` 目录下执行项目的 TypeScript 类型检查与构建校验（如 `npm run lint:eslint` 或 `npx tsc --noEmit`），确认无类型不匹配与未定义引用。
