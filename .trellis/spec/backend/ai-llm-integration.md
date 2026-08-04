# AI / LLM Integration（派聪明多供应商抽象）

> 上游自带 AI 助手的供应商抽象。二开路线 1（摘要/标签）与路线 2（RAG 生成）**必须复用**这套抽象，不另起炉灶。

---

## 抽象结构

```
paicoding-api/api/model/enums/ai/AISourceEnum.java     # 供应商枚举（含能力标记）
paicoding-service/service/chatai/
├── ChatFacade.java                # 对外门面（web 层只调它）
├── service/
│   ├── ChatService.java           # 供应商接口：source() + doAnswer + doAsyncAnswer
│   ├── AbsChatService.java        # 模板类：限流计数/敏感词/历史上下文，子类只实现模型调用
│   ├── ChatServiceFactory.java    # 按 AISourceEnum 路由到具体实现
│   └── impl/{chatgpt,zhipu,xunfei,deepseek,doubao,ali,pai}/
├── constants/ChatConstants.java   # Redis key 定义
└── search/AiWebSearchService.java # 联网搜索（智谱 web_search / Coding MCP）
```

## 供应商枚举与路由

- `AISourceEnum`：`CHAT_GPT_3_5 / CHAT_GPT_4 / PAI_AI / XUN_FEI_AI / ZHI_PU_AI / ALI_AI / DEEP_SEEK / DOU_BAO_AI / ZHIPU_CODING`，通过方法覆写声明能力（如 `XUN_FEI_AI.syncSupport()=false`、`ZHI_PU_AI.asyncSupport()=true`）。
- `ChatServiceFactory`：构造器注入 `List<ChatService>`，按 `chatService.source()` 建 Map，`getChatService(aiSource)` 路由——新增供应商 = 新增一个 `@Service` 实现即插即用。

## 新增一个供应商的固定套路（以 DeepSeek 为例）

`service/chatai/service/impl/deepseek/`：
- `DeepSeekChatServiceImpl extends AbsChatService`：实现 `doAnswer`（同步）与 `doAsyncAnswer`（SSE 流式，`AbstractStreamListener` 的 onMsg/onClosed/onError 回调 → `consumer.accept(AiChatStatEnum.MID/END/ERROR, response)`）。
- `DeepSeekIntegration`：HTTP 细节（okhttp + JSON 手拼），配置从 yml/`DynamicConfigContainer` 读。

## 配置与密钥

- `resources-env/<env>/application-ai.yml`：`ai.source` 启用列表、`ai.maxNum.*` 限流额度、各供应商段（`chatgpt.conf.*`、`zhipu.*`、`deepseek`、`doubao`...），密钥全部 `${PAICODING_XXX_API_KEY:}` 占位。
- 密钥注入：仓库根 `.env`（`.env.example` 列全了 `PAICODING_OPENAI_API_KEY` 等），`DotenvUtil` 在 Spring 启动前载入。
- 代理：`net.proxy`（application-ai.yml），OpenAI 类供应商 `proxy: true` 走 SOCKS。

## 与二开路线的对接（建议，待确认）

- 自定义 OpenAI 兼容网关（sub2api）：`chatgpt.conf.CHAT_GPT_3_5.apiHost` 可直接指向网关地址，复用 chatgpt 实现即可，无需新增供应商。
- 摘要/标签生成属于「无会话的一次性调用」，`AbsChatService`/`ChatFacade` 绑定了用户限流、敏感词、历史上下文，不适配后台管道。**上游已有先例：`AiSeoServiceImpl`**（`service/article/service/impl/AiSeoServiceImpl.java`）：直接注入 `ZhipuIntegration`，拼 prompt 模板 → `new ChatItemVo().initQuestion(prompt)` → `zhipuIntegration.directReturn(0L, item)` + 超时包装（`AsyncUtil`，240s），要求模型返回严格 JSON 后解析落库。AI 摘要管道建议复用此模式。
- 注意：现有 `ArticleRestController.generateSummary` 接口**不是 AI**，实现是 `ArticleUtil.pickSummary(content)` 纯文本截取（`ArticleReadServiceImpl:95`）——路线 1 落地时可作为 LLM 失败的降级兼容。
