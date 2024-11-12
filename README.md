<p align="center">
  <a href="https://paicoding.com/">
    <img src="https://cdn.tobebetterjavaer.com/images/README/1681354262213.png" alt="技术派" width="400">
  </a>
</p>

一个从技术派main分支开始，拆分单体应用为微服务应用的教程。

[//]: # (一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目👍 。)

[//]: # (<br><br>)

[//]: # (<p align="center">)

[//]: # (  <a href="https://paicoding.com/article/detail/15"><img src="https://img.shields.io/badge/技术派-学习圈子-brightgreen.svg?style=for-the-badge"></a>)

[//]: # (  <a href="https://paicoding.com/" target="_blank"><img src="https://img.shields.io/badge/技术派-首页-critical?style=for-the-badge"></a>)

[//]: # (  <a href="https://github.com/itwanger/paicoding-admin" target="_blank"><img src="https://img.shields.io/badge/技术派-管理端-yellow.svg?style=for-the-badge"></a>)

[//]: # (  <a href="https://gitee.com/itwanger/paicoding" target="_blank"><img src="https://img.shields.io/badge/技术派-码云地址-blue.svg?style=for-the-badge"></a>)

[//]: # (</p>)

[//]: # ()
[//]: # ()
[//]: # (## 一、配套服务)

[//]: # ( )
[//]: # (1. **技术派网址**：[https://paicoding.com]&#40;https://paicoding.com&#41;)

[//]: # (2. **技术派教程**：[https://paicoding.com/column]&#40;https://paicoding.com/column&#41; 目前已更新高并发手册、JVM 手册、Java 并发编程手册、二哥的 Java 进阶之路，以及技术派部分免费教程。我们的宗旨是：**学编程，就上技术派**😁)

[//]: # (3. **技术派管理端源码**：[paicoding-admin]&#40;https://github.com/itwanger/paicoding-admin&#41;)

[//]: # (4. **技术派专属学习圈子**：[不走弯路，少采坑，附 120 篇技术派全套教程]&#40;https://paicoding.com/article/detail/17&#41; )

[//]: # (5. **派聪明AI助手**：AI 时代，怎能掉队，欢迎体验 [技术派的派聪明 AI 助手]&#40;https://paicoding.com/chat&#41;)

[//]: # (6. **码云仓库**：[https://gitee.com/itwanger/paicoding]&#40;https://gitee.com/itwanger/paicoding&#41; （国内访问速度更快）)

[//]: # ()
[//]: # (## 二、项目介绍)

[//]: # ()
[//]: # (### 项目演示)

[//]: # ()
[//]: # (#### 前台社区系统)

[//]: # ()
[//]: # (- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding]&#40;https://github.com/itwanger/paicoding&#41;)

[//]: # (- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding]&#40;https://gitee.com/itwanger/paicoding&#41;)

[//]: # (- 项目演示地址：[https://paicoding.com]&#40;https://paicoding.com&#41;)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/images/20230602/d7d341c557e7470d9fb41245e5bb4209.png&#41;)

[//]: # ()
[//]: # (#### 后台社区系统)

[//]: # ()
[//]: # (- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding-admin]&#40;https://github.com/itwanger/paicoding-admin&#41;)

[//]: # (- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding-admin]&#40;https://gitee.com/itwanger/paicoding-admin&#41;)

[//]: # (- 项目演示地址：[https://paicoding.com/admin-view]&#40;https://paicoding.com/admin/&#41;)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/images/20230602/83139e13a4784c0fbf0adedd8e287c5b.png&#41;)

[//]: # ()
[//]: # (#### 代码展示)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/images/20231205/b8f76cb8e09f4ebca84b3ddd3b61c13e.png&#41;)

[//]: # ()
[//]: # ()
[//]: # (### 架构图)

[//]: # ()
[//]: # (#### 系统架构图)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/paicoding/3da165adfcad0f03d40e13e941ed4afb.png&#41;)

[//]: # ()
[//]: # ()
[//]: # (#### 业务架构图)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-business.jpg&#41;)

[//]: # ()
[//]: # (### 组织结构)

[//]: # ()
[//]: # (```)

[//]: # (paicoding)

[//]: # (├── paicoding-api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等)

[//]: # (├── paicoding-core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）)

[//]: # (├── paicoding-service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里)

[//]: # (├── paicoding-ui -- HTML 前端资源（包括 JavaScript、CSS、Thymeleaf 等）)

[//]: # (├── paicoding-web -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等)

[//]: # (```)

[//]: # ()
[//]: # (#### 环境配置说明)

[//]: # ()
[//]: # (资源配置都放在 `paicoding-web` 模块的资源路径下，通过maven的env进行环境选择切换)

[//]: # ()
[//]: # (当前提供了四种开发环境)

[//]: # ()
[//]: # (- resources-env/dev: 本地开发环境，也是默认环境)

[//]: # (- resources-env/test: 测试环境)

[//]: # (- resources-env/pre: 预发环境)

[//]: # (- resources-env/prod: 生产环境)

[//]: # ()
[//]: # (环境切换命令)

[//]: # ()
[//]: # (```bash)

[//]: # (# 如切换生产环境)

[//]: # (mvn clean install -DskipTests=true -Pprod)

[//]: # (```)

[//]: # ()
[//]: # (#### 配置文件说明)

[//]: # ()
[//]: # (- resources)

[//]: # (  - application.yml: 主配置文件入口)

[//]: # (  - application-config.yml: 全局的站点信息配置文件)

[//]: # (  - logback-spring.xml: 日志打印相关配置文件)

[//]: # (  - liquibase: 由liquibase进行数据库表结构管理)

[//]: # (- resources-env)

[//]: # (  - xxx/application-dal.yml: 定义数据库相关的配置信息)

[//]: # (  - xxx/application-image.yml: 定义上传图片的相关配置信息)

[//]: # (  - xxx/application-web.yml: 定义web相关的配置信息)

[//]: # ()
[//]: # (#### [前端工程结构说明]&#40;docs/前端工程结构说明.md&#41;)

[//]: # ()
[//]: # (### 技术选型)

[//]: # ()
[//]: # (后端技术栈)

[//]: # ()
[//]: # (|         技术          | 说明                   | 官网                                                                                                 |)

[//]: # (|:-------------------:|----------------------|----------------------------------------------------------------------------------------------------|)

[//]: # (| Spring & SpringMVC  | Java全栈应用程序框架和WEB容器实现 | [https://spring.io/]&#40;https://spring.io/&#41;                                                           |)

[//]: # (|     SpringBoot      | Spring应用简化集成开发框架     | [https://spring.io/projects/spring-boot]&#40;https://spring.io/projects/spring-boot&#41;                   |)

[//]: # (|       mybatis       | 数据库orm框架             | [https://mybatis.org]&#40;https://mybatis.org&#41;                                                       |)

[//]: # (|    mybatis-plus     | 数据库orm框架             | [https://baomidou.com/]&#40;https://baomidou.com/&#41;                                                     |)

[//]: # (| mybatis PageHelper  | 数据库翻页插件              | [https://github.com/pagehelper/Mybatis-PageHelper]&#40;https://github.com/pagehelper/Mybatis-PageHelper&#41; |)

[//]: # (|    elasticsearch    | 近实时文本搜索              | [https://www.elastic.co/cn/elasticsearch/service]&#40;https://www.elastic.co/cn/elasticsearch/service&#41; |)

[//]: # (|        redis        | 内存数据存储               | [https://redis.io]&#40;https://redis.io&#41;                                                               |)

[//]: # (|      rabbitmq       | 消息队列                 | [https://www.rabbitmq.com]&#40;https://www.rabbitmq.com&#41;                                               |)

[//]: # (|       mongodb       | NoSql数据库             | [https://www.mongodb.com/]&#40;https://www.mongodb.com/&#41;                                               |)

[//]: # (|        nginx        | 服务器                  | [https://nginx.org]&#40;https://nginx.org&#41;                                                             |)

[//]: # (|       docker        | 应用容器引擎               | [https://www.docker.com]&#40;https://www.docker.com&#41;                                                   |)

[//]: # (|      hikariCP       | 数据库连接                | [https://github.com/brettwooldridge/HikariCP]&#40;https://github.com/brettwooldridge/HikariCP&#41;         |)

[//]: # (|         oss         | 对象存储                 | [https://help.aliyun.com/document_detail/31883.html]&#40;https://help.aliyun.com/document_detail/31883.html&#41;                                               |)

[//]: # (|        https        | 证书                   | [https://letsencrypt.org/]&#40;https://letsencrypt.org/&#41;                                               |)

[//]: # (|         jwt         | jwt登录                | [https://jwt.io]&#40;https://jwt.io&#41;                                                                   |)

[//]: # (|       lombok        | Java语言增强库            | [https://projectlombok.org]&#40;https://projectlombok.org&#41;                                             |)

[//]: # (|        guava        | google开源的java工具集     | [https://github.com/google/guava]&#40;https://github.com/google/guava&#41;                                 |)

[//]: # (|      thymeleaf      | html5模板引擎            | [https://www.thymeleaf.org]&#40;https://www.thymeleaf.org&#41;                                             |)

[//]: # (|       swagger       | API文档生成工具            | [https://swagger.io]&#40;https://swagger.io&#41;                                                           |)

[//]: # (| hibernate-validator | 验证框架                 | [hibernate.org/validator/]&#40;hibernate.org/validator/&#41;                                               |)

[//]: # (|     quick-media     | 多媒体处理                | [https://github.com/liuyueyi/quick-media]&#40;https://github.com/liuyueyi/quick-media&#41;                 |)

[//]: # (|      liquibase      | 数据库版本管理              | [https://www.liquibase.com]&#40;https://www.liquibase.com&#41;                                             |)

[//]: # (|       jackson       | json/xml处理           | [https://www.jackson.com]&#40;https://www.jackson.com&#41;                                                 |)

[//]: # (|      ip2region      | ip地址                 | [https://github.com/zoujingli/ip2region]&#40;https://github.com/zoujingli/ip2region&#41;                   |)

[//]: # (|      websocket      | 长连接                  | [https://docs.spring.io/spring/reference/web/websocket.html]&#40;https://docs.spring.io/spring/reference/web/websocket.html&#41;                   |)

[//]: # (|      sensitive-word      | 敏感词                  | [https://github.com/houbb/sensitive-word]&#40;https://github.com/houbb/sensitive-word&#41;                   |)

[//]: # (|       chatgpt       | chatgpt              | [https://openai.com/blog/chatgpt]&#40;https://openai.com/blog/chatgpt&#41;                   |)

[//]: # (|        讯飞星火         | 讯飞星火大模型              | [https://www.xfyun.cn/doc/spark/Web.html]&#40;https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E&#41;                   |)

[//]: # ()
[//]: # (## 三、技术派教程)

[//]: # (技术派教程共 120+ 篇，从中整理出 20 篇，供大家免费学习。)

[//]: # (- [（🌟 新人必看）技术派系统架构&功能模块一览]&#40;https://paicoding.com/article/detail/15&#41;)

[//]: # (- [（🌟 新人必看）小白如何学习技术派]&#40;https://paicoding.com/article/detail/366&#41;)

[//]: # (- [（🌟 新人必看）如何将技术派写入简历]&#40;https://paicoding.com/article/detail/373&#41;)

[//]: # (- [（🌟 新人必看）技术派架构方案设计]&#40;https://paicoding.com/column/6/5&#41;)

[//]: # (- [（🌟 新人必看）技术派技术方案设计]&#40;https://paicoding.com/article/detail/208&#41;)

[//]: # (- [（🌟 新人必看）技术派项目管理流程]&#40;https://paicoding.com/article/detail/445&#41;)

[//]: # (- [（🌟 新人必看）技术派MVC分层架构]&#40;https://paicoding.com/article/detail/446&#41;)

[//]: # (- [（🌟 新人必看）技术派项目工程搭建手册]&#40;https://paicoding.com/article/detail/459&#41;)

[//]: # (- [（👍 强烈推荐）技术派微信公众号自动登录]&#40;https://paicoding.com/article/detail/448&#41;)

[//]: # (- [（👍 强烈推荐）技术派微信扫码登录实现]&#40;https://paicoding.com/article/detail/453&#41;)

[//]: # (- [（👍 强烈推荐）技术派Session/Cookie身份验证识别]&#40;https://paicoding.com/article/detail/449&#41;)

[//]: # (- [（👍 强烈推荐）技术派Mysql/Redis缓存一致性]&#40;https://paicoding.com/column/6/3&#41;)

[//]: # (- [（👍 强烈推荐）技术派Redis实现用户活跃排行榜]&#40;https://paicoding.com/article/detail/454&#41;)

[//]: # (- [（👍 强烈推荐）技术派消息队列RabbitMQ]&#40;https://paicoding.com/column/6/2&#41;)

[//]: # (- [（👍 强烈推荐）技术派消息队列RabbitMQ连接池]&#40;https://paicoding.com/column/6/1&#41;)

[//]: # (- [（👍 强烈推荐）技术派消息队列Kafka]&#40;https://paicoding.com/article/detail/460&#41;)

[//]: # (- [（👍 强烈推荐）技术派Cancal实现MySQL和ES同步]&#40;https://paicoding.com/column/6/8&#41;)

[//]: # (- [（👍 强烈推荐）技术派ES实现查询]&#40;https://paicoding.com/article/detail/341&#41;)

[//]: # (- [（👍 强烈推荐）技术派定时任务实现]&#40;https://paicoding.com/article/detail/457&#41;)

[//]: # (- [（👍 扬帆起航）送给坚持到最后的自己，一起杨帆起航]&#40;https://paicoding.com/article/detail/447&#41;)

[//]: # ()
[//]: # ()
[//]: # (## 四、环境搭建)

[//]: # ()
[//]: # (### 开发工具)

[//]: # ()
[//]: # (|        工具        | 说明           | 官网                                                                                                           | )

[//]: # (|:----------------:|--------------|--------------------------------------------------------------------------------------------------------------|)

[//]: # (|       IDEA       | java开发工具     | [https://www.jetbrains.com]&#40;https://www.jetbrains.com&#41;                                                       |)

[//]: # (|     Webstorm     | web开发工具      | [https://www.jetbrains.com/webstorm]&#40;https://www.jetbrains.com/webstorm&#41;                                     |)

[//]: # (|      Chrome      | 浏览器          | [https://www.google.com/intl/zh-CN/chrome]&#40;https://www.google.com/intl/zh-CN/chrome&#41;                         |)

[//]: # (|   ScreenToGif    | gif录屏        | [https://www.screentogif.com]&#40;https://www.screentogif.com&#41;                                                   |)

[//]: # (|     SniPaste     | 截图           | [https://www.snipaste.com]&#40;https://www.snipaste.com&#41;                                                         |)

[//]: # (|     PicPick      | 图片处理工具       | [https://picpick.app]&#40;https://picpick.app&#41;                                                                   |)

[//]: # (|     MarkText     | markdown编辑器  | [https://github.com/marktext/marktext]&#40;https://github.com/marktext/marktext&#41;                                 |)

[//]: # (|       curl       | http终端请求     | [https://curl.se]&#40;https://curl.se&#41;                                                                           |)

[//]: # (|     Postman      | API接口调试      | [https://www.postman.com]&#40;https://www.postman.com&#41;                                                           |)

[//]: # (|     draw.io      | 流程图、架构图绘制    | [https://www.diagrams.net/]&#40;https://www.diagrams.net/&#41;                                                       |)

[//]: # (|      Axure       | 原型图设计工具      | [https://www.axure.com]&#40;https://www.axure.com&#41;                                                     |)

[//]: # (|     navicat      | 数据库连接工具      | [https://www.navicat.com]&#40;https://www.navicat.com&#41;                                                           |)

[//]: # (|     DBeaver      | 免费开源的数据库连接工具 | [https://dbeaver.io]&#40;https://dbeaver.io&#41;                                                                     |)

[//]: # (|      iTerm2      | mac终端        | [https://iterm2.com]&#40;https://iterm2.com&#41;                                                                     |)

[//]: # (| windows terminal | win终端        | [https://learn.microsoft.com/en-us/windows/terminal/install]&#40;https://learn.microsoft.com/en-us/windows/terminal/install&#41; |)

[//]: # (|   SwitchHosts    | host管理       | [https://github.com/oldj/SwitchHosts/releases]&#40;https://github.com/oldj/SwitchHosts/releases&#41;                 |)

[//]: # ()
[//]: # ()
[//]: # (### 开发环境)

[//]: # ()
[//]: # (|      工具       | 版本        | 下载                                                                                                                     |)

[//]: # (|:-------------:|:----------|------------------------------------------------------------------------------------------------------------------------|)

[//]: # (|      jdk      | 1.8+      | [https://www.oracle.com/java/technologies/downloads/#java8]&#40;https://www.oracle.com/java/technologies/downloads/#java8&#41; |)

[//]: # (|     maven     | 3.4+      | [https://maven.apache.org/]&#40;https://maven.apache.org/&#41;                                                                 |)

[//]: # (|     mysql     | 5.7+/8.0+ | [https://www.mysql.com/downloads/]&#40;https://www.mysql.com/downloads/&#41;                                                   |)

[//]: # (|     redis     | 5.0+      | [https://redis.io/download/]&#40;https://redis.io/download/&#41;                                                               |)

[//]: # (| elasticsearch | 8.0.0+    | [https://www.elastic.co/cn/downloads/elasticsearch]&#40;https://www.elastic.co/cn/downloads/elasticsearch&#41;                 |)

[//]: # (|     nginx     | 1.10+     | [https://nginx.org/en/download.html]&#40;https://nginx.org/en/download.html&#41;                                               |)

[//]: # (|   rabbitmq    | 3.10.14+  | [https://www.rabbitmq.com/news.html]&#40;https://www.rabbitmq.com/news.html&#41;                                               |)

[//]: # (|    ali-oss    | 3.15.1    | [https://help.aliyun.com/document_detail/31946.html]&#40;https://help.aliyun.com/document_detail/31946.html&#41;               |)

[//]: # (|      git      | 2.34.1    | [http://github.com/]&#40;http://github.com/&#41;                                                                               |)

[//]: # (|    docker     | 4.10.0+   | [https://docs.docker.com/desktop/]&#40;https://docs.docker.com/desktop/&#41;                                                   |)

[//]: # (| let's encrypt | https证书   | [https://letsencrypt.org/]&#40;https://letsencrypt.org/&#41;                                                                   |)

[//]: # ()
[//]: # (### 搭建步骤)

[//]: # ()
[//]: # (#### 本地部署教程)

[//]: # ()
[//]: # (> [本地开发环境手把手教程]&#40;docs/本地开发环境配置教程.md&#41;)

[//]: # ()
[//]: # (### 云服务器部署教程)

[//]: # ()
[//]: # (> [环境搭建 & 基于源码的部署教程]&#40;docs/安装环境.md&#41;)

[//]: # (> [服务器启动教程]&#40;docs/服务器启动教程.md&#41;)

[//]: # ()
[//]: # (## 五、友情链接)

[//]: # ()
[//]: # (- [toBeBetterjavaer]&#40;https://github.com/itwanger/toBeBetterJavaer&#41; ：一份通俗易懂、风趣幽默的Java学习指南，内容涵盖Java基础、Java并发编程、Java虚拟机、Java企业级开发、Java面试等核心知识点。学Java，就认准二哥的Java进阶之路😄)

[//]: # (- [paicoding-admin]&#40;https://github.com/itwanger/paicoding-admin&#41; ：🚀🚀🚀 paicoding-admin，技术派管理端，基于 React18、React-Router v6、React-Hooks、Redux、TypeScript、Vite3、Ant-Design 5.x、Hook Admin、ECharts 的一套社区管理系统，够惊艳哦。)

[//]: # ()
[//]: # (## 六、鸣谢)

[//]: # ()
[//]: # (技术派收到了 [Jetbrains]&#40;https://jb.gg/OpenSourceSupport&#41; 多份 Licenses（详情戳 [这里]&#40;https://paicoding.com/article/detail/331&#41; ），并已分配给项目 [活跃开发者]&#40;https://github.com/itwanger/paicoding/graphs/contributors&#41; ，非常感谢 Jetbrains 对开源社区的支持。)

[//]: # ()
[//]: # (![JetBrains Logo &#40;Main&#41; logo]&#40;https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg&#41;)

[//]: # ()
[//]: # ()
[//]: # (## 七、star 趋势图)

[//]: # ()
[//]: # ([![Star History Chart]&#40;https://api.star-history.com/svg?repos=itwanger/paicoding&type=Date&#41;]&#40;https://star-history.com/#itwanger/paicoding&Date&#41;)

[//]: # ()
[//]: # (## 八、公众号)

[//]: # ()
[//]: # (GitHub 上标星 10000+ 的开源知识库《 [二哥的 Java 进阶之路]&#40;https://github.com/itwanger/toBeBetterJavaer&#41; 》第一版 PDF 终于来了！包括Java基础语法、数组&字符串、OOP、集合框架、Java IO、异常处理、Java 新特性、网络编程、NIO、并发编程、JVM等等，共计 32 万余字，可以说是通俗易懂、风趣幽默……详情戳：[太赞了，GitHub 上标星 8700+ 的 Java 教程]&#40;https://javabetter.cn/overview/&#41;)

[//]: # ()
[//]: # (微信搜 **沉默王二** 或扫描下方二维码关注二哥的原创公众号，回复 **222** 即可免费领取。)

[//]: # ()
[//]: # (![]&#40;https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/gongzhonghao.png&#41;)

[//]: # ()
[//]: # (## 九、许可证)

[//]: # ()
[//]: # ([Apache License 2.0]&#40;https://github.com/itwanger/paicoding/edit/main/README.md&#41;)

[//]: # ()
[//]: # (Copyright &#40;c&#41; 2022-2023 技术派（楼仔、沉默王二、一灰、小超）)

