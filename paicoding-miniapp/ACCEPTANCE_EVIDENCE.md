# 微信小程序上线前验收证据

本文件用于上线前记录真实 AppID、真实设备和微信 AI 能力的验收结果。不要写入 AppSecret、token、Cookie、手机号、用户 openid 等敏感值。

## 基本信息

- 验收日期：2026-06-23 19:58 CST
- 验收人：Codex 本地自动化验证
- 小程序 AppID 后 6 位：
- 后端环境：本地 dev；预发 / 生产未验证
- API 域名：小程序 develop 默认 `http://127.0.0.1:8080`；本地 API smoke 曾用备用端口 `http://127.0.0.1:18080` 验证；预发 / 生产未验证
- 微信开发者工具版本：CLI 可执行但未登录，`islogin` 返回 `login=false`
- 真机设备与微信版本：

## 自动检查

| 检查项 | 命令或证据 | 结果 |
| --- | --- | --- |
| 默认 preflight | `scripts/preflight-miniapp.sh` | 2026-06-23 19:54 通过；Java 8 小程序专项测试 62 条通过，且包含详情页登录边界、详情缺失兜底、详情小程序码 `scene` 入口、文章图片预览、评论列表、一级评论、评论回复、子回复分页展开、评论点赞/取消点赞、删除自己的评论/回复、拒绝客户端 `commentId` 更新旧评论、拒绝跨文章回复、拒绝跨文章拉取子回复、拒绝用子评论作为顶级评论拉取回复、AI 入口不被全局启动自动登录、收藏登录与刷新、我的收藏、浏览历史、全页面原生下拉刷新、点赞连点保护、登录后头像昵称完善提示、默认随机头像不算微信头像授权完成、我的页资料未完善状态、退出登录清理资料提示、隐私授权状态、微信隐私 pending resolver 回写、隐私协议入口、搜索提示空结果兜底、搜索输入 64 字符端侧边界、最近搜索、联想直达详情、搜索联想 timer 清理、搜索旧回包隔离、首页分类旧回包隔离、分类 ID 兜底、分类空元素兜底、小程序设备头解析、资料页头像和简介边界、简介清空、可信头像域名边界、搜索关键词后端兜底、首页/搜索页行为、AI 卡片空态、API 地址一致性、本地 E2E 启动脚本、动态配置日志脱敏、release gate 顺序、真实 AppID 格式门禁、readiness 核心门禁、隐私授权静态门禁、证据文件门禁、AI validate 安全门禁、AI 证据生成器、真机证据生成器和字节码 descriptor 回归检查 |
| 详情页登录边界 | `scripts/test-miniapp-detail-page.js` | 通过；AI Skill 卡片入口加载详情不自动登录，普通详情入口会登录，AI 入口下用户主动点赞仍会触发登录保护；详情页支持 `id`、`articleId` 和小程序码 `scene=123` / `scene=id%3D123` / `scene=articleId%3D123` / `scene=a%3D123` 文章入口；详情页原生下拉刷新会重拉文章详情和评论首页并停止刷新态 |
| 文章图片预览 | `paicoding-miniapp/pages/detail/detail.*`；`scripts/test-miniapp-detail-page.js` | 通过；详情页会从正文 HTML 提取 HTTPS 图片，去重后展示为文中图片缩略图，点击调用 `wx.previewImage` 预览，不依赖 `rich-text` 内部事件 |
| 评论与回复 | `paicoding-miniapp/pages/detail/detail.*`；`WxMiniProgramRestControllerTest`；`scripts/test-miniapp-detail-page.js` | 通过；详情页会加载评论列表和子回复，折叠子回复可通过小程序接口分页展开，游客可阅读评论，发表一级评论或回复时触发登录保护并提交 `parentCommentId` / `topCommentId`，成功后刷新首屏评论和评论数；评论点赞/取消点赞只允许 `PRAISE/CANCEL_PRAISE`，后端校验评论属于当前文章；已登录用户只在自己的评论/回复上展示删除入口，删除时后端校验评论所属文章和当前用户；发表评论接口会创建新的保存对象，拒绝客户端携带 `commentId` 改写旧评论，并返回 `submittedCommentId` 供预发 smoke 精确清理；同时校验父评论/顶级评论属于当前文章；子回复分页接口会校验顶级评论属于当前文章且不是子评论 |
| 资料页头像边界 | `scripts/test-miniapp-profile-page.js` | 通过；头像上传成功后只保存服务端 URL，上传失败回滚旧头像，昵称/简介非法输入不发请求，空简介可清空旧简介，隐私授权状态、微信隐私 pending resolver 回写和协议入口可用，退出后 `onShow` 不被动重登；我的页原生下拉刷新会刷新当前用户资料并停止刷新态 |
| 头像昵称授权引导 | `paicoding-miniapp/utils/auth.js`；`paicoding-miniapp/utils/privacy.js`；`paicoding-miniapp/app.js`；`paicoding-miniapp/pages/index/index.js`；`paicoding-miniapp/pages/profile/profile.*`；`WxMiniProgramAuthServiceTest`；`scripts/test-miniapp-auth.js`；`scripts/test-miniapp-profile-page.js` | 通过；登录返回 `needProfile` 或本地判断头像/昵称未完善时，会在首页提示进入“我的”页；后端默认随机头像不再被误判为微信头像授权完成；小程序启动和“我的”页会注册微信隐私授权回调；“我的”页会展示资料未完善状态，并通过微信 `chooseAvatar` 和 `nickname` 完成授权；同意隐私按钮会回写本次微信隐私授权事件，资料完善后清理提示状态，退出登录也会清理残留提示 |
| 首页/搜索页行为 | `scripts/test-miniapp-feed-search-pages.js` | 通过；覆盖首页登录后加载分类和文章、分页、原生下拉刷新、分类切换、详情跳转、分类切换旧回包隔离、搜索提示、联想项直达详情、最近搜索保存/复用/清空、分享搜索入口写入历史、空搜索不请求、搜索结果分页、搜索结果原生下拉刷新、搜索提交清理待触发联想 timer、已发出 hint 回包隔离、旧搜索回包隔离 |
| 我的内容 | `paicoding-miniapp/pages/collection/collection.*`；`paicoding-miniapp/pages/history/history.*`；`scripts/test-miniapp-collection-page.js`；`scripts/test-miniapp-history-page.js` | 通过；个人中心可进入我的收藏和浏览历史，两个页面均使用登录态接口、分页加载、原生下拉刷新并可打开文章详情；收藏页支持取消收藏 |
| AppID 配置脚本 | `scripts/test-miniapp-configure-appid.sh` | 通过；可从环境变量/参数写入真实 AppID；只有显式 `--tourist` 才能恢复 `touristappid`；自测不会改动真实项目配置 |
| 上线前总闸脚本 | `scripts/test-miniapp-release-gates.sh` | 通过；覆盖缺少 `WECHAT_MINI_APP_ID`、`touristappid` 或非法 AppID、前后端 AppID 不一致、mock 开关为 true、缺少 `BASE_URL`、缺少 `SMOKE_LOGIN_CODE`、缺少 `AI_VALIDATE_SCRIPT` 等拒绝路径；验证不输出 AppSecret，预发 API smoke 会先于严格 readiness 执行并写入证据，mutation smoke 会覆盖点赞、收藏、评论、回复、评论点赞和删除，优先使用 `submittedCommentId` 清理自己创建的 smoke 评论，smoke 登录和评论 payload 均使用 JSON 序列化避免真实 code 或内容特殊字符破坏请求体 |
| 本地 E2E 启动脚本 | `scripts/test-miniapp-local-e2e.sh`；`scripts/run-miniapp-local-e2e.sh` | 通过；脚本固定 JDK8、dev profile 和 Maven libexec，自动选择本地空闲端口，临时固定并恢复 `.dev-port`，打包后用各模块 `target/classes` 和过滤后的 runtime classpath 启动 `QuickForumApplication`，再跑小程序 API smoke，并只清理自己启动的后端进程；2026-06-23 19:58 真实执行通过，smoke 返回 `article_id=2533625399918592`；本地运行日志默认写入 `.runtime/miniapp-e2e/`，不进入上线证据目录 |
| AI Skill 只读边界 | `scripts/test-miniapp-ai-boundary.js` | 通过；只允许 `searchArticles` / `getArticleDetail`，只允许 GET 访问 `/mini/api/search` 和 `/mini/api/articles/{articleId}`，拒绝登录、头像、支付、写入 HTTP 方法和互动接口；AI 卡片进入详情页带 `from=ai-skill`，详情页不会因此自动登录；空搜索结果会显示明确空态，不渲染空白详情卡 |
| API 地址一致性 | `scripts/test-miniapp-config-consistency.js` | 通过；主包和 AI Skill 的 develop/trial/release API 地址必须一致，preflight 默认 API smoke 地址必须等于 develop 地址 |
| 默认 readiness | `scripts/check-miniapp-readiness.sh` | 2026-06-23 19:54 通过审计模式；`failures=0`、`warnings=10`；release gate 脚本可执行性、隐私授权链路、AI Skill 配置、主包/AI Skill API 域名一致性和证据文件门禁已检查 |
| 严格 readiness | `STRICT=true scripts/check-miniapp-readiness.sh` | 2026-06-23 18:20 已执行但未通过；`failures=10`，阻塞项为真实 AppID、DevTools 登录、AI preview、后端真实环境变量、真机证据、证据文件组和验收空字段 |
| 证据文件门禁 | `STRICT=true scripts/check-miniapp-readiness.sh`；`scripts/test-miniapp-readiness-evidence.sh` | 已接入并自测通过；上线前必须在 `paicoding-miniapp/evidence/` 提供非空且带通过语义的预发 smoke、真机链路、微信后台、AI preview、AI execute/render 证据文件，文本类证据会扫描常见敏感值、Bearer token 和 JSON 形态敏感值 |
| Readiness 核心门禁 | `scripts/test-miniapp-readiness-core.sh` | 通过；覆盖前后端 AppID 不一致、验收关键字段为空、未明确允许提审、未解决问题不为无、验收文件 JSON token 泄露五类拒绝路径 |
| AI validate 安全门禁 | `scripts/test-miniapp-ai-validate-safety.sh` | 通过；`RUN_AI_VALIDATE=true` 必须显式传入非 `/tmp` 的 `AI_VALIDATE_SCRIPT`，执行 validator 时会从子进程环境移除后端 AppSecret 等敏感变量 |
| AI 证据生成器 | `scripts/test-miniapp-ai-evidence-writer.sh` | 通过；官方 validate 报告有 execute/render 通过信息时会生成 `ai-execute-render.validate.md`，仅有 preview 通过或 render 失败时不会伪造通过证据 |
| 真机证据生成器 | `scripts/test-miniapp-evidence-writer.sh` | 通过；可生成真机登录、头像资料、文章流、互动和微信后台域名/隐私脱敏证据，拒绝 token/openid/AppSecret 等敏感值，也拒绝把“未验证”等占位内容写成通过证据 |
| 搜索关键词兜底 | `WxMiniProgramRestControllerTest` | 通过；后端会 trim 搜索关键词，空关键词返回空页，超过 64 字符的搜索和搜索提示都会拒绝，不会打到搜索服务 |
| API 域名一致性门禁 | `scripts/check-miniapp-readiness.sh`；`scripts/test-miniapp-readiness-core.sh` | 通过；上线前会同时检查主包和 AI Skill 的 trial/release API URL 必须为 HTTPS，且两处 trial/release URL 必须一致 |
| 预发 API smoke | `ALLOW_REMOTE_SMOKE=true BASE_URL=https://预发域名 scripts/smoke-miniapp-api.sh` | 未执行 |
| 本地 API smoke | `scripts/run-miniapp-local-e2e.sh`；`BASE_URL=http://127.0.0.1:18080 scripts/smoke-miniapp-api.sh` | 通过；2026-06-23 19:58 由本地 E2E 脚本启动后端并执行，覆盖登录、分类、文章、搜索、详情、点赞/收藏互动和非法 type 拒绝；当前脚本已扩展 mutation smoke，开启后还会覆盖评论列表、评论、回复、子回复分页、评论点赞/取消、非法评论收藏类型拒绝和删除；开发默认端口仍为 8080 |
| 官方 AI validate | `AI_VALIDATE_SCRIPT=/path/to/reviewed/wxa-skills-validate/scripts/validate.mjs RUN_AI_VALIDATE=true scripts/preflight-miniapp.sh` | 静态规则通过；preview 因 DevTools 未登录失败；不再自动执行 `/tmp` 下的 validator |
| 后端字节码检查 | `scripts/preflight-miniapp.sh` 内置 `javap` 检查 | 未发现 `ArticleReadService`、`CategoryService`、`UserFootService`、`UserService`、`SessionDeviceMeta` 短名 descriptor |
| 本地端口清理 | `lsof -iTCP:18080 -sTCP:LISTEN -n -P` | 备用端口 smoke 后已关闭 Spring Boot，端口未占用；当前 8080 端口被本机其他 `node` 进程占用，跑默认端口 smoke 前需释放或显式改端口 |
| 头像 URL 回归 | `WxMiniProgramAuthServiceTest` | 服务端上传 CDN URL 支持 128 以上、512 以内；超过 512 仍拒绝；第三方 HTTP(S) 上传结果会被拒绝；相似 CDN 主机名不会因字符串前缀撞上而通过；登录和资料 JSON 不接收任意头像 URL，头像只通过 multipart 上传接口保存 |
| 动态配置日志脱敏 | `DynamicConfigContainerRedactionTest` | 通过；配置刷新日志保留普通配置，但会遮蔽 secret、password、token、apiKey、AK/SK、Authorization/Bearer 等敏感值 |
| 子 Agent 独立复核 | Maxwell / Hegel / Laplace / Bernoulli / Russell / Hypatia 只读 release-risk review | Maxwell/Hegel/Laplace 前序复核的问题已处理；Bernoulli 复核指出 3 个问题：客户端 `commentId` 可触发旧评论更新、`app.js` 全局启动自动登录可能破坏 AI 入口、回复参数缺少父/顶级评论归属校验；三项均已修复。Russell 复核指出交接命令缺 `AI_VALIDATE_SCRIPT`、scroll-view 刷新竞态和预发 smoke 未覆盖评论；三项均已处理。Hypatia 只读复核未发现新的微信小程序代码级上线风险，剩余阻塞为真实 AppID、真机、微信后台域名/隐私和 AI execute/render 证据；本地由 `scripts/preflight-miniapp.sh` 在 2026-06-23 19:54 以 Java 8 重新通过 |

