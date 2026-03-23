<p align="center">
  <a href="https://paicoding.com/">
    <img src="https://cdn.tobebetterjavaer.com/images/README/1681354262213.png" alt="技术派，让技术也能很好玩" width="400">
  </a>
</p>
一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目👍 。
<br><br>
<p align="center">
  <a href="https://paicoding.com/article/detail/15"><img src="https://img.shields.io/badge/技术派-学习圈子-brightgreen.svg?style=for-the-badge"></a>
  <a href="https://paicoding.com/" target="_blank"><img src="https://img.shields.io/badge/技术派-首页-critical?style=for-the-badge"></a>
  <a href="https://github.com/itwanger/paicoding-admin" target="_blank"><img src="https://img.shields.io/badge/技术派-管理端-yellow.svg?style=for-the-badge"></a>
  <a href="https://gitee.com/itwanger/paicoding" target="_blank"><img src="https://img.shields.io/badge/技术派-码云地址-blue.svg?style=for-the-badge"></a>
</p>


## 一、配套服务
 
1. **技术派网址**：[https://paicoding.com](https://paicoding.com)
2. **技术派教程**：[https://paicoding.com/column](https://paicoding.com/column) 目前已更新高并发手册、JVM 手册、Java 并发编程手册、二哥的 Java 进阶之路，以及技术派部分免费教程。我们的宗旨是：**学编程，就上技术派**😁
3. **技术派管理端源码**：[paicoding-admin](https://github.com/itwanger/paicoding-admin)
4. **技术派专属学习圈子**：[不走弯路，少采坑，附 120 篇技术派全套教程](https://paicoding.com/article/detail/17) 
5. **派聪明AI助手**：AI 时代，怎能掉队，欢迎体验 [技术派的派聪明 AI 助手](https://paicoding.com/chat)
6. **码云仓库**：[https://gitee.com/itwanger/paicoding](https://gitee.com/itwanger/paicoding) （国内访问速度更快）

## 二、项目介绍

### 项目演示

#### 前台社区系统

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding](https://github.com/itwanger/paicoding)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding](https://gitee.com/itwanger/paicoding)
- 项目演示地址：[https://paicoding.com](https://paicoding.com)

![技术派首页](https://cdn.tobebetterjavaer.com/images/20230602/d7d341c557e7470d9fb41245e5bb4209.png)

#### Vue 版前后端分离版本

这个版本对技术派进行了二次开发，将用户端的前端 UI 使用 Vue3 重写，并且将后端升级到 Spring Boot 3 版本，喜欢 Vue3 或者 Spring Boot 3 版本的球友可以看看这个分支。

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding/tree/springboot3%26vue3](https://github.com/itwanger/paicoding/tree/springboot3%26vue3)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding/tree/springboot3%26vue3](https://gitee.com/itwanger/paicoding/tree/springboot3%26vue3)
- 项目演示地址（球友小灰飞）：[https://www.xuyifei.site/](https://www.xuyifei.site/)

![编程汇vue3+Spring Boot3](https://cdn.tobebetterjavaer.com/paicoding/README-1799f2f840fc4687a1cda4486782a07a.png)


#### 后台社区系统

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding-admin](https://github.com/itwanger/paicoding-admin)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding-admin](https://gitee.com/itwanger/paicoding-admin)
- 项目演示地址：[https://paicoding.com/admin-view](https://paicoding.com/admin/)

![技术派后台管理系统](https://cdn.tobebetterjavaer.com/images/20230602/83139e13a4784c0fbf0adedd8e287c5b.png)

admin 端部署写在了 paicoding-admin 项目的 README.md 中，请注意查看⚠️。

#### 代码展示

![技术派源码结构](https://cdn.tobebetterjavaer.com/images/20231205/b8f76cb8e09f4ebca84b3ddd3b61c13e.png)


### 架构图

#### 系统架构图

![技术派系统架构图](https://cdn.tobebetterjavaer.com/paicoding/3da165adfcad0f03d40e13e941ed4afb.png)


#### 业务架构图

![技术派业务架构图](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-business.jpg)

### 组织结构

```
paicoding
├── paicoding-api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等
├── paicoding-core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
├── paicoding-service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里
├── paicoding-ui -- HTML 前端资源（包括 JavaScript、CSS、Thymeleaf 等）
├── paicoding-web -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等
```

#### 环境配置说明

资源配置都放在 `paicoding-web` 模块的资源路径下，通过maven的env进行环境选择切换

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

#### 配置文件说明

- resources
  - application.yml: 主配置文件入口
  - application-config.yml: 全局的站点信息配置文件
  - logback-spring.xml: 日志打印相关配置文件
  - liquibase: 由liquibase进行数据库表结构管理
- resources-env
  - xxx/application-dal.yml: 定义数据库相关的配置信息
  - xxx/application-image.yml: 定义上传图片的相关配置信息
  - xxx/application-web.yml: 定义web相关的配置信息

#### `.env` 配置说明

项目支持在仓库根目录通过 `.env` / `.env.local` 管理本地或服务器上的敏感配置。

- 首次使用时，可复制 `.env.example` 为 `.env`
- Spring Boot 启动前会自动加载 `.env` / `.env.local`
- `launch.sh` 和 `deploy.sh` 启动 jar 时，也会自动 `source` 这两个文件
- `.env.local` 适合放机器本地专属配置，优先级高于 `.env`
- 进程环境变量和 `-D` 启动参数优先级仍然更高
- `.env`、`.env.local` 已加入 `.gitignore`，不会提交到仓库

示例：

```bash
cp .env.example .env
```

常见配置包括：

- `PAICODING_DB_PASSWORD`
- `PAICODING_OPENAI_API_KEY`
- `PAICODING_ZHIPU_API_SECRET_KEY`
- `PAICODING_XUNFEI_APP_ID`
- `PAICODING_XUNFEI_API_KEY`
- `PAICODING_XUNFEI_API_SECRET`
- `PAICODING_XUNFEI_API_PASSWORD`
- `PAICODING_DEEPSEEK_API_KEY`
- `PAICODING_DOUBAO_API_KEY`
- `PAICODING_DOUBAO_ENDPOINT`
- `PAICODING_WX_APP_SECRET`
- `PAICODING_OSS_AK`
- `PAICODING_OSS_SK`
- `PAICODING_OSS_ENDPOINT`
- `PAICODING_OSS_BUCKET`
- `PAICODING_OSS_HOST`

[前端工程结构说明](docs/前端工程结构说明.md)

### 技术选型

后端技术栈

|         技术          | 说明                   | 官网                                                                                                 |
|:-------------------:|----------------------|----------------------------------------------------------------------------------------------------|
| Spring & SpringMVC  | Java全栈应用程序框架和WEB容器实现 | [https://spring.io/](https://spring.io/)                                                           |
|     SpringBoot      | Spring应用简化集成开发框架     | [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)                   |
|       mybatis       | 数据库orm框架             | [https://mybatis.org](https://mybatis.org)                                                       |
|    mybatis-plus     | 数据库orm框架             | [https://baomidou.com/](https://baomidou.com/)                                                     |
| mybatis PageHelper  | 数据库翻页插件              | [https://github.com/pagehelper/Mybatis-PageHelper](https://github.com/pagehelper/Mybatis-PageHelper) |
|    elasticsearch    | 近实时文本搜索              | [https://www.elastic.co/cn/elasticsearch/service](https://www.elastic.co/cn/elasticsearch/service) |
|        redis        | 内存数据存储               | [https://redis.io](https://redis.io)                                                               |
|      rabbitmq       | 消息队列                 | [https://www.rabbitmq.com](https://www.rabbitmq.com)                                               |
|       mongodb       | NoSql数据库             | [https://www.mongodb.com/](https://www.mongodb.com/)                                               |
|        nginx        | 服务器                  | [https://nginx.org](https://nginx.org)                                                             |
|       docker        | 应用容器引擎               | [https://www.docker.com](https://www.docker.com)                                                   |
|      hikariCP       | 数据库连接                | [https://github.com/brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP)         |
|         oss         | 对象存储                 | [https://help.aliyun.com/document_detail/31883.html](https://help.aliyun.com/document_detail/31883.html)                                               |
|        https        | 证书                   | [https://letsencrypt.org/](https://letsencrypt.org/)                                               |
|         jwt         | jwt登录                | [https://jwt.io](https://jwt.io)                                                                   |
|       lombok        | Java语言增强库            | [https://projectlombok.org](https://projectlombok.org)                                             |
|        guava        | google开源的java工具集     | [https://github.com/google/guava](https://github.com/google/guava)                                 |
|      thymeleaf      | html5模板引擎            | [https://www.thymeleaf.org](https://www.thymeleaf.org)                                             |
|       swagger       | API文档生成工具            | [https://swagger.io](https://swagger.io)                                                           |
| hibernate-validator | 验证框架                 | [hibernate.org/validator/](hibernate.org/validator/)                                               |
|     quick-media     | 多媒体处理                | [https://github.com/liuyueyi/quick-media](https://github.com/liuyueyi/quick-media)                 |
|      liquibase      | 数据库版本管理              | [https://www.liquibase.com](https://www.liquibase.com)                                             |
|       jackson       | json/xml处理           | [https://www.jackson.com](https://www.jackson.com)                                                 |
|      ip2region      | ip地址                 | [https://github.com/zoujingli/ip2region](https://github.com/zoujingli/ip2region)                   |
|      websocket      | 长连接                  | [https://docs.spring.io/spring/reference/web/websocket.html](https://docs.spring.io/spring/reference/web/websocket.html)                   |
|      sensitive-word      | 敏感词                  | [https://github.com/houbb/sensitive-word](https://github.com/houbb/sensitive-word)                   |
|       chatgpt       | chatgpt              | [https://openai.com/blog/chatgpt](https://openai.com/blog/chatgpt)                   |
|        讯飞星火         | 讯飞星火大模型              | [https://www.xfyun.cn/doc/spark/Web.html](https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E)                   |

## 三、技术派教程

技术派教程共 120+ 篇，从中整理出 20 篇，供大家免费学习。
- [（🌟 新人必看）技术派系统架构&功能模块一览](https://paicoding.com/article/detail/15)
- [（🌟 新人必看）小白如何学习技术派](https://paicoding.com/article/detail/366)
- [（🌟 新人必看）如何将技术派写入简历](https://paicoding.com/article/detail/373)
- [（🌟 新人必看）技术派架构方案设计](https://paicoding.com/column/6/5)
- [（🌟 新人必看）技术派技术方案设计](https://paicoding.com/article/detail/208)
- [（🌟 新人必看）技术派项目管理流程](https://paicoding.com/article/detail/445)
- [（🌟 新人必看）技术派MVC分层架构](https://paicoding.com/article/detail/446)
- [（🌟 新人必看）技术派项目工程搭建手册](https://paicoding.com/article/detail/459)
- [（👍 强烈推荐）技术派微信公众号自动登录](https://paicoding.com/article/detail/448)
- [（👍 强烈推荐）技术派微信扫码登录实现](https://paicoding.com/article/detail/453)
- [（👍 强烈推荐）技术派Session/Cookie身份验证识别](https://paicoding.com/article/detail/449)
- [（👍 强烈推荐）技术派Mysql/Redis缓存一致性](https://paicoding.com/column/6/3)
- [（👍 强烈推荐）技术派Redis实现用户活跃排行榜](https://paicoding.com/article/detail/454)
- [（👍 强烈推荐）技术派消息队列RabbitMQ](https://paicoding.com/column/6/2)
- [（👍 强烈推荐）技术派消息队列RabbitMQ连接池](https://paicoding.com/column/6/1)
- [（👍 强烈推荐）技术派消息队列Kafka](https://paicoding.com/article/detail/460)
- [（👍 强烈推荐）技术派Cancal实现MySQL和ES同步](https://paicoding.com/column/6/8)
- [（👍 强烈推荐）技术派ES实现查询](https://paicoding.com/article/detail/341)
- [（👍 强烈推荐）技术派定时任务实现](https://paicoding.com/article/detail/457)
- [（👍 扬帆起航）送给坚持到最后的自己，一起杨帆起航](https://paicoding.com/article/detail/447)


## 四、环境搭建

### 开发工具

|        工具        | 说明           | 官网                                                                                                           | 
|:----------------:|--------------|--------------------------------------------------------------------------------------------------------------|
|       IDEA       | java开发工具     | [https://www.jetbrains.com](https://www.jetbrains.com)                                                       |
|     Webstorm     | web开发工具      | [https://www.jetbrains.com/webstorm](https://www.jetbrains.com/webstorm)                                     |
|      Chrome      | 浏览器          | [https://www.google.com/intl/zh-CN/chrome](https://www.google.com/intl/zh-CN/chrome)                         |
|   ScreenToGif    | gif录屏        | [https://www.screentogif.com](https://www.screentogif.com)                                                   |
|     SniPaste     | 截图           | [https://www.snipaste.com](https://www.snipaste.com)                                                         |
|     PicPick      | 图片处理工具       | [https://picpick.app](https://picpick.app)                                                                   |
|     MarkText     | markdown编辑器  | [https://github.com/marktext/marktext](https://github.com/marktext/marktext)                                 |
|       curl       | http终端请求     | [https://curl.se](https://curl.se)                                                                           |
|     Postman      | API接口调试      | [https://www.postman.com](https://www.postman.com)                                                           |
|     draw.io      | 流程图、架构图绘制    | [https://www.diagrams.net/](https://www.diagrams.net/)                                                       |
|      Axure       | 原型图设计工具      | [https://www.axure.com](https://www.axure.com)                                                     |
|     navicat      | 数据库连接工具      | [https://www.navicat.com](https://www.navicat.com)                                                           |
|     DBeaver      | 免费开源的数据库连接工具 | [https://dbeaver.io](https://dbeaver.io)                                                                     |
|      iTerm2      | mac终端        | [https://iterm2.com](https://iterm2.com)                                                                     |
| windows terminal | win终端        | [https://learn.microsoft.com/en-us/windows/terminal/install](https://learn.microsoft.com/en-us/windows/terminal/install) |
|   SwitchHosts    | host管理       | [https://github.com/oldj/SwitchHosts/releases](https://github.com/oldj/SwitchHosts/releases)                 |


### 开发环境

|      工具       | 版本        | 下载                                                                                                                     |
|:-------------:|:----------|------------------------------------------------------------------------------------------------------------------------|
|      jdk      | 1.8+      | [https://www.oracle.com/java/technologies/downloads/#java8](https://www.oracle.com/java/technologies/downloads/#java8) |
|     maven     | 3.4+      | [https://maven.apache.org/](https://maven.apache.org/)                                                                 |
|     mysql     | 5.7+/8.0+ | [https://www.mysql.com/downloads/](https://www.mysql.com/downloads/)                                                   |
|     redis     | 5.0+      | [https://redis.io/download/](https://redis.io/download/)                                                               |
| elasticsearch | 8.0.0+    | [https://www.elastic.co/cn/downloads/elasticsearch](https://www.elastic.co/cn/downloads/elasticsearch)                 |
|     nginx     | 1.10+     | [https://nginx.org/en/download.html](https://nginx.org/en/download.html)                                               |
|   rabbitmq    | 3.10.14+  | [https://www.rabbitmq.com/news.html](https://www.rabbitmq.com/news.html)                                               |
|    ali-oss    | 3.15.1    | [https://help.aliyun.com/document_detail/31946.html](https://help.aliyun.com/document_detail/31946.html)               |
|      git      | 2.34.1    | [http://github.com/](http://github.com/)                                                                               |
|    docker     | 4.10.0+   | [https://docs.docker.com/desktop/](https://docs.docker.com/desktop/)                                                   |
| let's encrypt | https证书   | [https://letsencrypt.org/](https://letsencrypt.org/)                                                                   |

### 搭建步骤

#### 本地部署教程

> [本地开发环境手把手教程](docs/本地开发环境配置教程.md)

本地启动前，建议先准备 `.env`：

```bash
cp .env.example .env
```

如果通过脚本启动：

```bash
./launch.sh start
```

脚本会自动加载当前目录下的 `.env` / `.env.local`。

### 云服务器部署教程

> [环境搭建 & 基于源码的部署教程](docs/安装环境.md)
> [服务器启动教程](docs/服务器启动教程.md)

服务器上如果使用 `./launch.sh start` 或 `./deploy.sh restart` 启动，也需要提前在脚本所在目录准备 `.env` 或通过系统环境变量注入对应配置。

## 五、友情链接

- [toBeBetterjavaer](https://github.com/itwanger/toBeBetterJavaer) ：一份通俗易懂、风趣幽默的Java学习指南，内容涵盖Java基础、Java并发编程、Java虚拟机、Java企业级开发、Java面试等核心知识点。学Java，就认准二哥的Java进阶之路😄
- [paicoding-admin](https://github.com/itwanger/paicoding-admin) ：🚀🚀🚀 paicoding-admin，技术派管理端，基于 React18、React-Router v6、React-Hooks、Redux、TypeScript、Vite3、Ant-Design 5.x、Hook Admin、ECharts 的一套社区管理系统，够惊艳哦。

## 六、鸣谢

技术派收到了 [Jetbrains](https://jb.gg/OpenSourceSupport) 多份 Licenses（详情戳 [这里](https://paicoding.com/article/detail/331) ），并已分配给项目 [活跃开发者](https://github.com/itwanger/paicoding/graphs/contributors) ，非常感谢 Jetbrains 对开源社区的支持。

![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)


## 七、star 趋势图

[![Star History Chart](https://api.star-history.com/svg?repos=itwanger/paicoding&type=Date)](https://star-history.com/#itwanger/paicoding&Date)

## 八、公众号

GitHub 上标星 13000+ 的开源知识库《 [二哥的 Java 进阶之路](https://github.com/itwanger/toBeBetterJavaer) 》第一版 PDF 终于来了！包括Java基础语法、数组&字符串、OOP、集合框架、Java IO、异常处理、Java 新特性、网络编程、NIO、并发编程、JVM等等，共计 32 万余字，可以说是通俗易懂、风趣幽默……详情戳：[太赞了，GitHub 上标星 13000+ 的 Java 教程](https://javabetter.cn/overview/)

微信搜 **沉默王二** 或扫描下方二维码关注二哥的原创公众号，回复 **222** 即可免费领取。

![沉默王二公众号](https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/gongzhonghao.png)

## 九、更新记录

[关键版本迭代记录](docs/version.md)

## 十、许可证

[Apache License 2.0](https://github.com/itwanger/paicoding/edit/main/README.md)

Copyright (c) 2022-2025 技术派（楼仔、沉默王二、一灰、小超、小灰飞）
