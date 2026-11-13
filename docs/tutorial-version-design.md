> 实现进展（2026-11-13）：参见 [实施说明](tutorial-version-implementation.md)。地址栏完全不变的约束已再次由用户确认；Antigravity 评估稿的 `?v=` 建议不采用。

# 教程版本设计：固定 URL、独立历史正文与讨论

状态：设计稿，未实施。日期：2026-11-13。

## 1. 范围与结论

同一教程文章始终使用同一个 article.id、article.url_slug。默认进入最新正式发布的版本；读者在原页面切换历史版本，地址栏不新增路径、query 或版本 hash。

建议复用 article_detail 保存正文修订，新增 article_edition 管理面向读者的正式版本。comment 增加版本归属及原始正文快照关联。column_article、收藏、点赞、阅读总数、购买记录继续关联原文章。

这里必须区分两种“版本”：

- 修订：修错字、修改一段文字、保存草稿，会产生内部正文快照。
- 正式版本：初版、重构版，由作者主动发布，出现在读者的版本菜单里。

例如：正式初版可先后对应内部修订 17、18；重构版可对应内部修订 19、20。内部 version=20 不意味着读者要看到“第 20 版”。

调研依据为本地工作区的 Liquibase、Java、Mapper 和 Thymeleaf/JS 源码。没有连接生产数据库，不声称已经确认这篇文章有多少条历史正文、哪些留言属于初版。本文中的新字段、接口均为建议。

## 2. 已核实的表结构与实际行为

初始结构来自 init_schema_221209.sql，后续以 000_initial_schema.xml 引用的结构变更为准。init_data_230415.sql 中也能搜到 CREATE TABLE，但相关内容实际是插入的教程正文中的 SQL 示例，不是这里的真实建表变更；不能将搜索命中的教学代码当作迁移执行结果。

| 表 | 已有关键字段/约束 | 对版本功能的影响 |
| --- | --- | --- |
| article | id、url_slug、title、short_title、summary、picture、status、read_type、创建/更新时间；slug 为普通索引 | 文章身份与唯一阅读入口保留；标题等目前只有当前值，没有历史快照 |
| article_detail | id、article_id、version、content、deleted；唯一键 (article_id, version) | 已有多修订存储基础，但没有正式发布版本名、修订发布状态或公开正文指针 |
| column_article | article_id、column_id、section、group_id、read_type、preview_percent | 保留原章节位置和访问规则，不插入第二个教程章节 |
| column_article_group | column_id、parent_group_id、title、section | 这是目录分组，不是版本组，不能挪作版本关系 |
| comment | article_id、top_comment_id、parent_comment_id、content、highlight_info、deleted | 普通评论、划线评论共用这张表；目前没有正文版本归属 |
| user_foot | document_id、document_type、read_stat、praise_stat、collection_stat；唯一键 (user_id, document_id, document_type) | 保持文章身份才能自然保留点赞、收藏与足迹；read_stat 不是版本学习记录 |
| article_pay_record | article_id、pay_user_id、pay_status 等 | 权益仍跟随原文章，不需要按版本重复解锁 |
| notify_msg | related_id、comment_id 等 | 可由评论反查所属版本；并非所有旧通知都保存了 comment_id |

相关增量：231018 将多张表的主键/文章 ID 扩到 bigint；241029 加阅读与支付；241217 加目录分组；251103 加 highlight_info；251104 将评论正文改为 TEXT；251105_1 加通知 comment_id；251110 加 article.url_slug；260525 加教程试看比例。

### 2.1 正文不是“每次更新都覆盖”

ArticleWriteServiceImpl.updateArticle() 根据传入文章 status 是否为 REVIEW 生成 review 参数；ArticleConverter.toArticleDo() 的 status 来自请求或 actionType 推断，并非直接读取库内旧状态。

ArticleDao.updateArticleContent() 有两条路径：

- update=true：ArticleDetailMapper.updateContent() 原地改最后一条 content，并将 version 加一。
- update=false：查询最后一条，version 加一、id 置空后插入新行。

因此正常发布更新可能已经留下多条正文；不能把“存在覆盖路径”说成“所有历史都不存在”。同样也不能把所有存量 detail 行都认定为可公开的已发布版本。

