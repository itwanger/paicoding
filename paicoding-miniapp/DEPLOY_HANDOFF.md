# 微信小程序生产交接

本文件只描述上线前交接步骤。生产发布、微信后台提审和回滚由维护者确认后执行。

## 后端发布前

在生产机器的 `.env` 或 `.env.local` 中补齐：

```bash
PAICODING_WX_MINI_APP_ID=真实小程序AppID
PAICODING_WX_MINI_APP_SECRET=真实小程序AppSecret
PAICODING_WX_MINI_MOCK_ENABLED=false
```

发布前先在预发环境验证。以下命令只用于预发主机；生产执行前必须由维护者二次确认当前主机、目录、环境和回滚包：

```bash
pwd
hostname
grep -n "env.name" paicoding-web/target/classes/application-web.yml
export PAICODING_WX_MINI_APP_ID=真实预发AppID
export PAICODING_WX_MINI_APP_SECRET=真实预发AppSecret
export PAICODING_WX_MINI_MOCK_ENABLED=false
./launch.sh restart
```

预发/生产环境启动时，如果 mock 被打开或 AppID/AppSecret 缺失，应用会 fail-fast，不能带着不可用的小程序登录上线。

`/mini/api/auth/login` 目前有本机进程内轻量限流，用来挡住开发期误刷和单实例异常流量。生产如果是多实例，建议在 Nginx、WAF 或 Redis 层追加分布式限流，避免同一设备/IP 跨实例绕过。

## Nginx 与域名

小程序端生产请求域名默认为 `https://paicoding.com`。如果改成独立 API 子域名，需要同步修改：

- `paicoding-miniapp/utils/config.js`
- `paicoding-miniapp/skills/article-search/utils/request.js`
- 微信小程序后台的服务器域名配置

`scripts/check-miniapp-readiness.sh` 会检查主包与 AI Skill 的 trial/release API URL 都是 HTTPS，并且两处配置一致；改域名后必须先跑 readiness，避免普通页面和 AI Skill 调到不同后端。

Nginx 至少要确保以下路径走到同一个 paicoding-web upstream：

```nginx
location /mini/api/ {
    proxy_pass http://paicoding_web;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

微信后台需要配置合法域名：

- `request`：`https://paicoding.com` 或正式 API 子域名
- `uploadFile`：头像上传所用正式域名
- `downloadFile`：文章图片/CDN 所用正式域名

## 小程序提审前

1. 用脚本把 `project.config.json` 的 `appid` 改成真实小程序 AppID：
   ```bash
   WECHAT_MINI_APP_ID=真实小程序AppID scripts/configure-miniapp-appid.sh
   ```
2. 确认 `setting.urlCheck=true`，生产提审不能依赖开发者工具“不校验域名”。
3. 小程序后台配置《小程序用户隐私保护指引》，声明头像、昵称等个人信息用途。
4. 确认个人页隐私协议入口可打开，授权后头像选择和昵称保存链路正常。
5. 用真机预览验证 `PRODUCTION_CHECKLIST.md` 中所有条目。
6. 按 `AI_SKILL_RESEARCH.md` 确认 AI 开发模式接入边界：第一版只开放文章搜索/详情，不开放登录、头像上传、点赞、收藏、评论。
7. 使用官方 `wxa-skills-validate` 对 `skills/article-search` 完成静态校验、组件渲染和编译校验。
8. 登录微信开发者工具并切换真实小程序 AppID 后，再补跑 AI Skill 真机执行验证。
9. 使用官方 `wxa-skills-eval` 或等价评估流程，对“搜文章”和“打开详情”核心话术补一份质量报告。

## 回滚

后端回滚：

1. 保留发布前 jar、配置和 `pid.log`。
2. 如果 `/mini/api/auth/login`、`/mini/api/user/me` 或文章接口上线后异常，先从 Nginx 层临时下线小程序入口或暂停提审版本发布。
3. 生产回滚命令只由维护者确认后执行。执行前先确认主机、目录和环境：

```bash
pwd
hostname
grep -n "env.name" paicoding-web/target/classes/application-web.yml
./launch.sh restart
```

小程序回滚：

1. 微信公众平台保留上一版线上版本。
2. 新版本发布后如果发现登录、头像或文章浏览阻断，立即在小程序后台回退到上一线上版本。
3. 后端接口保留向后兼容：`token` 和 `Authorization: Bearer <token>` 不删除，新增字段只做增强。

## 本地验收命令

