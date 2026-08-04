# Logging Guidelines

> 日志约定（提取自上游真实代码）。

---

## 框架与声明

- Lombok `@Slf4j` + logback，所有类统一 `@Slf4j`（如 `ArticleRestController`、`RabbitmqServiceImpl`）。
- 配置：`paicoding-web/src/main/resources/logback-spring.xml`
  - pattern 含 MDC：`%mdc{traceId}|%mdc{bizCode}`
  - 输出：STDOUT + 滚动文件 `${log.path}/pai-${log.env}.log`（按天+100MB 分割，保留 3 天）
  - **ERROR 级触发邮件告警**：appender `errorAlarm` → `core/util/AlarmUtil`。⚠️ 因此 `log.error` 别滥用——只用于真正需要人工介入的非预期异常。

## MDC / traceId

- `core/mdc/MdcDot.java` 注解：`@MdcDot(bizCode = "#articleId")` 打在 Controller 方法上（`ArticleRestController.recommend`），SpEL 取参数写入 MDC bizCode。
- traceId 由 `core/mdc/` 下工具链路生成，日志 pattern 自动携带。

## 级别惯例（从代码归纳）

| 级别 | 场景 | 例子 |
|------|------|------|
| debug | 高频流式内容，包 `log.isDebugEnabled()` | `DeepSeekChatServiceImpl`：`if (log.isDebugEnabled()) log.debug("DeepSeek返回内容: {}", ...)` |
| info | 关键业务动作 | `RabbitmqServiceImpl`：`log.info("Publish msg: {}", message)` |
| warn | 调用方问题、可自愈失败 | `ForumExceptionHandler`：`log.warn("illegal request! {}", ReqInfoContext.getReqInfo(), ex)`；`ArticleSearchSyncListener` 同步失败记 warn 不抛出 |
| error | 非预期异常（触发邮件告警） | `ForumExceptionHandler`：`log.error("unexpect error! {}", ReqInfoContext.getReqInfo(), ex)` |

- 占位符 `{}`，异常对象作最后一个参数；不拼接字符串。
- 不打敏感信息：密钥走 `.env`，`DynamicConfigContainer` 有脱敏逻辑（`DynamicConfigContainerRedactionTest`）。
