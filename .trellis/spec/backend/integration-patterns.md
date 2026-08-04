# Integration Patterns (Redis / RabbitMQ / ES / 事件 / 异步)

> 中间件使用模式（提取自上游真实代码）。与二开路线 1（MQ 管道）、2（RAG 检索）直接相关。

## ⚠️ MongoDB：不存在（上游现状）

上游 README 技术栈罗列了 MongoDB，但**全仓无 Mongo 依赖与代码**（所有 pom.xml 与 Java 源码均无）。请勿被 README 误导。计数主链路实际是 **MySQL `user_foot` 表 + Redis**：`statistics/service/impl/CountServiceImpl.java` 构造注入 `UserFootDao` 查点赞/收藏计数，另注入 `stringRedisTemplate` 做缓存计数；本地部署不需要 Mongo。

---

## Redis

- 统一封装：`paicoding-core/.../core/cache/RedisClient.java`（约 500 行**静态方法**工具类，非 Bean）。
  - 启动时 `RedisClient.register(redisTemplate)` 注入 template；所有 key 自动加前缀 `pai_`（`KEY_PREFIX`）。
  - 值序列化：String 直存，对象走 `JsonUtil.toStr`（`valBytes()`）。
  - API 覆盖 string/hash/list/zset：`getStr/setStr/hGet/hIncr/hGetAll/lRange/expire/ttl...`
- key 命名惯例：点分层级 + 业务前缀，集中定义在常量类，如 `ChatConstants`（`service/chatai/constants/ChatConstants.java`）：
  ```java
  "chat.rates." + ai.name().toLowerCase() + "-" + LocalDate.now()   // 每日限流计数（hash）
  "chat.list."  + ai + "." + user                                    // 会话列表（hash）
  "chat.history." + ai + "." + user                                  // 聊天历史（list）
  ```
- 典型用法：限流计数 `RedisClient.hIncr(key, field, 1)` + 首次设置 `expire(key, 86400L)`（`AbsChatService.incrCnt`）；聊天历史直接存 Redis list（`ChatHistoryServiceImpl`），**不落 MySQL**。
- 配置：`resources-env/<env>/application-dal.yml` 的 `spring.redis.*`。

## RabbitMQ（现状：演示级实现，路线 1 需重点评估）

- **不用 Spring AMQP**。core 层自造原生客户端封装：
  - `core/rabbitmq/RabbitmqConnectionPool.java`（连接池，大小 `rabbitmq.pool_size`）
  - `core/rabbitmq/RabbitmqUtil.java`（每 host 一个 ConnectionFactory）
- 业务入口：`service/notify/service/RabbitmqService`（impl 在 `impl/RabbitmqServiceImpl.java`）：
  - 开关：`rabbitmq.switchFlag`（`resources-env/dev/application-rabbitmq.yml`，默认 `false` 即本地默认不启用）。
  - 发送：`publishMsg(exchange, BuiltinExchangeType.DIRECT, routingKey, jsonStr)`——声明持久化 exchange 后 basicPublish。
  - 调用方：`UserFootServiceImpl:128` 点赞时 `rabbitmqService.enabled()` 判断后发消息，否则走 Spring 事件降级。
  - 消费：`processConsumerMsg()` 为 `while(true)` 轮询 + 手动 ack，作者标注 `TODO 非常Low`；消息幂等**未实现**。
- 命名常量：`core/common/CommonConstants.java`：`EXCHANGE_NAME_DIRECT="direct.exchange"`、`QUERE_NAME_PRAISE="quere.praise"`、routingKey=`"praise"`。
- ⚠️ 路线 1 方向已定（细则待任务 1 PRD 确认后转正）：现有实现无重试/幂等/DLQ，新的 AI 摘要管道用 `spring-boot-starter-amqp`（@RabbitListener + 手动 ack + 死信重试），**独立声明交换机/队列，命名加 `ai.` 前缀与存量隔离，不动上游现有实现**；队列拓扑、重试与死信策略在任务 1 PRD 中定。

## Elasticsearch

- 客户端配置：`service/config/es/ElasticsearchConfig.java`（`RestHighLevelClient`，7.x 兼容模式连 8.x：`apiCompatibilityMode`）。
- 开关与连接：`resources-env/dev/application-dal.yml` `elasticsearch.*`：`open`（默认 false，未装 ES 可跑）、`article-index: paicoding_article_v1`、hosts/scheme/超时。
- 索引/字段常量：`service/constant/EsIndexConstant.java`、`EsFieldConstant.java`。
- 搜索实现：`service/article/service/search/impl/ArticleSearchServiceImpl.java`——ES 只做召回排序，展示数据回源 MySQL（类头注释）；`@Value("${elasticsearch.open:false}")` + `ObjectProvider<RestHighLevelClient>` 实现 ES 缺席降级；用低级 `Request`/`Response` 手拼 JSON DSL。
- 索引同步：`ArticleSearchSyncListener`——`@Async @EventListener(ArticleMsgEvent.class)` 监听文章事件同步/删除索引，失败仅 `log.warn` 不阻塞主流程。
- 来源确认（git 比对 `upstream/main`，fork 零独有提交）：ES 搜索链路全部是**上游提交**：`3ecd043e`（首页引入 ES，兼容未安装 ES）→ `5933d1cc`（全文检索完整实现，附设计文档 `docs/article-elasticsearch-search-plan.md`）。
- 上游 ES 能力现状（RAG 任务的起点基线）：
  - 纯 **BM25** 关键词检索（title/shortTitle/summary/content，IK 分词、字段加权）+ 高亮片段 snippet（`highlightSnippets`）。
  - 设计文档明确声明**不引入向量字段/embedding/kNN**——向量化、kNN 检索、LLM 生成均为路线 2 的新增边界。
  - 已有降级链路：`elasticsearch.open=false` 或 ES 异常时回退 MySQL like 查询——路线 2 要求的「检索失败降级关键词搜索」可直接复用此模式。
  - 历史数据重建接口：`/admin/article/search/rebuild`（小批次滚动 bulk）——向量索引回填可仿照。
- fork 基线：tag `upstream-base`（= `4b55260a`，当前 main 与 upstream/main 完全一致），今后增量一律以此为 diff 基线。

## Spring 事件（进程内异步的首选模式）

发布统一走 `SpringUtil.publishEvent(...)`：

```java
// ArticleWriteServiceImpl:210
SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.CREATE, article));
```

- 事件类在 `paicoding-api`：`api/model/event/ArticleMsgEvent.java`（泛型内容 + `ArticleEventEnum` CREATE/ONLINE/REVIEW/DELETE）、`NotifyMsgEvent`。
- 消费：`@Async + @EventListener` 组件，如 `ArticleSearchSyncListener`、`notify/service/impl/NotifyMsgListener`、`rank/service/listener/UserActivityListener`、`statistics/listener/UserStatisticEventListener`。
- `@EnableAsync` 在 `QuickForumApplication`；线程池配置见 `service/config/async/`。
- **路线 1 对接点**：「文章发布事件」已存在（`ArticleEventEnum.ONLINE`），AI 摘要管道可在 `ONLINE` 事件处桥接到 MQ。
