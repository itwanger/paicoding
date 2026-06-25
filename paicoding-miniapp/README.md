# PaiCoding 微信原生小程序

## 本地开发

1. 后端启动本地 `paicoding-web`。小程序开发版默认请求 `http://127.0.0.1:8080`，如果后端启动日志显示了其他端口，用 `utils/config.js` 里的 `API_BASE_URL.develop` 临时改成本地端口。
2. 使用微信开发者工具打开 `paicoding-miniapp`。
3. `project.config.json` 默认开启域名校验。仅本地联调 `127.0.0.1` 时，可在微信开发者工具本机设置里临时勾选“不校验合法域名、web-view 域名、TLS 版本以及 HTTPS 证书”，不要把生产配置改成绕过域名校验。
4. 本地没有小程序 `AppSecret` 时，后端 dev/test 配置允许 mock 登录；拿到真实 AppID/AppSecret 后，把 `utils/config.js` 的 `forceMockLogin` 保持为 `false`，并配置后端环境变量：
   - `PAICODING_WX_MINI_APP_ID`
   - `PAICODING_WX_MINI_APP_SECRET`
   - `PAICODING_WX_MINI_MOCK_ENABLED=false`
5. 本地接口冒烟：
   ```bash
   ../scripts/preflight-miniapp.sh
   RUN_API_SMOKE=true BASE_URL=http://127.0.0.1:8080 ../scripts/preflight-miniapp.sh
   ```
   如果 8080 已被其他本地服务占用，可以把后端临时启动到其他端口，并显式传入 `BASE_URL`；这种情况下也要同步小程序 `utils/config.js` 和 AI Skill `skills/article-search/utils/request.js` 的 `develop` 地址。
6. 一键本地后端 + 小程序 API smoke：
   ```bash
   ../scripts/run-miniapp-local-e2e.sh
   ```
   该脚本会用 JDK8 和 dev profile 启动本地 `paicoding-web`，自动选择 `18080` 起的空闲端口，跑完 `/mini/api/**` smoke 后关闭自己启动的后端进程；如果本地 MySQL/Redis/ES 等依赖未就绪，按脚本输出的 backend log 定位即可。

## 功能范围

- 强制登录：`wx.login` 或本地 mock code -> 后端 `/mini/api/auth/login` -> token。
- 头像昵称：使用 `chooseAvatar` 和 `type="nickname"`，并通过微信隐私授权回调和隐私协议入口处理用户同意状态。
- 内容能力：首页文章流、分类、搜索、文章详情、点赞、收藏。
- AI 能力：`skills/article-search` 暴露文章搜索和详情原子接口，供微信小程序 AI 开发模式调度。
- AI 调研与边界：见 `AI_SKILL_RESEARCH.md`。第一版只开放文章搜索/详情，不把登录、头像上传、点赞、收藏作为 AI 可调用能力。

## 生产上线前检查

1. 用脚本把 `project.config.json` 的 `appid` 替换成真实小程序 AppID，并使用已登录的微信开发者工具：
   ```bash
   WECHAT_MINI_APP_ID=真实小程序AppID ../scripts/configure-miniapp-appid.sh
   ```
   需要恢复本地游客 AppID 时执行：
   ```bash
   ../scripts/configure-miniapp-appid.sh --tourist
   ```
2. 微信小程序后台配置服务器域名：
   - `request`：`https://paicoding.com` 或正式 API 子域名
   - `uploadFile`：头像上传所用正式域名
   - `downloadFile`：图片/CDN 所用正式域名
3. 后端 pre/prod 必须配置真实 `PAICODING_WX_MINI_APP_ID`、`PAICODING_WX_MINI_APP_SECRET`，且 `PAICODING_WX_MINI_MOCK_ENABLED=false`。服务启动时会对 pre/prod 做 fail-fast。
4. 小程序后台配置《小程序用户隐私保护指引》，声明头像、昵称等个人信息处理用途；真机验证未同意/已同意隐私协议时头像昵称填写能力都可恢复。
5. 真机预览验证：首次登录、头像上传、昵称保存、首页分页、分类切换、搜索提示、文章详情、点赞、收藏、退出登录。
6. AI Skill 校验要求：Node.js >= 18，微信开发者工具 nightly 版，已登录，且「设置 -> 安全设置 -> 服务端口」已开启。用官方 `wechat-miniprogram/ai-mode-skills` 的 `wxa-skills-validate` 校验 `skills` 目录；静态校验可在未登录时完成，preview 编译和真机执行验证需要已登录的开发者工具，真实执行还需要真实小程序 AppID。
7. 真实 AppID、后端环境变量和微信开发者工具登录态就绪后，必须重新跑：
   ```bash
   STRICT=true ../scripts/check-miniapp-readiness.sh
   AI_VALIDATE_SCRIPT=/path/to/reviewed/wxa-skills-validate/scripts/validate.mjs RUN_AI_VALIDATE=true ../scripts/preflight-miniapp.sh
   ```