## 真机链路

| 场景 | 期望结果 | 证据 |
| --- | --- | --- |
| 首次打开 | 自动登录成功，后端返回真实微信 session 对应 token | 未验证 |
| 头像授权 | `chooseAvatar` 选择头像后上传成功，页面展示服务端 URL，不保存 `wxfile://` | 未验证 |
| 昵称保存 | 微信昵称输入保存成功，超长昵称被拦截 | 未验证 |
| 退出登录 | 退出后不自动重新创建会话，点击需要登录的操作才重新登录 | 未验证 |
| 首页文章流 | 分类切换、分页、下拉刷新、失败重试正常 | 未验证 |
| 搜索 | 搜索提示、结果页、无结果、失败重试正常 | 未验证 |
| 文章详情 | 详情展示正常，作者缺失等边界不崩溃 | 未验证 |
| 点赞收藏 | 点赞/取消、收藏/取消成功，连点不重复提交，计数最终同步 | 未验证 |
| 评论回复 | 评论列表、一级评论、展开更多回复、回复评论、评论点赞/取消点赞、删除自己的评论/回复、登录态恢复和评论数刷新正常 | 未验证 |
| 我的内容 | 我的收藏、浏览历史分页、下拉刷新和详情跳转正常 | 未验证 |
| 过期登录态恢复 | 头像上传、昵称保存、点赞、收藏、评论遇到过期 token 后重新登录并重试一次 | 未验证 |

