# 小程序上线证据目录

此目录用于本地保存上线前验收截图、录屏、导出的 JSON 报告或脱敏 Markdown 记录。目录下除本 README 外默认被 `.gitignore` 忽略，不要提交 AppSecret、token、Cookie、openid、手机号、后台完整账号信息或未打码用户资料。证据文件必须非空；文本类证据会被 readiness 扫描常见敏感值形态，并需要包含通过语义标记，例如 `status: pass`、`状态：通过` 或对应工具输出里的成功标记。

`STRICT=true scripts/check-miniapp-readiness.sh` 会检查以下证据文件组是否存在：

- `pre-api-smoke.*`：预发 API smoke 命令输出或脱敏报告，需包含 `miniapp smoke: ok`。
- `device-login.*`：真机首次打开、真实微信登录成功证据。
- `device-avatar-profile.*`：真机头像选择、头像上传、昵称保存证据。
- `device-article-flow.*`：真机首页文章流、搜索、详情浏览证据。
- `device-interactions.*`：真机点赞、取消点赞、收藏、取消收藏和过期登录态恢复证据。
- `wechat-domain-privacy.*`：微信后台 request/upload/download 合法域名和隐私保护指引证据。
- `ai-preview.*`：微信 AI Skill 官方 validate preview 编译通过证据，需包含 `summary.buildStatus: pass` 或等价 JSON 字段。
- `ai-execute-render.*`：微信 AI Skill execute/render 成功、组件非空白且无裁剪证据，需包含 `status: pass` 或明确的 execute/render pass 结论。

真机和微信后台证据可以用脚本生成脱敏 Markdown，脚本会拒绝常见 token、openid、AppSecret 形态，也会拒绝把“未验证”等占位内容写成 `status: pass`：

```bash
python3 scripts/write-miniapp-evidence.py \
  --group device-login \
  --status pass \
  --env pre \
  --app-id-suffix abcdef \
  --device 'iPhone 15 / WeChat 8.x' \
  --result '首次打开后真实微信登录成功，首页和个人页均能读取用户态。'
```

微信后台域名/隐私证据需要补齐四个详情字段：

```bash
python3 scripts/write-miniapp-evidence.py \
  --group wechat-domain-privacy \
  --status pass \
  --env pre \
  --app-id-suffix abcdef \
  --detail requestDomain=https://paicoding.com \
  --detail uploadDomain=https://paicoding.com \
  --detail downloadDomain=https://cdn.paicoding.com \
  --detail privacy=avatar-nickname \
  --result '微信后台 request、uploadFile、downloadFile 合法域名和隐私保护指引均已配置。'
```

建议文件名示例：

```text
pre-api-smoke.2026-06-23.md
device-login.iphone15-wechat-8.x.mp4
device-avatar-profile.iphone15-wechat-8.x.png
device-article-flow.iphone15-wechat-8.x.mp4
device-interactions.iphone15-wechat-8.x.mp4
wechat-domain-privacy.admin-screenshots.md
ai-preview.validate-report.json
ai-execute-render.article-search.md
```

文本证据最小模板：

```markdown
# Device Login Evidence

- status: pass
- checkedAt: 2026-06-23 20:00 CST
- appIdSuffix: abcdef
- device: iPhone 15 / WeChat 8.x
- env: pre
- result: 首次打开后自动登录成功，页面进入首页，个人页可看到用户 ID。
- redaction: 已打码用户昵称、头像、openid、token、Cookie、手机号。
```

```markdown
# WeChat Domain Privacy Evidence

- status: pass
- checkedAt: 2026-06-23 20:10 CST
- requestDomain: https://paicoding.com
- uploadDomain: https://paicoding.com
- downloadDomain: https://cdn.paicoding.com
- privacy: 已配置头像、昵称用途；小程序个人页可打开隐私协议。
- redaction: 已打码后台账号、主体信息、AppSecret。
```

```markdown
# AI Execute Render Evidence

- status: pass
- checkedAt: 2026-06-23 20:20 CST
- validator: wxa-skills-validate
- preview: buildStatus=pass
- execute: searchArticles=pass, getArticleDetail=pass
- render: article-card=pass, empty state=pass, overflowed=false
- redaction: validate 报告不包含 token、openid、AppSecret。
```