另一个迁移细节：插入分支复用了 latest 对象，未显式清空 createTime/updateTime。历史行时间是否真实反映保存时间，必须核实运行时填充逻辑和实际数据，不能直接按时间给评论分版。

### 2.2 阅读默认选择最大修订号

ArticleDao.queryArticleDetail() 读取文章元数据，再调用 findLatestDetail() 按 version 降序取第一条非删除正文。没有“当前正式发布正文”的指针。审核显示由 article.status 整体控制。

ArticleReadServiceImpl.queryArticleContentForAI() 同样直接取最新 detail。ArticleMapper.xml 的 articleSearchDocumentSelect 也使用 max(version) 选择正文；只改页面读取，搜索和 AI 仍会拿错内容。

### 2.3 固定 URL 和教程上下文已经存在

ArticleSlugViewController 按 article.url_slug 找文章，再根据 column_article 关系转入 ColumnViewController.articleByRootArticleSlug()。该方法在固定短地址下构造教程阅读页。

因此不需要新建 slug、复制 article，也不需要改现有 URL 路由。主要调整的是路由解析之后“选择哪份正文”。

### 2.4 评论不是一处查询

ColumnViewController.buildColumnArticleView() 分别查普通评论、一级评论总数、热门评论、划线评论。CommentRestController 还有分页、子回复、发帖后整块刷新、划线弹层、普通与划线 AI 流式回复入口。

CommentDao 当前均主要按 articleId 查询；热门评论来自 CommentMapper.getHotTopCommentId() 的回复数聚合。只修改一级评论列表，会让历史热门评论或旧划线仍混入新版。

### 2.5 划线已经有恢复机制，需要保留

HighlightDto 已包含 selectedText、textStart、textFingerprint、context、anchoredText、revision 等字段。CommentWriteServiceImpl.relocateHighlight() 保留原引用 selectedText，更新定位文本并做 revision/CAS 校验。前端会显示无法定位的划线，并支持作者重新定位。

这些能力可以解决同一正式版内的小修订，不应把所有旧版划线自动匹配到新版。

### 2.6 学习位置与时间展示也受影响

column-detail/index.html 按“专栏 + articleId”保存滚动位置，正文高度变了还可能按比例恢复。同页切版必须把 detailId 纳入 key，否则初版阅读到 80% 会把重构版带到不相关位置。

该页面为游客保存 readArticles/lastArticleId；登录用户沿用 user_foot。两者都不区分正式版。

ColumnArticleMapper.listColumnArticles 取 a.create_time，column-menu/index.html 却将 menu.createTime 标成“更新时间”。新增版本后必须提供明确的正式发布时间，不能继续借用创建时间。

## 3. 阅读体验

### 3.1 默认阅读

- 原 URL 首屏由服务端渲染最新已发布正式版，不依赖先加载空页再发请求。
- 标题信息区放一个紧凑的“重构版 · 最新 ▾”版本入口，附最近正式发布时间。
- 菜单列出版本名称、正式发布日期、一句话更新说明。只有一个版本时不展示空的切换器。
- 专栏目录仍是一条章节；显示当前正式版发布时间。目录分组、上一篇/下一篇都不按版本复制。

### 3.2 选择历史版

- 请求受权限控制的版本阅读接口，一次拿到所选正文、标题、评论首屏、计数及划线信息，成功后整体替换。
- 不调用 pushState/replaceState 修改 URL，也不加 ?version= 或 #v1。
- 显示“正在阅读初版”和“返回最新版”；切换到正文开头，或恢复这个快照自己已存的阅读位置。
- 刷新或重新打开普通文章链接，恢复最新正式版。版本选择不写入全局 cookie，不让另一个标签页受到影响。
- 加载失败保留原正文与当前版本标记，不出现“标记初版、内容新版”的半切换状态。
- 移动端使用紧凑的菜单/底部选择面板；保留现有正文目录和“只看我的划线”控件。

### 3.3 评论可见性

正文下方继续使用现有讨论区，增加“本版讨论 / 历史讨论”范围切换，两个入口显示同口径的一级讨论数量。历史入口始终可见；新版零留言时直接提供“查看此前讨论”。

