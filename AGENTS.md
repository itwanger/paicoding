# AGENTS.md

面向所有 AI 工具的入口文件（fork 重写版，替换上游 Qoder 遗留内容）。
只保留三类内容：工作流声明、硬性环境事实、关键事实纠错。详细规范一律看 `.trellis/spec/`。

## 1. 工作流声明（最重要）

- 本仓库使用 **Trellis 工作流**：任务、规范、journal 均在 `.trellis/` 下。
- 开发任务必须走 Trellis 节奏：brainstorm → PRD → implement → check → update-spec → finish-work。
- **`.trellis/spec/` 是编码规范的唯一权威来源**；本文件与 spec 冲突时，以 spec 为准。
- git 规则（main 只走 PR、每任务一分支、Conventional Commits）：`.trellis/spec/guides/git-workflow.md`。
- fork 基线 tag：`upstream-base`；上游 remote：`itwanger/paicoding`。

## 2. 硬性环境事实

- **JDK 8 构建**。Spring Boot 2.7.1 + Lombok，高版本 JDK（如 21）会导致 Lombok 注解处理器失败。
- 本机为 **Windows**。
  - TODO（任务 0 部署跑通后回填）：本机 JDK 8 路径、Maven 路径、验证过的构建命令。
- **不要使用 `mvn clean`**，除非明确要求。上游告诫的原因是「JDK 21 + clean 全量重建触发 Lombok 失败」；在正确锁定 JDK 8 前保留此告诫，任务 0 验证构建后重新评估。
- 优先按模块构建：`mvn -pl <module> -am -DskipTests compile`（TODO：任务 0 验证后确认）。

## 3. 技术栈（按代码现实，纠错版）

Spring Boot 2.7.1 / JDK 8 / Maven 多模块 / MyBatis-Plus + MySQL / Redis / Elasticsearch / RabbitMQ / Thymeleaf / Liquibase。

- ⚠️ **没有 MongoDB**：上游 README 技术栈罗列了它，但全仓无依赖与代码（已核实）。计数主链路是 MySQL `user_foot` 表 + Redis。详见 `.trellis/spec/backend/integration-patterns.md`。

## 4. 架构速查

模块分层、DO/DTO/VO、数据访问、异常、日志、中间件、LLM 抽象、多环境配置：
**全部见 `.trellis/spec/backend/`（入口 `index.md`）**，此处不重复。

spec 未覆盖的补充事实：

- 模块依赖图（各模块 pom 已核实）：

  ```
  paicoding-web ──► paicoding-ui, paicoding-service
  paicoding-service ──► paicoding-core, paicoding-api
  paicoding-core ──► paicoding-api
  ```

- Swagger/API 文档：Knife4j（knife4j-openapi2-spring-boot-starter），启动后访问 `/doc.html`。
- 测试框架：JUnit 4 + Spock（`paicoding-web/src/test/groovy/` 存在 Groovy 测试，`spock.version=2.1-groovy-3.0`）。
- Liquibase：`paicoding-web/src/main/resources/liquibase/`，启动自动执行（详见 `.trellis/spec/backend/database-guidelines.md`）。

## 5. 本文件的维护

merge upstream 时若本文件冲突：**以我们的版本为基础，人工甄别上游新增的有效信息后选择性合入**（规则同步记录于 `.trellis/spec/guides/git-workflow.md`）。