## 微信后台

| 配置项 | 结果 |
| --- | --- |
| 服务器 `request` 合法域名已配置 | 未确认 |
| 服务器 `uploadFile` 合法域名已配置 | 未确认 |
| 服务器 `downloadFile` 合法域名已配置 | 未确认 |
| 隐私保护指引覆盖头像、昵称 | 未确认 |
| AI 能力/开发模式已开启 | 未确认 |

## AI Skill

| 场景 | 期望结果 | 证据 |
| --- | --- | --- |
| 静态规则 | `errors=0`、`warnings=0` | 通过；`total=49 passed=49 failed=0 errors=0 warnings=0` |
| preview 编译 | `buildStatus=pass` | 未通过；当前失败为 `access_token missing`，需登录 DevTools 并换真实 AppID |
| `searchArticles` execute | `status=ok` 且 `invokeResult.isError !== true` | 未验证 |
| `searchArticles` render | 组件非空白、无裁剪、字段绑定正确 | 未验证 |
| `getArticleDetail` execute | `status=ok` 且 `invokeResult.isError !== true` | 未验证 |
| `getArticleDetail` render | 组件非空白、无裁剪、字段绑定正确 | 未验证 |

## 发布结论

- 是否允许提审：
- 未解决问题：
- 回滚确认人：
