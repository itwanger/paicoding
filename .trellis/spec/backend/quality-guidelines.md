# Quality Guidelines

> 代码风格、测试现状与注意事项（提取自上游真实代码，区分「上游现状」与「建议」）。

---

## 命名与风格（上游现状）

- 上游自带规范：`docs/约定.md`（Controller rest/view 分包、DAO `get/list/select/update/remove` 前缀、Service `query/find` 前缀）。
- Lombok 全面使用：`@Data`、`@Slf4j`、`@Getter`；实体 `@EqualsAndHashCode(callSuper = true)`。
- 类头 javadoc 带 `@author` + `@date`（如 `ArticleDao`：`@author louzai @date 2022-07-18`）。
- 注释中文为主，业务枚举字段用 `@see XxxEnum` 关联。
- 依赖注入：`@Autowired` 字段注入为主，`@Resource` 次之；无构造器注入惯例——新代码跟随。
- 工具类静态方法风格：`RedisClient`、`JsonUtil`、`SpringUtil`（`SpringUtil.getBean()/getConfig()/publishEvent()` 是取 Bean/配置/发事件的统一入口，`core/util/SpringUtil.java`）。

## 测试现状（如实记录）

- 测试全部集中在 `paicoding-web/src/test/`（56 个文件），多为**手工验证型/集成型**：`BasicTest` 基类 = `@SpringBootTest(classes = QuickForumApplication.class)` + JUnit4 `SpringJUnit4ClassRunner`，需真实环境（DB 等）才能跑。
- 少量纯单测：`UrlSlugUtilArticlePathTest`、`SlugGeneratorTest`、`DynamicConfigContainerRedactionTest`。
- **无 CI 强制测试、无覆盖率要求、service/core 模块无 test 目录**。
- 建议（非上游现状）：新增代码的测试放 `paicoding-web/src/test/`，纯逻辑优先写不依赖 Spring 上下文的单测；依赖中间件的验证以冒烟脚本/手工清单记录。

## 上游代码里的已知反模式（勿效仿）

以下在上游代码中真实存在，但新代码不要复制：

1. **吞异常**：`RabbitmqServiceImpl.processConsumerMsg` 中 `catch (Exception e) {}` 空 catch、`e.printStackTrace()`。
2. **轮询式 MQ 消费**：同文件 `while(true) + sleep(10000)`，作者自己标注 `TODO: 这种方式非常 Low`。
3. **Controller 直接引用 DO**：`ArticleRestController` import `ArticleDO`，破坏分层。
4. **静态可变常量**：`CommonConstants.EXCHANGE_NAME_DIRECT` 等为 `public static String`（非 final）。
5. 拼写遗留：`conveter` 包名、`QUERE_NAME_PRAISE`、MQ 配置键 `passport`（实为 password）——已有代码不改，新代码不沿用错拼。

## 禁止事项（建议，待确认）

- 不绕过 `ResVo`/`StatusEnum` 自造响应结构。
- 不绕过 `RedisClient` 直接注入 `RedisTemplate`（key 前缀 `pai_` 由 `RedisClient` 统一加）。
- 不引入与现有栈重复的库（json 已有 fastjson/jackson 并存、http 已有 okhttp/hutool，新增依赖前先找现成）。
- `log.error` 会触发邮件告警（`AlarmUtil`），仅用于需人工介入的场景。
