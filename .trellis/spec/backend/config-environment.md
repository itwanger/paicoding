# Config & Environment

> 多环境切换、敏感配置注入与动态配置（提取自上游真实代码）。任务 0（本地部署）直接依赖本文件。

---

## 多环境机制

- 环境目录：`paicoding-web/src/main/resources-env/{dev,test,pre,prod}/`，每个环境一套 `application-{dal,web,ai,rabbitmq,image,login,pay,vod}.yml`。
- 切换：根 `pom.xml:187` Maven profiles（`dev` 默认激活，`<env>dev</env>`），构建时把对应目录并入 classpath。本地开发零参数即为 dev。
- 主配置 `paicoding-web/src/main/resources/application.yml` 通过 `spring.config.import: application-dal.yml,application-web.yml,...` 聚合所有分片配置。

## 敏感配置注入（.env）

- `.env.example`（仓库根）→ `cp .env.example .env`；`core/util/DotenvUtil.java` 在 Spring 启动前把 `.env` / `.env.local` 载入 JVM system properties（环境变量与 `-D` 优先级更高）。
- `launch.sh` / `deploy.sh` 启动 jar 前也会 source 这些文件。
- yml 中全部用 `${PAICODING_XXX:default}` 占位：数据库 `${PAICODING_DB_PASSWORD:}`、ES `${PAICODING_ES_*}`、各 LLM key `${PAICODING_*_API_KEY:}`、OSS/微信等。
- 本地可跑的最小集：数据库密码；ES（`PAICODING_ES_OPEN` 默认 false）、RabbitMQ（`switchFlag: false`）、LLM key 均可缺省关闭。

## 动态配置

- `core/autoconf/DynamicConfigContainer.java`：运行时动态配置容器（DB 表 `global_conf` 驱动，支持 `ConfigRefreshEvent` 刷新；含敏感值脱敏，见 `DynamicConfigContainerRedactionTest`）。
- 读取配置的统一入口之一：`SpringUtil.getConfig("rabbitmq.switchFlag")`（`RabbitmqServiceImpl.enabled()`）。

## 数据库迁移

- Liquibase 启动自动执行：`spring.liquibase.change-log: classpath:liquibase/master.xml`，详见 `database-guidelines.md`。

## 已有部署文档（任务 0 参考）

- `docs/本地开发环境配置教程.md`（上游：JDK8+/Maven/MySQL 8.x+）
- `docs/安装环境.md`、`docs/服务器启动教程.md`、`launch.sh`、`start-elasticsearch.sh`
- 未发现官方 docker-compose 文件（仓库根与 scripts/ 均无）——任务 0 自建 `docker-compose.dev.yml`（仓库根目录，fork 新增、仅本地开发用），服务版本对齐上游 README，**不含 Mongo**（见 integration-patterns.md）。
- 后台管理端是独立仓库 `itwanger/paicoding-admin`（React，连主应用 admin API），不在本仓；本地部署时 clone 上游原版到仓外目录运行，不修改、不纳入版本管理。