- 默认正文与当前版讨论一致，热门评论也限制在本版。
- 历史讨论按原线程显示，标明所属版本，可按版本筛选，并提供“查看对应正文”。
- 点击对应正文先切版，再加载目标线程；不要求目标评论恰好在分页首屏。
- 当前版的讨论计数和全文章累计评论计数分别命名，不能把包含回复的总数与一级讨论数相加。
- 历史版仍可发表评论和回复。编辑已有评论只改内容，不改归属；回复继承顶级线程的正式版本。
- 点赞、收藏、阅读总数继续属于整篇文章，不会切一次版就重新开始累计。

### 3.4 通知与不变 URL

现有回复通知会生成 #comment-{id}，这是已有评论锚点，不是新增版本 URL。保留兼容：由 commentId 查询权限与版本，加载对应正文和线程后定位。普通打开原 URL 仍优先最新版。

如果新入口要求地址栏连评论 hash 也不增加，站内点击通知时可把 articleId + commentId 写入当前标签页 sessionStorage，跳到原文章地址后一次性消费；不使用跨标签共享的 localStorage。旧通知没有 commentId 时只能进入历史讨论入口，不能假装精确定位。

复制不带任何上下文的原 URL 无法指定某个历史版本，这是固定地址规则下的明确行为。第一期不引入历史版分享链接。

## 4. 建议的数据模型

### 4.1 article：文章身份与当前发布版

新增 current_edition_id BIGINT UNSIGNED NULL。NULL 表示尚未启用版本管理，兼容已有文章。启用后只根据此指针找默认正式版，不以 max(version) 推断。

article.title/short_title/summary/picture 继续作为当前发布版的列表展示值；只有正式发布或修订发布成功时同步。草稿编辑不得提前覆盖这些字段。url_slug、作者、权益、目录关系继续归文章所有。

### 4.2 article_detail：继续存正文修订

保留现有 id、article_id、version、content 和唯一键。建议增加：

| 字段 | 用途 |
| --- | --- |
| edition_id BIGINT UNSIGNED NULL | 归属哪个正式版本；旧数据未确认时为空 |
| metadata_snapshot TEXT NULL | 与该份正文同时保存的标题、短标题、摘要、封面等 JSON 快照；使用 TEXT 避免擅自要求数据库原生 JSON 版本 |
| first_published_at DATETIME NULL | 该修订首次公开的时间；为空的内部草稿不能从读者接口访问 |

启用版本管理后的正文修订采用追加式保存，不覆盖已发布、评论引用或历史归档的 detail 行。所有新行显式设置创建/更新时间，不复制旧快照时间。

保留 version 作为文章内单调递增的内部序号。取号需要在文章行锁保护下完成，不能直接使用现有并发不受保护的 max + 1。唯一键作为最后一道约束。前端所有 bigint ID 按字符串传递，避免大文章 ID 精度问题。

### 4.3 新增 article_edition：正式发布版本

| 字段 | 用途 |
| --- | --- |
| id BIGINT UNSIGNED | 主键 |
| article_id BIGINT UNSIGNED | 同一篇文章 |
| edition_no INT UNSIGNED | 正式版本序号；唯一键 (article_id, edition_no) |
| name VARCHAR(80) | 初版、重构版等读者可见名称 |
| change_note TEXT | 作者填写的更新说明 |
| published_detail_id BIGINT UNSIGNED NULL | 该正式版当前公开正文快照 |
| draft_detail_id BIGINT UNSIGNED NULL | 正在编辑、尚未发布的正文快照 |
| draft_status TINYINT | NONE / DRAFT / REVIEW / APPROVED；独立于正式版是否已公开，避免待审核草稿影响旧版阅读 |
| status TINYINT | DRAFT / PUBLISHED / WITHDRAWN；历史版仍是 PUBLISHED，由 article.current_edition_id 区分默认版 |
| first_published_at DATETIME NULL | 首次正式发布时间 |
| last_published_at DATETIME NULL | 最近一次修订发布的时间 |
| create_time / update_time | 管理时间，不充当正式发布时间 |

发布新大版本才增加 edition_no；修订当前版只推进 published_detail_id。已经有公开正文的版本保存草稿时，仍保留 PUBLISHED 状态和旧 published_detail_id，对外阅读不受影响。

