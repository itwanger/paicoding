# 文章交互表格与 AI 学习助教：实现与验收记录

更新日期：2026-11-18。当前版本聚焦文章内计划表与数据表的原位阅读增强、本地打卡进度保存及单行 AI 学习助教，不新建服务端个人表或修改作者原文。

## 功能范围与交互设计

- **按规模分级接管**：文章内行数 ≥ 4、列数 ≥ 5 或内容较长的规则表格自动升级为交互表格；少于 4 行且列数较少的小型对照表、含合并单元格的复杂表格仅包裹横向滚动容器，保持原生紧凑排版。
- **按需加载网格资源**：页面入口仅加载轻量脚本与样式，检测到需要升级的表格时再按需加载本地固定版本 Tabulator 6.3.1（支持 `global.siteInfo.oss` 静态资源前缀），无表格页面不额外下载 460 KB 网格资源。
- **打卡进度与视图偏好持久化**：按 `文章 ID + 版本 ID + 表格序号` 在浏览器本地存储打卡勾选状态、隐藏列、分组列、列宽与“只看未完成”筛选状态，顶部展示实时完成进度条。
- **单击查看完整内容**：长文本在单元格内以两行紧凑展示，单击单元格非链接区域或点击行号即可展开详情面板，兼容桌面端与移动端触屏操作；页内锚点链接保持页内跳转，外链新开标签页。
- **原页展开**：点击「展开」后表格在当前文章中占满可用宽度，桌面端并排显示详情，手机端上下排列。点击「收起」或按 Esc 恢复阅读布局。短列按内容收紧，长列分享可用宽度，手动调整的列宽优先。
- **兼容多版本切换与划线评论**：暴露 `window.initArticleTables` 与 `window.resetArticleTables`，配合 `article-editions.js` 在版本切换时自动清理并重挂载；保留隐藏的源 `<table>` 并隔离生成的 `.pai-article-ui` 节点，确保 `selection.js` 的划线字符指纹与标签序号不变。
- **CSV 安全导出**：支持按当前搜索筛选结果与可见列导出 UTF-8 BOM CSV，清理非法文件名字符，并对 `= + - @` 开头的电子表格公式字符做前置单引号转义。

## 后端接口与 AI 行为

- **接口路径**：
  - `GET /api/tables/capabilities`：页面级单次请求，返回 `{ ai, loggedIn }`。
  - `POST /api/tables/learning-help/stream`：SSE 流式返回单行知识点讲解、学习顺序、自测题或自定义问答。
  - `POST /api/tables/learning-help`：同步 JSON 兜底接口。
- **安全与反向代理兼容**：写接口要求 `X-Pai-Table: 1` 请求头，拦截跨站请求，并优先读取 `X-Forwarded-Host` / `X-Forwarded-Proto` 比对 `Origin`，兼容 Nginx 反向代理转发。
- **全局异常对齐**：`TableException` 继承 `ForumException`，配合 `ForumExceptionHandler` 原样透传 400 / 401 / 403 / 429 / 502 / 503 状态码与提示信息，未登录访问时前端自动唤起 `#loginModal` 登录弹窗。
- **分级配额与失败退回**：复用 `DeepSeekConf` 配置；普通登录用户默认每日 10 次（`tables.ai.daily-limit`），知识星球正式用户默认每日 30 次（`tables.ai.star-daily-limit`）。上游模型超时或返回 5xx 时自动退回预扣次数。
- **前端多动作缓存与安全渲染**：按 `行号 + 操作类型` 独立缓存“讲解知识点 / 学习顺序 / 出自测题”结果，切换按钮不覆盖已有回答、不重复扣减次数；指令通过输入框内的「发送」提交，失败保留草稿；AI 输出通过纯 DOM 解析渲染代码块、行内代码、列表及折叠参考答案，不使用 `innerHTML` 拼接未转义内容。

## 本地测试命令

```bash
JAVA8_HOME=$(/usr/libexec/java_home -v 1.8)
export JAVA_HOME="$JAVA8_HOME"
export PATH="$JAVA8_HOME/bin:$PATH"
/opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn -pl paicoding-web -am \
  -Dtest=ForumExceptionHandlerTest,TableIntegrationTest,TableAiServiceTest,TableAiConfigurationTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
node scripts/test-table-model.js
node scripts/test-highlight-anchors.js
node scripts/test-article-table-interactions.js
```
