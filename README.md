quick-forum
---

社区工程原型

## 结构说明

- forum-web: Web前端入口，包括权限身份校验、全局异常处理等
- forum-ui：前端资源包
- forum-service: 核心的服务包，db操作，服务封装在这里
- forum-core: 通用模块，如工具包util， 如通用的组件放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）

## 本地部署教程

> [本地开发环境手把手教程](docs/本地开发环境配置教程.md)

## 云服务器部署教程

> [环境搭建 & 基于源码的部署教程](docs/安装环境.md)
> [服务器启动教程](docs/服务器启动教程.md)

## 项目结构说明

**当前项目工程模块**

- [forum-api](forum-api): 定义一些通用的枚举、实体类定义、DO\DTO\VO等
- [forum-core](forum-core): 核心工具组件相关的模块
- [forum-service](forum-service): 服务模块，业务相关的主要逻辑，db的操作都在这里
- [forum-ui](forum-ui): html前端资源
- [forum-web](forum-web): web模块，http入口，项目启动的入口

**环境配置说明**

资源配置都放在 `forum-web` 模块的资源路径下，通过maven的env进行环境选择切换

当前提供了四种开发环境

- resources-env/dev: 本地开发环境，也是默认环境
- resources-env/test: 测试环境
- resources-env/pre: 预发环境
- resources-env/prod: 生产环境

环境切换命令

```bash
# 如切换生产环境
mvn clean install -DskipTests=true -Pprod
```

**配置文件说明**

- resources
  - application.yml: 主配置文件入口
  - application-config.yml: 全局的站点信息配置文件
  - logback-spring.xml: 日志打印相关配置文件
  - schema-all.sql: 项目中所有表结构定义sql文件
  - init-data.sql: 初始化数据sql文件
  - schema.sql, test-data.sql: 开发阶段的sql文件，后续会删除，不用关注
- resources-env
  - xxx/application-dal.yml: 定义数据库相关的配置信息
  - xxx/application-image.yml: 定义上传图片的相关配置信息
  - xxx/application-web.yml: 定义web相关的配置信息