article.current_edition_id 必须属于本文章且已发布；edition 的两个 detail 指针也必须匹配 article_id/edition_id。通过事务与服务校验保证，避免增加循环外键而偏离项目现有风格。

保留单独 edition 表的理由：直接把每条 detail 变成公开版本，会把草稿、修错字和重写混在一起；直接增加另一篇 article 则会拆散 URL、收藏、目录及权益。

### 4.4 comment：正式版归属 + 原始正文

新增 edition_id BIGINT UNSIGNED NULL、article_detail_id BIGINT UNSIGNED NULL。

- edition_id 决定讨论分组。每个线程内所有回复归属相同 edition，服务端从顶级评论推导，不相信客户端随意填写的 topCommentId/版本组合。
- article_detail_id 保存发言时所依据的公开正文快照；一级评论由请求中的阅读上下文确定，回复默认继承线程原始正文。第一期不提供跨修订上下文回复，以免同一线程的 AI 历史混用资料。
- 原评论 ID、父子关系和用户点赞不变。
- 对已发生重新定位的划线，highlight_info 增加 anchoredDetailId，表示当前锚点所属修订；保留 article_detail_id 表示最初正文。现有 highlight.revision 继续表示定位操作修订号，不用它代替文章版本。
- 重新定位只允许在同一正式版内。跨正式版的问题通过新评论引用旧讨论承接，不原地搬走历史线程。
- 所属版本已确认但精确修订无法确认时，允许只有 edition_id；两者都不确定时都为空，显示为“早期讨论（版本待确认）”。NULL 不表示最新版。

建议增加 (article_id, edition_id, deleted, top_comment_id, id) 支持本版一级列表；为按顶级线程懒加载补充 (top_comment_id, deleted, id)。历史聚合与热门查询在真实数据量上用 EXPLAIN 决定是否需要额外索引，不盲目一次加满。

### 4.5 学习记录

第一期保留 user_foot 原语义，不清空 read_stat。滚动位置 key 增加 editionId/detailId；旧 key 不恢复到新版。

如果后续做“你读过初版，但重构版还没看”，再新增按 (user_id, article_id, edition_id) 唯一的版本访问记录，记录 last_seen_detail_id/last_seen_at。不要把 user_foot.update_time 当作最后阅读时间，它也承载点赞、收藏等更新。页面访问只能称“看过”，不能据此标成“学习完成”。游客在本地进度对象中增加版本映射即可。

## 5. 写入、读取与一致性

### 5.1 作者操作

实际发布入口是独立仓库 `/Users/itwanger/Documents/GitHub/paicoding-admin` 的“文章管理 → 文章编辑”，路由为 `/article/edit/index`。不在读者网站新增作者工作台。

已核实管理端调用链：

- `src/views/article/edit/search/index.tsx` 顶部“更新/保存”调用 `handleSaveOrUpdate`。
- `src/views/article/edit/index.tsx:1260` 先合并待应用的图片尺寸变更，再打开右侧 Drawer；顶部按钮并不直接提交。
- 同文件的 Drawer 包含标题、摘要、封面、分类、标签、阅读权限、slug、状态等表单，由抽屉内按钮调用 `handleSubmit`。
- `handleSubmit` 读取 `contentRef.current`，校验表单后调用 `saveArticleApi`，成功后清理本地草稿并返回列表。
- `src/api/modules/article.ts` 的正文保存调用 `/article/save`（再由 API baseURL 加上实际前缀）；`/article/update` 是另一条元数据更新接口，不能因按钮叫“更新”就接错接口。
- 后端 `ArticleSettingRestController.save()` 最终进入 `ArticleWriteService.saveArticle()`。管理员编辑已有文章传 author=null，新版本实现必须保留文章原作者并使用当前管理员身份执行权限校验。

界面改动沿用这条流程：顶部保留导入、覆盖源文件、上传视频、转链和更新按钮，新增“版本管理”；在现有更新抽屉顶部增加“修订当前版 / 发布新版本”选项，默认修订当前版。选择新版本后才展示版本名称；更新说明与发布结果提示紧邻该选项。保留原元数据表单，抽屉增加“保存草稿”，主按钮随模式显示“更新当前版 / 发布新版本”。版本管理以只读历史列表/正文预览为主，不直接覆盖当前编辑内容。

