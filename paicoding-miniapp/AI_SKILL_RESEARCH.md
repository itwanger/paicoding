# 微信小程序 AI 能力调研记录

更新时间：2026-06-23 19:13 CST

## 结论

- PaiCoding 小程序端适合先接入微信小程序 AI 的“开发模式”：把文章搜索、文章详情这类明确、可读、无资金风险的能力拆成原子接口和原子组件。
- 当前已实现的 `skills/article-search` 覆盖“搜索文章”和“查看文章详情”，不把登录、头像上传、点赞、收藏放入 AI Skill。这样可以先验证内容检索价值，避免 AI 直接触发用户态写操作。
- 上线前不能只看静态规则通过。官方校验闭环要求继续完成开发者工具 preview 编译、原子接口 execute、原子组件 render、真机/渲染验证。
- 当前本机微信开发者工具 CLI 未登录、项目仍是 `touristappid`，所以 AI Skill 只能证明静态结构正确，不能证明真实 AppID 权限、执行和渲染通过。
- 2026-06-23 复核官方资料后，当前接入重点仍是 Skill/MCP 化：`mcp.json` 管单个原子接口契约，`SKILL.md` 管业务流程，`content` 返回当前事实与下一步动作，`structuredContent` 返回组件渲染数据；`app.json` 必须注册 `agent.skills` 和独立 `skills` 分包，`project.config.json` 必须把 `skills` 纳入打包。
- 2026-06-23 18:05 再次在线复核后，第一版仍不建议为了 AI 开发模式迁移后端：PaiCoding 已有 Spring Boot 内容接口，当前只需要把低风险只读能力封装成 Skill；CloudBase/mp-skills 可作为工具链或后续云函数扩展选项。
- 2026-06-23 18:23 再次在线复核，`wechat-miniprogram/ai-mode-skills` 仓库仍是微信 AI 开发模式辅助工具入口；仓库 README 当前列出的版本为 `wxa-skills-generate 0.1.20`、`wxa-skills-validate 0.1.18`、`wxa-skills-eval 0.1.18`。上线前应按 validate + eval 闭环采证，而不是只看本地静态结构。

## 官方资料

- 微信小程序 AI 开发模式辅助 Skills 工具集：`https://github.com/wechat-miniprogram/ai-mode-skills`
- 校验 Skill：`https://github.com/wechat-miniprogram/ai-mode-skills/blob/master/wxa-skills-validate/SKILL.md`
- 生成 / 校验 / 评测工作流：`wxa-skills-generate`、`wxa-skills-validate`、`wxa-skills-eval`
- CloudBase mp-skills 工具套件：`https://docs.cloudbase.net/mp-skill`
- 微信小程序 AI 开发解决方案：`https://docs.cloudbase.net/solutions/wechat-miniprogram-ai/`
- 小程序成长计划：`https://docs.cloudbase.net/ai/ai-inspire-plan`
- 微信官方 AI 开发模式文档入口：`https://developers.weixin.qq.com/`（从 CloudBase 解决方案页跳转）

## 2026-06-23 实时调研摘要

- `wechat-miniprogram/ai-mode-skills` 官方工具仓库说明工具链包含生成、校验和评测三段：`wxa-skills-generate`、`wxa-skills-validate`、`wxa-skills-eval`；其中 `validate` 的闭环包括静态校验、真机执行、渲染验证和交付文档，因此 PaiCoding 不能把静态 `errors=0` 当成上线完成。
- 2026-06-23 18:23 在线复核时，官方工具仓库 README 当前列出的版本为：`wxa-skills-generate 0.1.20`、`wxa-skills-validate 0.1.18`、`wxa-skills-eval 0.1.18`；仓库未发布 GitHub Releases，因此应以仓库说明和实际 checkout 的 `SKILL.md` / package 信息为准。
- 2026-06-23 18:23 在线复核官方仓库 issues，存在“已在管理后台开启 ai 开发模式，但是真机调用 agent 还是报 agent compile mode is disabled”的开放问题；这说明上线前必须保留真机执行证据，不能用后台开关截图替代 execute/render。
- 官方工具仓库列出的前置条件包括微信开发者工具已安装、已登录、使用 nightly 版本，并开启开发者工具服务端口；这解释了当前 `login=false` / `access_token missing` 为什么是外部门禁，而不是小程序代码本身可绕过的问题。
- 官方工具仓库把核心结构定义为 `skills/{skill}/apis/{name}.js` 原子接口、`skills/{skill}/components/{name}/index.{js,json,wxml,wxss}` 原子组件、`mcp.json` 契约源，以及 wx API 白名单；这与当前 `skills/article-search` 的目录和只读边界一致。
- CloudBase 小程序 AI 方案页给出的定位是把查询、预约、下单、支付等流程封装成 Skill，让微信 AI 调用；对 PaiCoding 第一版来说，应优先封装“文章查询/详情”这类低风险只读流程。
- CloudBase `mp-skills` 文档把小程序 AI 开发模式描述为“业务功能封装为 SKILL”，并列出 `SKILL.md`、`mcp.json`、`apis/`、`components/` 四类组成；这再次确认 PaiCoding 当前不需要重做小程序框架，只需要把现有业务接口封装为 Skill。
- CloudBase `mp-skills` 快速开始页对“已有小程序”给出的路径是进入项目、安装/创建 Skill、运行 `validate`，再做环境搭建和 `eval` 质量评估；PaiCoding 已是已有小程序 + 自有后端，因此 `setup` 云开发环境不是第一版上线必需项。
- 2026-06-23 再次在线复核 `wechat-miniprogram/ai-mode-skills` 和 CloudBase `mp-skills` 文档后，仍建议走“已有原生小程序 + 自定义 Skill”路线：CloudBase CLI 可以帮助创建、搜索、校验 Skill，但 PaiCoding 已有 Spring Boot API，不需要为了接入 AI 开发模式改成云函数后端。
- `mp-skills` 文档把小程序 AI 开发模式描述为把业务功能封装为 SKILL，包含 `SKILL.md`、`mcp.json`、`apis/`、`components/`；这与当前 `skills/article-search` 的目录结构一致。
- 小程序成长计划包含云开发资源与 AI Token/生图资源包，但 PaiCoding 当前后端是自有 Spring Boot，不强制迁移 CloudBase；除非后续要做云函数版 AI 原子接口或模型调用，现阶段只用 Skill 协议接入即可。
- 2026-06-23 19:13 再次在线复核 `wechat-miniprogram/ai-mode-skills` 与 CloudBase `mp-skills` 资料，工具链重点仍是“生成 / 校验 / 评测”：`wxa-skills-generate` 生成 `skills/{skill}`，`wxa-skills-validate` 做静态、真机执行、渲染验证和交付文档，`wxa-skills-eval` 做质量评测。PaiCoding 当前第一版只读 Skill 不需要迁移后端，但上线前必须用真实 AppID、已登录 DevTools 和预发域名补齐 preview、execute、render、eval 证据。