后端编译和测试必须使用 Java 8。当前脚本会显式设置 `JAVA_HOME=$(/usr/libexec/java_home -v 1.8)` 并把 Java 8 `bin` 放到 `PATH` 前面；不要使用本机默认 JDK21 跑 Maven，也不要执行 `mvn clean`。

```bash
scripts/preflight-miniapp.sh
```

默认 preflight 会执行小程序 JS/JSON 检查、脚本语法检查、端侧 auth 重登重试测试、详情页登录边界测试、评论/回复/子回复分页/点赞/删除行为测试、资料页头像/昵称边界测试、我的收藏/浏览历史页面测试、首页/搜索页行为测试、原生下拉刷新行为测试、AI Skill 原子接口契约测试、AI Skill 只读边界测试、远程/生产 smoke 保护测试、release gate 顺序测试、readiness 核心门禁测试、证据文件门禁测试、AI 证据生成器测试、`git diff --check`、Java 8 小程序相关单测和关键类字节码 descriptor 回归检查；不会自动跑接口 smoke，也不会强制调用需要微信开发者工具登录态的 AI validate。

`scripts/smoke-miniapp-api.sh` 默认只允许本地域名；本地域名下会额外验证点赞、取消点赞、收藏、取消收藏。传入 `AVATAR_FILE=/path/avatar.png` 时会额外验证头像 multipart 上传。如果明确要在预发环境验证接口，需要设置 `ALLOW_REMOTE_SMOKE=true`；如果还要验证互动写接口，再额外设置 `RUN_MUTATION_SMOKE=true`。生产域名 `https://paicoding.com` 还有第二层保护，必须额外设置 `ALLOW_PRODUCTION_SMOKE=true`，并且只能在维护者确认后执行。

可选扩展验收：

```bash
scripts/check-miniapp-readiness.sh
STRICT=true scripts/check-miniapp-readiness.sh
RUN_API_SMOKE=true scripts/preflight-miniapp.sh
RUN_API_SMOKE=true AVATAR_FILE=/tmp/avatar.png scripts/preflight-miniapp.sh
RUN_API_SMOKE=true BASE_URL=http://127.0.0.1:18080 scripts/preflight-miniapp.sh
scripts/run-miniapp-local-e2e.sh
ALLOW_REMOTE_SMOKE=true RUN_API_SMOKE=true BASE_URL=https://预发域名 scripts/preflight-miniapp.sh
WECHAT_MINI_APP_ID=真实小程序AppID scripts/configure-miniapp-appid.sh
RUN_AI_VALIDATE=true scripts/preflight-miniapp.sh
```

小程序开发版和 AI Skill 的 `develop` API 地址默认都是 `http://127.0.0.1:8080`，`scripts/preflight-miniapp.sh` 在 `RUN_API_SMOKE=true` 且未传 `BASE_URL` 时也使用这个地址。若本机 8080 被其他进程占用，可显式传 `BASE_URL=http://127.0.0.1:18080` 之类的备用端口，但必须同步两处 develop 配置，避免普通页面和 AI Skill 打到不同后端。

`scripts/run-miniapp-local-e2e.sh` 是本地一键端到端脚本，只启动本机 dev profile 后端并运行 smoke，不部署、不提审、不访问生产。默认从 18080 开始找空闲端口，避免抢占微信开发者工具或前端本地端口；需要指定端口时传 `MINIAPP_E2E_PORT=18080`。

真实 AppID、AppSecret、微信开发者工具登录和预发域名都就绪后，可以用总闸脚本串行跑上线前门禁。这个脚本只做校验，不部署、不提审、不发布；`BASE_URL`、`SMOKE_LOGIN_CODE` 和 `AI_VALIDATE_SCRIPT` 都是必填项。`SMOKE_LOGIN_CODE` 必须来自真实 AppID 下的 `wx.login` 临时 code，不能用本地 mock code。脚本会对该环境执行远端 API smoke，写入 `paicoding-miniapp/evidence/pre-api-smoke.*.md`；其中 mutation smoke 会覆盖点赞、收藏、评论、回复、评论点赞和删除，并删除自己创建的 smoke 评论。脚本也会在严格 readiness 前写入 AI preview 摘要证据；如果官方 validate 报告中能识别到 execute 和 render 均通过，还会自动写入 `paicoding-miniapp/evidence/ai-execute-render.validate.md`。如果报告里没有 execute/render 通过信息，脚本不会伪造该证据，严格 readiness 会继续阻断：