发布新版本从当前编辑正文生成新 edition 草稿，填写版本名称/更新说明；发布修订留在原 edition。已在编辑器里完成重写时，切换发布方式不能重新加载线上正文、丢掉编辑内容。

管理端已经存在 debounce + localSet 的本地自动保存和草稿恢复。需要保留，并将草稿 key 扩展到 articleId + editionId/新版本草稿标识；“本地已自动保存”和“服务端草稿已保存”明确区分。服务端保存草稿不能通过把原 article.status 设为 OFFLINE 来实现。正式发布成功才清理对应草稿，失败保留内容；更换编辑对象、版本或返回列表时继续使用现有未保存保护。

原抽屉 AI 初始化会生成标题、摘要和 slug，并回填 status=0。版本化编辑必须保护固定 slug 和线上发布状态，不能将“初始化”产生的默认值直接写回原文章；仅在明确编辑新文章时保留原初始化语义。版本草稿、当前公开版和整篇文章上下线状态应分别处理。

启用版本管理的文章必须进入专用发布服务。不能继续不加分支地调用旧 saveArticle 更新 article.status/title/body，否则保存草稿会影响线上正文或审核状态。现有后台标题/摘要快捷更新也应进入同一修订发布规则；slug 修改能力不因本功能而自动触发。

审核对象为具体 draft_detail_id。审核通过后又保存新修订，必须重置 draft_status，不能继承旧快照的通过结果。原文章发布审核策略仍适用，只有当前配置允许免审的作者才能直接发布。

发布事务：

1. 校验作者权限、文章归属、草稿审核状态和 expectedPublishedDetailId，锁定 article/edition。
2. 校验草稿 detail 属于本版本，发布指针切到已审核快照；首次发布写 first_published_at，修订发布更新 last_published_at。
3. 新正式版发布时更新 article.current_edition_id；修订历史版不改变默认版。
4. 当前正式版发布时同步 article 标题、摘要、封面和更新时刻；保存草稿、修改历史版不更新当前文章的列表时间。
5. 事务提交后再失效相关缓存、同步搜索。当前 ArticleSearchSyncListener 是 Async + EventListener，新流程应保证 after-commit 派发，避免异步提前查到旧指针；失败可重试并按当前已发布指针重建。

撤回当前正式版时须同事务指定仍可公开的回退版；无可公开版则文章下线，不能留下悬空指针。首次上线将回退限制为后台操作，不在读者端扩展。

### 5.2 统一解析阅读上下文

建议集中生成 ArticleReadingContext(articleId, editionId, detailId, currentEditionId, accessMode)，供 SSR、版本接口、评论、AI 使用。

- 普通文章 URL：选择 article.current_edition_id → published_detail_id。
- 版本菜单：显式选择已发布 edition → 其 published_detail_id。
- 查看留言对应正文：由服务端从 commentId 解析原始公开 detail，而非任意暴露一个 detailId。
- 未启用版本管理文章走兼容路径；启用后不得回退到 max(version)。

历史快照必须曾经公开且属于用户可访问的文章；草稿不能通过猜 detailId 读取。内部未公开修订和已发布快照需要区分：为 detail 增加 first_published_at DATETIME NULL 作为公开资格标记，在发布事务中写入；旧快照只在迁移审核确认后补录。已撤回 edition 或整体下线文章仍不可公开读取。

教程访问控制复用 column_info.type、column_article.read_type、preview_percent 及星球状态；普通文章复用 ArticleReadViewServiceExtend 的访问规则。当前教程截断位于 ColumnViewController 私有方法，新 API 必须提取共享策略，不能直接返回 DAO 全文或复用缺少教程截断的通用 detail API。

### 5.3 版本接口草案

