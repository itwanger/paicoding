# 本地部署与冒烟验证

## Goal

在本机从零跑通 paicoding 主应用与 paicoding-admin 后台，逐项验证核心功能可用，并把完整部署步骤（含踩坑）沉淀为可复现文档。

## In Scope

### 1. 基础设施（Docker）

- 新建 `docker-compose.dev.yml`（仓库根目录，标注「fork 新增，仅本地开发用」），起 **MySQL / Redis / Elasticsearch / RabbitMQ** 四个服务，版本对齐上游 README 要求。
- **不含 MongoDB**：README 罗列但全仓无依赖与代码（已核实，见 `spec/backend/integration-patterns.md`）。
- 上游无官方 compose 文件（已核实仓库根与 scripts/ 均无），此为新增产物。

### 2. 配置

- `cp .env.example .env`，梳理必填项：
  - 必填：`PAICODING_DB_PASSWORD`（对齐 compose 中 MySQL 密码）
  - 可选：LLM key（可先留空，或配 sub2api 网关——`chatgpt.conf.*.apiHost` 指向网关地址即可，见 `spec/backend/ai-llm-integration.md`）
  - ES：`PAICODING_ES_OPEN=true` + hosts/scheme 对齐本地容器（注意 dev 默认 `scheme=https`，本地明文 http 需调整环境变量，不改代码）
- 确认 `resources-env/dev/` 各 yml 指向本地容器；RabbitMQ 需 `rabbitmq.switchFlag=true` 才启用（dev 默认 false）。
- Maven dev profile 默认激活，无需额外参数。

### 3. 构建与启动

- Maven 构建通过；启动时 Liquibase 自动建表（`liquibase/master.xml`）；应用启动日志无 ERROR。
- 启动入口：`QuickForumApplication`（或 `launch.sh`）。

### 4. admin 后台（独立仓库）

- `git clone https://github.com/itwanger/paicoding-admin` 到**本仓库外**目录：上游原版、不修改、不纳入本仓版本管理。
- 本地跑通，连主应用 admin API（`/api/admin/**`）。
- 已核实：主应用 CORS 为反射式放行（`CrossUtil.buildCors` 回写 Origin + 允许凭证），预期无需改代码即可连通；鉴权走 `@Permission(role = UserRole.ADMIN)`，需准备 ADMIN 角色账号（初始数据或 DB 手工提权，方式记入文档）。
- 若发现确实不改主仓代码就连不上的情况：**停下来单独列出问题**，不擅自改代码。

### 5. 冒烟清单（逐项人工验证并记录结果）

| # | 项目 | 结果 |
|---|------|------|
| 1 | 首页加载 | ☐ |
| 2 | 用户注册 / 登录 | ☐ |
| 3 | 发布文章 | ☐ |
| 4 | 文章列表与详情 | ☐ |
| 5 | ES 关键词搜索（`/search/api/list`，含 ES 关闭时 MySQL 降级验证） | ☐ |
| 6 | 评论 | ☐ |
| 7 | 点赞、收藏 | ☐ |
| 8 | 后台管理入口（主应用侧 admin API 可达） | ☐ |
| 9 | RabbitMQ 控制台（:15672）可见 `quere.praise` 队列（switchFlag=true 后点赞触发） | ☐ |
| 10 | 派聪明 AI 助手：配置 LLM key 后能对话 | ☐ |
| 11 | paicoding-admin 本地可登录，管理文章/标签可用 | ☐ |

## Out of Scope

- 任何功能开发。
- 任何上游代码修改。例外：不改就跑不起来的修改——允许，但需在部署文档中单独记录文件、改动内容与原因。

## Acceptance Criteria

- [ ] 冒烟清单 11 项全绿（结果记录在本任务或部署文档中）
- [ ] `docker-compose.dev.yml` 提交，一条命令拉起全部基础设施
- [ ] 完整部署文档沉淀到 `docs/`（含 admin 启动方式与 API 地址配置、踩坑记录、环境问题的根因与解法）
- [ ] 验收方式：不看聊天记录、只看文档能从零重新部署一遍

## 执行约定

- 命令由开发者在终端执行，或由 agent 在开发者确认后执行；报错贴回共同排查。
- 每解决一个环境问题，立即把根因和解法写进部署文档，不停留在对话里。

## References

- `spec/backend/config-environment.md`（多环境/.env/Liquibase）
- `spec/backend/integration-patterns.md`（各中间件开关与降级）
- `docs/本地开发环境配置教程.md`、`docs/安装环境.md`、`launch.sh`、`start-elasticsearch.sh`（上游已有文档）
- fork 基线：tag `upstream-base`（`4b55260a`）