```bash
WECHAT_MINI_APP_ID=真实小程序AppID \
PAICODING_WX_MINI_APP_ID=真实小程序AppID \
PAICODING_WX_MINI_APP_SECRET=真实小程序AppSecret \
PAICODING_WX_MINI_MOCK_ENABLED=false \
SMOKE_LOGIN_CODE=真机或开发者工具wx.login临时code \
BASE_URL=https://预发域名 \
AI_VALIDATE_SCRIPT=/path/to/reviewed/wxa-skills-validate/scripts/validate.mjs \
scripts/run-miniapp-release-gates.sh
```

`scripts/check-miniapp-readiness.sh` 默认是审计模式，会把缺真实 AppID、后端环境变量缺失、开发者工具未登录、AI preview 未通过输出为 warning。上线前用 `STRICT=true` 跑，所有外部门禁都必须变成 pass。

真实 AppID 切换用 `scripts/configure-miniapp-appid.sh` 执行；本地调试需要恢复游客配置时执行 `scripts/configure-miniapp-appid.sh --tourist`。真机验收结果记录到 `ACCEPTANCE_EVIDENCE.md`，AppID 只记录后 6 位，不要在该文件写入 AppSecret、token、Cookie、手机号、openid 等敏感值。`STRICT=true scripts/check-miniapp-readiness.sh` 会检查该文件是否还存在 `未执行`、`未验证`、`未确认` 占位符，关键字段是否为空，并扫描常见敏感值形态。截图、录屏或脱敏报告放在 `paicoding-miniapp/evidence/`；严格 readiness 会要求预发 smoke、真机登录、头像资料、文章流、互动、微信后台域名/隐私、AI preview、AI execute/render 这些证据文件组都存在，并且文本证据带有通过语义标记。

真机和微信后台脱敏证据可用 `scripts/write-miniapp-evidence.py` 生成。脚本不会自动判定真实场景通过，只负责把你已经完成的验收结果写成 readiness 可识别的 Markdown，并拒绝常见 token、openid、AppSecret 形态和“未验证”占位内容。

`RUN_AI_VALIDATE=true` 会调用微信开发者工具 CLI；当前 CLI 登录态为 `login=false` 时会停在 preview 编译/上传阶段，需要登录并切换真实 AppID 后重跑。

当前本地已经通过的校验边界：

- JS 语法检查、JSON 格式检查通过。
- `scripts/preflight-miniapp.sh` 默认模式通过。
- Java 8 下 `GlobalInitServiceMiniProgramTest`、`ReqRecordFilterMiniProgramTest`、`WxMiniProgramAuthServiceTest`、`WxMiniProgramRestControllerTest`、`DynamicConfigContainerRedactionTest` 通过，共 62 个测试，覆盖登录、鉴权 Header 作用域、小程序设备头解析、资料校验、简介清空、登录限流、登录/资料 JSON 不接收任意头像 URL、服务端资料完整性判断、默认随机头像不算微信头像授权完成、服务端上传头像 CDN URL、第三方上传头像 URL 拒绝、相似 CDN 主机名拒绝、搜索关键词前后端兜底、分类/文章列表边界、分类 ID 兜底、分类空元素兜底、点赞收藏 allow-list、我的收藏、浏览历史、评论列表、一级评论、评论回复、子回复分页展开、评论点赞/取消点赞、删除自己的评论/回复、拒绝客户端 `commentId` 更新旧评论、拒绝跨文章回复、拒绝跨文章拉取子回复、拒绝用子评论作为顶级评论拉取回复、AI 入口不被全局启动自动登录和动态配置日志脱敏；小程序端 JS 行为测试额外覆盖首页、搜索、详情、我的、收藏、历史的原生下拉刷新。
- `scripts/preflight-miniapp.sh` 已纳入 `javap` descriptor 检查，防止 `ArticleReadService`、`CategoryService`、`UserFootService`、`UserService`、`SessionDeviceMeta` 再次编译成短名 descriptor。
- 本地后端曾以 `BASE_URL=http://127.0.0.1:18080` 跑通 `/mini/api/**` smoke，覆盖登录、用户态、分类、文章列表、搜索提示、文章详情、点赞、取消点赞、收藏、取消收藏，并验证非法互动 `type=1` 不会成功；开发默认端口仍以 `http://127.0.0.1:8080` 为准。
- 官方 `wxa-skills-validate` 静态规则通过：`total=49 passed=49 failed=0 errors=0 warnings=0`。
- 官方 `wxa-skills-validate` preview 编译未通过：2026-06-23 重跑结果为 `buildStatus=fail`、`stage=compile`，原因是当前微信开发者工具 CLI 登录态为 `login=false`，报 `access_token missing`。
- 登录开发者工具并切换真实 AppID 后必须重跑完整 validate，补齐 preview、execute、render 和真机验证。
