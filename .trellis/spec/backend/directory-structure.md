# Directory Structure

> 模块分层与代码组织（提取自上游真实代码，非理想化设计）。
> 上游自带规范文档：`docs/约定.md`（Controller 分类、DAO/Service 方法命名），本文件与其一致并补充实际代码证据。

---

## Maven 模块与依赖方向

依赖方向单向：`web → service → core → api`（`pom.xml` 根模块声明 5 个子模块，`paicoding-ui` 为前端静态资源）。

| 模块 | 职责 | 证据 |
|------|------|------|
| `paicoding-api` | 纯模型层：DTO/VO/Req、枚举、事件、异常、上下文。**无 Spring Bean、无业务逻辑** | `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/{vo,enums,event,exception,context,entity}` |
| `paicoding-core` | 与业务无关的基础设施：Redis 封装、RabbitMQ 连接池、MDC、权限注解、工具类 | `core/cache/RedisClient.java`、`core/rabbitmq/RabbitmqUtil.java`、`core/permission/Permission.java`、`core/util/` |
| `paicoding-service` | 业务逻辑 + 数据访问，按业务域分包（article/comment/user/notify/chatai/...） | `paicoding-service/src/main/java/com/github/paicoding/forum/service/` |
| `paicoding-web` | Controller、全局异常、拦截器、启动类、全部配置文件 | `paicoding-web/src/main/java/com/github/paicoding/forum/web/`、`paicoding-web/src/main/resources*` |

启动类：`paicoding-web/.../web/QuickForumApplication.java`（`@EnableAsync @EnableScheduling @EnableCaching`，实现 `WebMvcConfigurer` 注册拦截器与异常处理器）。

## service 模块内部：每个业务域固定四层

以 `service/article/` 为例（所有业务域同构）：

```
service/article/
├── repository/
│   ├── entity/     # ArticleDO.java — MyBatis-Plus 实体，继承 BaseDO
│   ├── mapper/     # ArticleMapper.java — MyBatis-Plus BaseMapper 接口
│   ├── dao/        # ArticleDao.java — @Repository，extends ServiceImpl<Mapper, DO>
│   └── params/     # SearchArticleParams.java — 复杂查询参数对象
├── service/        # 接口：ArticleReadService.java, ArticleWriteService.java
│   ├── impl/       # 实现：ArticleReadServiceImpl.java（@Service）
│   └── search/     # 子域可再分包（ES 搜索）
├── conveter/       # 注意上游拼写就是 conveter（article 域）；comment 域是 converter
└── helper/         # 业务辅助类
```

## 模型放置与转换约定

- **DO**（数据库实体）：只存在于 `service` 模块 `repository/entity/`，命名 `XxxDO`，不出 service 层边界（例外：上游 Controller 偶尔直接引用 DO，如 `ArticleRestController` import `ArticleDO`，不建议效仿）。
- **DTO / Req / VO**：全部放 `paicoding-api` 的 `api/model/vo/<业务>/` 下（上游把 DTO 也放在 vo 包里，如 `api/model/vo/article/dto/ArticleDTO.java`、`api/model/vo/article/ArticlePostReq.java`）。
- **页面级 VO**：web 模块自己的 `front/<业务>/vo/`，如 `web/front/article/vo/ArticleDetailVo.java`。
- **转换**：两种并存
  - 手写静态方法类：`service/article/conveter/ArticleConverter.java`（`public static ArticleDO toArticleDo(ArticlePostReq req, Long author)`）
  - MapStruct：`service/article/conveter/ArticleStructMapper.java` 等（mapstruct 1.4.2.Final，根 `pom.xml:162`）
  - 新代码两种均可，同一业务域跟随已有风格。

## Controller 组织（web 模块）

- 前台：`web/front/<业务>/rest/`（返回 JSON）与 `web/front/<业务>/view/`（返回 Thymeleaf 视图），**rest 与 view 分开放**（`docs/约定.md` 1.1 节）。
- 后台：`web/admin/rest/`。
- 路由风格：REST 接口 path 为 `业务/api/xxx`，如 `ArticleRestController` 上的 `@RequestMapping(path = "article/api")` + `@GetMapping("/data/detail/{articleId}")`。
- 读写拆分：`ArticleRestController`（读+写混合）之外，列表类单独 `ArticleListRestController`；service 层读写拆分更严格（见下）。

## Service 接口命名

- 接口 + impl 子包：`ArticleReadService` / `ArticleWriteService` → `impl/ArticleReadServiceImpl`。
- 读写拆分是上游惯例：`ArticleReadService` vs `ArticleWriteService`、`CommentReadService` vs `CommentWriteService`。
- 方法命名（`docs/约定.md`）：Service 层用 `queryXxx/findXxx`，DAO 层用 `getXxx`（单条）/`listXxx`（多条）/`selectXxxByXxx`/`updateXxx`/`removeXxx`。
- 注入方式：上游混用 `@Autowired` 字段注入（Controller/Service 多数）与 `@Resource`（DAO 内注 Mapper），无构造器注入惯例。

## 跨业务域调用

直接注入其他域的 Service 接口（不经过中间层），如 `ArticleRestController` 同时注入 `ArticleReadService`、`UserService`、`UserFootService`；service 域之间同样直接互注（`UserFootServiceImpl` 注入 `notify` 域的 `RabbitmqService`）。