- GET /article/api/{articleId}/editions：已发布版本元信息，不返回所有正文。
- GET /article/api/{articleId}/edition-view?editionId=...：权限处理后的阅读数据/Thymeleaf 片段，含快照 ID、标题、正文、当前版讨论首屏、计数与划线数据。
- GET /comment/api/context?commentId=...：校验文章和访问权限，解析线程、版本与公开快照，并获取目标线程，不依赖用户顺序翻页。
- 现有列表/发帖/划线/AI 请求补充 editionId、articleDetailId；服务端验证后使用，旧客户端兼容需要区分未版本化文章与版本化文章，不能默默将旧版发言归到发布时的最新版。

这些是后台 API 参数，不改变浏览器文章 URL。

### 5.4 评论与 AI

普通列表、一级总数、热门、划线、分页、发帖后的整块刷新都必须共享同一 edition 范围。子回复由顶级评论推导范围并校验文章一致性。全文章累计统计不改原含义。

普通/划线两套流式接口，以及 CommentWriteServiceImpl 的异步机器人都要使用评论的原始公开 detail；不能继续调用只接收 articleId 的 queryArticleContentForAI。没有确定原始快照的早期讨论，不以最新版冒充当时正文，可只基于引用与线程回答，并明确缺少原文上下文。

AI 请求开始时冻结阅读上下文，机器人回帖继承原线程归属。发布或切版发生在流式生成期间，均不能改变这次回答所依据的正文。

### 5.5 前端生命周期

现有页面有 const articleId、全局 articleHighlights、划线草稿、侧栏状态、AI 请求和多个事件监听。文章 ID 不变，但需要一个可更新的 readingContext 和可复用的挂载/清理流程。

切版时保存当前快照滚动位置；保存未提交评论草稿到对应版本；取消选区与重新定位状态，关闭旧划线侧栏；隔离/中止旧请求的 UI 回调；随后更新正文、目录、代码高亮、划线与评论区。旧 AI 后台任务若仍完成，只能回写原线程，不得插入当前版本 DOM。

为请求附带切换序号，只接受最后一次选择的响应。销毁旧监听、计时器/观察器再重新挂载，避免 repeated initUnderlineToSelection 累积监听。切版接口不调用带阅读加一副作用的 queryFullArticleInfo，不把每次切换算成新的整篇文章浏览。

### 5.6 搜索、SEO 和时间

ArticleMapper.xml 的搜索正文来源改为当前正式版 published_detail_id；ES 单篇同步、全量重建和数据库降级搜索共享这一来源，不索引草稿/历史全文。保留一个 articleId 对应一个搜索文档。

SSR 的 canonical、og:url、JSON-LD URL 继续使用原地址。SEO 的标题/摘要/正文/修改日期取当前已发布快照与发布时间；历史切换不更改分享目标。

目录 DTO 增加 publishedAt/modifiedAt，修正“更新时间显示 createTime”。旧文章未启用时可明确标成“创建时间”，不能伪造新版本的发布时间。

## 6. react-vs-toolcall 的迁移

先做只读数据盘点，建立“待归档初版正文、新版正文、评论归属候选”报告，再启用这篇文章。不要全站自动把存量 detail 全发布。

1. 按 slug 找实际 articleId，检查 slug 是否唯一、是否存在预期 column_article 关系。
2. 列出该文章 detail 的 id/version/deleted/时间/正文长度/摘要哈希，定向比较需要的修订，确认完整旧版是否仍在库里。
3. 核实每次公开发布的证据。内部修订号、记录时间都不能单独证明发布历史；标题元数据过去未留快照的部分需从备份或作者原稿补齐，否则明确标注未知。
4. 如果当前正文拼接了新旧两版，先从完整原稿或可靠快照恢复两份独立正文，保留迁移前混合正文作为内部备份。
5. 评论以顶级线程为单位归属，后续回复继承。普通留言仅靠文字/时间无法可靠归属的，保留“早期讨论”；不让 AI 自动猜测后静默迁移。
6. 划线使用 selectedText、textFingerprint、context 校验候选正文；因指纹基于 DOM，需采用现有渲染与文本索引口径。已经重新定位的 anchoredText 不能当作最初版本证据。
7. 建立两版、公开快照、评论映射后，在事务内切换 current_edition_id。切换前暂停该文章的旧编辑写入或使用统一文章锁，防止盘点后正文再次变化；复核正文哈希与预期一致。继续使用原 articleId 和 slug，原 commentId/父子关系完全保留。
8. 发布前保存迁移映射和原字段值；失败按映射回滚指针及关联。上线后若已有新评论，不通过删除新记录简单回滚，使用前向修复。

