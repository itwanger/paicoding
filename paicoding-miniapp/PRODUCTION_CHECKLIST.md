# 微信小程序上线前清单

当前完成状态以 `ACCEPTANCE_EVIDENCE.md` 为准；本清单描述的是上线前必须全部通过的门禁，不代表当前已经完成。

## 后端环境

- `pre` / `prod` 环境必须设置 `PAICODING_WX_MINI_APP_ID`。
- `pre` / `prod` 环境必须设置 `PAICODING_WX_MINI_APP_SECRET`。
- `pre` / `prod` 环境必须保持 `PAICODING_WX_MINI_MOCK_ENABLED=false`。
- `/mini/api/**` 必须通过 HTTPS 正式域名访问。
- `Authorization: Bearer <token>` 只在 `/mini/api/**` 生效，不能用于后台路径。
- 头像只通过 `/mini/api/user/avatar` multipart 上传，不接受小程序端传入任意远程 URL 转存。
- `/mini/api/auth/login` 和 `/mini/api/user/profile` 不保存 JSON 里的 `avatarUrl`，避免绕过上传链路写入任意头像字符串。
- `/mini/api/user/avatar` 的上传结果必须来自 `image.cdn-host` 或 `image.oss.host`；第三方 HTTP(S) URL 必须拒绝。
- `/mini/api/auth/login` 已有本机进程内轻量限流；如果预发/生产是多实例部署，需要在 Nginx/WAF/Redis 层补一层分布式限流。

## 微信后台

- 真实小程序 AppID 必须绑定项目。
- 服务器域名必须配置 `request`、`uploadFile`、`downloadFile` 合法域名。
- 域名必须已 ICP 备案，证书链有效，生产不使用 IP、localhost、HTTP。
- 必须配置《小程序用户隐私保护指引》，覆盖头像、昵称等个人信息。
- 小程序启动时已注册微信隐私授权回调，个人页已提供隐私协议入口、同意按钮和 pending resolver 回写；后台隐私指引必须和页面采集项一致。

## 小程序真机验收

- `STRICT=true scripts/check-miniapp-readiness.sh` 必须通过，不再使用 `touristappid`，微信开发者工具已登录，AI validate preview 已通过。
- 真实 AppID 切换必须通过 `scripts/configure-miniapp-appid.sh` 执行，避免手改 JSON 漏字段或格式错误。
- 真实 AppID/AppSecret、微信开发者工具登录和预发域名就绪后，必须带 `BASE_URL=https://预发域名` 和真实 `SMOKE_LOGIN_CODE` 跑 `scripts/run-miniapp-release-gates.sh`，该脚本只校验不发布，并且不能跳过预发 API smoke。
- `ACCEPTANCE_EVIDENCE.md` 必须填写真实设备、真实 AppID、API 域名和关键链路证据，且未包含 AppSecret/token/openid 等敏感值；对应截图、录屏或脱敏报告放到 `paicoding-miniapp/evidence/`，文件命名按该目录 README 执行。
- 真机和微信后台脱敏证据可用 `scripts/write-miniapp-evidence.py` 生成；脚本只负责记录已完成验收结果，不能替代真实设备、真实 AppID 和微信后台检查。
- 首次打开自动登录成功。
- 头像选择并上传成功，上传失败不会保存 `wxfile://` 临时路径。
- 昵称保存成功，超长昵称会被拦截。
- 首页分类、分页、下拉刷新、无数据、失败重试正常。
- 搜索提示、搜索结果、无结果、失败重试正常。
- 文章详情、点赞、收藏、评论、展开更多回复、回复、评论点赞/取消点赞、删除自己的评论/回复、未登录恢复正常。
- 我的收藏、浏览历史分页加载和详情跳转正常。
- 头像上传、昵称保存、点赞、收藏、评论、评论点赞遇到过期 token 时会重新登录并重试一次。
- 文章点赞/收藏和评论点赞连点不会重复提交，成功后计数和状态同步刷新。
- 退出登录后不自动重新创建会话，下一次访问再登录。
- 真机预览使用真实 AppID、真实 AppSecret、`PAICODING_WX_MINI_MOCK_ENABLED=false` 完成登录链路验证。

## AI Skill 验收

- 必须按 `AI_SKILL_RESEARCH.md` 确认第一版 AI Skill 只覆盖文章搜索/详情，不覆盖登录、头像上传、点赞、收藏、评论等用户态写操作。
- `app.json` 必须包含 `lazyCodeLoading`、`agent.skills`、`subPackages`。
- `project.config.json` 必须包含 `packOptions.include=skills`。
- `skills/article-search` 包含 `SKILL.md`、`mcp.json`、`index.js`、`apis/`、`components/`。
- `mcp.json` 的 `componentPath` 与组件路径完全一致，`relatedPage` 指向真实页面。
- 原子接口不跨分包 `require` 主包文件。
- 使用官方 `wxa-skills-validate` 完成静态校验、组件 mock 渲染和编译校验。
- 使用官方 `wxa-skills-eval` 或等价评估流程补一份核心话术质量报告，至少覆盖“搜文章”和“打开详情”两类路径。
- 已有小程序接入 AI 开发模式时，CloudBase/mp-skills 可作为 Skill 管理、validate、eval 工具链，但第一版不要求 PaiCoding 后端迁移到云函数；Spring Boot `/mini/api/search` 和 `/mini/api/articles/{articleId}` 仍是原子接口数据源。
- 原子组件 render 产物的 `consoleMessages.snapshotCard` 包含 `[ai-mode] article-card overflow monitor=on`，且不包含 `overflowed=true`。
- 真机执行验证需要登录微信开发者工具并绑定真实小程序 AppID 后再跑，不能只依赖本地 tourist appid。
