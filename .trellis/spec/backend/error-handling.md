# Error Handling

> 统一响应封装与全局异常处理（提取自上游真实代码；异常信息防泄露加固为**上游近期提交** `d0e25206` "Hide public exception details"，已经 git 比对确认）。

---

## 统一响应：ResVo<T>

`paicoding-api/.../api/model/vo/ResVo.java`：所有 REST 接口返回 `ResVo<T>`，结构为 `{status: {code, msg}, result: T}`。

```java
return ResVo.ok(vo);                        // 成功
return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, articleId);  // 失败
```

## 错误码：StatusEnum

`paicoding-api/.../api/model/vo/constants/StatusEnum.java`，码规范 `业务_状态_code`（类头注释）：

- 业务段：100 全局 / 200 文章 / 300 评论 / 400 用户
- 状态段：借用 http 语义，4xx 调用方问题、5xx 服务问题
- 例：`ILLEGAL_ARGUMENTS(100_400_001, "参数异常")`、`ARTICLE_NOT_EXISTS(200_404_001, "文章不存在:%s")`——msg 里的 `%s` 由 `Status.newStatus(statusEnum, args)` 格式化填充。

新业务错误码按此分段扩展（如 AI 相关可新增 500 段——**建议，需确认**）。

## 业务异常：ForumException

`paicoding-api/.../api/model/exception/ForumException.java`：RuntimeException + 内嵌 `Status`。
抛出统一走工厂：`throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);`（`ExceptionUtil.java`）。

## 全局异常处理

`paicoding-web/.../web/global/ForumExceptionHandler.java`——**不是** `@ControllerAdvice`，而是实现 `HandlerExceptionResolver`（`@Order(-100)`），在 `QuickForumApplication.configureHandlerExceptionResolvers` 注册。要点：

- 按请求类型分流：`/**/api/**`、admin、Ajax、json Content-Type → 返回 `ResVo.fail(status)` JSON；页面请求 → `error/403|404|500` 视图。
- `ForumException` 的 msg 原样透出给用户；其他异常公开接口只回兜底文案 `"服务异常，请稍后重试"`，细节仅写日志（防信息泄露，见类头注释）；admin 路径（登录态之后）才回根因摘要 `ExceptionUtils.getRootCauseMessage(ex)`。
- 日志分级：参数/方法不匹配类异常记 `warn`，非预期异常记 `error` 并附 `ReqInfoContext.getReqInfo()`。
- 另有 `GlobalExceptionHandler.java` 处理兜底场景，二者并存。

## 参数校验

**上游没有** Bean Validation（全仓 web 层无 `@Valid`/`@Validated`）。校验方式是 Controller/Service 内手工判断 + 抛 `ForumException` 或返回 `ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS...)`。新代码跟随此风格（引入 JSR-303 属于风格变更，需单独决策）。

## 权限

`@Permission(role = UserRole.LOGIN/ADMIN)` 注解（`paicoding-core/.../core/permission/`），由 `web/hook/interceptor/GlobalViewInterceptor` 统一拦截；未登录返回 `StatusEnum.FORBID_NOTLOGIN`。