本地仅能确认结构和代码能力。生产正文是否完整、真实发布日期与评论分界是上线前待核实项，不是本设计已经验证的事实。

## 7. 实施顺序与验收

第一期包含：单篇文章灰度启用、发布版本/修订、原 URL 切换、历史讨论、划线隔离、AI 上下文、访问权限、搜索/SEO 与滚动位置适配。新版优先依靠默认读取当前发布版，暂不额外开发用户通知推送、全文 diff 或版本学习仪表盘。

第二期再做：对读过旧版的用户提示“有更新”、版本对比、更新订阅。前提是建立独立版本访问记录，不修改原收藏/阅读足迹含义。

关键验收：

- 页面初开/刷新为最新版，切换前后 URL 字节一致；既有评论锚点兼容单独验证。
- 保存新草稿后线上标题、正文、目录日期、ES 搜索结果均不改变；正式发布后一致切换。
- 本版列表/热门/划线/计数/懒加载范围一致；回复旧线程、AI 完成回帖后仍留在旧版。
- 小修订后的失效划线可重新定位；跨正式版不自动搬移；可查看最初公开正文。
- 连续快速切版、网络失败、带未提交草稿切版、AI 流中切版均不串内容、不丢草稿、不重复绑定。
- 匿名/登录/星球/作者访问历史与最新版遵守同样的现有权益策略，猜测草稿 ID 无法读到草稿。
- 阅读旧版不覆盖新版滚动位置；切版不重复累计文章阅读量。
- 旧通知目标不在首屏或是子回复时，仍能取到并展示原线程；没有 commentId 的通知有明确降级。
- 迁移的未知评论可见，不能全部误归初版或最新版；未启用文章保持兼容。
- 并发保存、重复发布和审核拒绝不造成版本号冲突、线上指针指向草稿或旧版被覆盖。

实现后的 Java 检查须按 AGENTS.md 使用 Java 8 与 Maven libexec，执行受影响模块的针对性测试，不运行 mvn clean。本次只产出设计，没有改运行代码、执行 SQL 或运行构建。

## 8. 源码定位

- paicoding-web/src/main/resources/liquibase/changelog/000_initial_schema.xml
- paicoding-web/src/main/resources/liquibase/data/init_schema_221209.sql
- paicoding-web/src/main/resources/liquibase/data/init_data_230415.sql
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/dao/ArticleDao.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/mapper/ArticleDetailMapper.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ArticleWriteServiceImpl.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/conveter/ArticleConverter.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ArticleReadServiceImpl.java
- paicoding-service/src/main/resources/mapper/ArticleMapper.xml
- paicoding-service/src/main/resources/mapper/ColumnArticleMapper.xml
- paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/search/ArticleSearchSyncListener.java
- paicoding-web/src/main/java/com/github/paicoding/forum/web/front/article/view/ArticleSlugViewController.java
- paicoding-web/src/main/java/com/github/paicoding/forum/web/front/article/view/ColumnViewController.java
- paicoding-web/src/main/java/com/github/paicoding/forum/web/front/comment/rest/CommentRestController.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/comment/repository/dao/CommentDao.java
- paicoding-service/src/main/resources/mapper/CommentMapper.xml
- paicoding-service/src/main/java/com/github/paicoding/forum/service/comment/service/impl/CommentWriteServiceImpl.java
- paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/comment/dto/HighlightDto.java
- paicoding-service/src/main/java/com/github/paicoding/forum/service/notify/service/impl/NotifyMsgListener.java
- paicoding-ui/src/main/resources/templates/views/column-detail/index.html
- paicoding-ui/src/main/resources/templates/views/column-detail/column-menu/index.html
- paicoding-ui/src/main/resources/static/js/biz/highlightcomment.js
- paicoding-ui/src/main/resources/templates/components/comment/comment-list.html
- paicoding-ui/src/main/resources/templates/components/notice/notify-reply-item.html
- paicoding-web/src/main/java/com/github/paicoding/forum/web/global/SeoInjectService.java