## 对 PaiCoding 的落地范围

### 已落地

- `app.json` 配置了 `lazyCodeLoading`、`agent.skills`、独立 `skills` 分包。
- `project.config.json` 将 `skills` 纳入打包 include，并忽略 `cli-agent-run` 校验产物。
- `skills/article-search/mcp.json` 作为契约源，声明原子接口、输入输出 schema、组件路径和关联页面。
- 原子接口不跨分包依赖主包工具，避免 AI Skill 独立运行时找不到主包模块。
- 原子组件提供独立的 `index.js/json/wxml/wxss`，用于渲染结构化文章结果。
- 原子接口返回的 `content` 使用“本次事实 + 下一步动作”描述；空结果会提示换关键词，不让 AI 用同一个关键词重复调用。
- 原子接口和 request 工具按官方排障口径输出 `[ai-mode]` 入参、请求前后、出口和 catch 日志，但不打印 token、openid、AppSecret。
- `structuredContent` 字段与 `mcp.json` schema 对齐，包含组件渲染需要的 `cover`、标题、摘要、作者、阅读数、点赞数、收藏数、评论数和阅读权限等字段。
- 原子组件已接入 `NotificationType.Overflow` 监听并输出 `[ai-mode] article-card overflow monitor=on` 基线日志，供官方 render 阶段判断是否发生裁剪。
- `scripts/test-miniapp-ai-boundary.js` 已加入默认 preflight，用自动化方式锁定只读边界：只允许 `searchArticles` / `getArticleDetail`，只允许 GET 访问搜索和详情接口，并禁止登录、头像、支付、写入 HTTP 方法和互动接口；AI 卡片打开普通详情页时会带 `from=ai-skill`，避免详情页仅因 AI 卡片跳转而自动创建登录会话。
- `scripts/test-miniapp-detail-page.js` 已加入默认 preflight，用行为测试证明 AI 卡片入口加载详情不自动登录，同时保留用户主动点赞/收藏时的登录保护。

### 暂不纳入第一版 Skill

- 头像授权与上传：涉及个人信息和 multipart 上传，不作为 AI 可调用能力。
- 登录、退出：属于会话控制，不作为 AI 原子接口。
- 点赞、收藏：属于用户态写操作，等内容检索 Skill 跑通后再评估是否开放。
- 管理后台能力：小程序端不暴露。

## 上线前 AI 验收门槛

- 微信开发者工具使用支持 AI 开发模式的 nightly 版。
- 开发者工具已登录，且“设置 -> 安全设置 -> 服务端口”已开启。
- `project.config.json` 使用真实小程序 AppID，且该 AppID 在小程序后台已开通 AI 能力/开发模式权限。
- 运行官方 `wxa-skills-validate`，并确认：
  - 静态规则 `errors === 0`。
  - preview 编译通过，不能停留在 CLI 未登录或 `access_token missing`。
  - 每个原子接口 execute 成功，`status === "ok"` 且 `invokeResult.isError !== true`。
  - 每个有组件的原子接口 render 成功，没有裁剪、空白、字段绑定错乱。
- 如果 execute/render 返回 `agent compile mode is disabled`，不要改代码绕过；先确认 AppID 是否有 AI 开发模式权限。
- `wxa-skills-eval` 建议作为上线前质量补充：validate 证明接口与渲染可执行，eval 再评估意图理解、轨迹和最终答案质量。第一版不把 eval 作为代码合并阻塞，但生产提审前应至少对“搜文章”和“打开详情”两类核心话术留一份评估报告。

## 当前验证状态

- 静态校验：已通过，最近一次结果为 `total=49 passed=49 failed=0 errors=0 warnings=0`。
- preview 编译：未通过。2026-06-23 14:58 使用官方 `validate.mjs` 重跑后，`buildStatus=fail`、`stage=compile`，失败原因是微信开发者工具需要重新登录：`access_token missing`。
- execute / render：未完成。当前本机微信开发者工具 CLI 登录态为 `login=false`，且缺真实 AppID 权限验证。
- 真机验证：未完成。需要用户提供/切换真实小程序 AppID，并登录微信开发者工具后再执行。

## 后续扩展建议

- 第一阶段只保留“文章搜索”和“文章详情”，用于证明微信 AI 可以稳定读取技术内容。
- 第二阶段再考虑“按标签聚合文章”“推荐相关专题”等只读能力。
- 第三阶段如需开放点赞/收藏，必须单独做用户确认、幂等、风控和隐私评估。
