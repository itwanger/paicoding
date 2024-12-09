一个基于 Spring Boot3、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等。
<br><br>

## 写在前面

> 这个版本是将原本技术派经过二次开发，将前端使用vue3重写，并且将后端技术栈升级到Spring Boot3之后的版本，同时对项目进行了一些优化，使得项目更加适合二次开发。
> 
> 值得注意的是后端部分接口可能看起来有部分重复，这是因为兼容了直接使用原本Thymleaf中的实现，从而可以对比两种实现

## 一、配套服务
 
1. **前后端分离网址**：[http://www.xuyifei.site](http://www.xuyifei.site)
2. **技术派管理端源码**：[paicoding-admin](https://github.com/itwanger/paicoding-admin)
3. **Vue3前端重构的前端源码**：现已合并到管理端项目中，但仍然是前后端分离模式。

## 二、项目介绍

### 项目演示

#### 前台社区系统

- 项目仓库（GitHub）：[https://github.com/XuYi-fei/Coder-s-platform](https://github.com/XuYi-fei/Coder-s-platform)
- 项目仓库（码云）：[https://gitee.com/XuYi-fei/Coder-s-platform](https://gitee.com/XuYi-fei/Coder-s-platform)
- 前后端分离版本项目演示地址：[http://www.xuyifei.site](http://www.xuyifei.site)

![](https://cdn.tobebetterjavaer.com/images/20230602/d7d341c557e7470d9fb41245e5bb4209.png)

#### 后台社区系统

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding-admin](https://github.com/itwanger/paicoding-admin)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding-admin](https://gitee.com/itwanger/paicoding-admin)
- 项目演示地址：[https://paicoding.com/admin-view](https://paicoding.com/admin/)

![](https://cdn.tobebetterjavaer.com/images/20230602/83139e13a4784c0fbf0adedd8e287c5b.png)


### 组织结构

```
paicoding
├── paicoding-front -- Vue3实现的前端模块
├── paicoding-api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等
├── paicoding-core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
├── paicoding-service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里
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

#### 前端工程结构说明

见[前端Vue版本](https://github.com/XuYi-fei/Coder-s-Corner)

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
|         jwt         | jwt登录                | [https://jwt.io](https://jwt.io)                                                                   |
|       lombok        | Java语言增强库            | [https://projectlombok.org](https://projectlombok.org)                                             |
|        guava        | google开源的java工具集     | [https://github.com/google/guava](https://github.com/google/guava)                                 |
|        vue3         | 强大的前端开发框架            | [hhttps://cn.vuejs.org/](https://cn.vuejs.org/)                                             |
|       swagger       | API文档生成工具            | [https://swagger.io](https://swagger.io)                                                           |
| hibernate-validator | 验证框架                 | [hibernate.org/validator/](hibernate.org/validator/)                                               |
|     quick-media     | 多媒体处理                | [https://github.com/liuyueyi/quick-media](https://github.com/liuyueyi/quick-media)                 |
|      liquibase      | 数据库版本管理              | [https://www.liquibase.com](https://www.liquibase.com)                                             |
|       jackson       | json/xml处理           | [https://www.jackson.com](https://www.jackson.com)                                                 |
|      ip2region      | ip地址                 | [https://github.com/zoujingli/ip2region](https://github.com/zoujingli/ip2region)                   |
|      websocket      | 长连接                  | [https://docs.spring.io/spring/reference/web/websocket.html](https://docs.spring.io/spring/reference/web/websocket.html)                   |
|   sensitive-word    | 敏感词                  | [https://github.com/houbb/sensitive-word](https://github.com/houbb/sensitive-word)                   |
|       chatgpt       | chatgpt              | [https://openai.com/blog/chatgpt](https://openai.com/blog/chatgpt)                   |
|        讯飞星火         | 讯飞星火大模型              | [https://www.xfyun.cn/doc/spark/Web.html](https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E)                   |


## 三、环境搭建

### 开发工具

|        工具        | 说明           | 官网                                                                                                           | 
|:----------------:|--------------|--------------------------------------------------------------------------------------------------------------|
|       IDEA       | java开发工具     | [https://www.jetbrains.com](https://www.jetbrains.com)                                                       |
|     Webstorm     | web开发工具      | [https://www.jetbrains.com/webstorm](https://www.jetbrains.com/webstorm)                                     |
|      Chrome      | 浏览器          | [https://www.google.com/intl/zh-CN/chrome](https://www.google.com/intl/zh-CN/chrome)                         |
|     PicPick      | 图片处理工具       | [https://picpick.app](https://picpick.app)                                                                   |
|     MarkText     | markdown编辑器  | [https://github.com/marktext/marktext](https://github.com/marktext/marktext)                                 |
|       curl       | http终端请求     | [https://curl.se](https://curl.se)                                                                           |
|     Postman      | API接口调试      | [https://www.postman.com](https://www.postman.com)                                                           |
|     draw.io      | 流程图、架构图绘制    | [https://www.diagrams.net/](https://www.diagrams.net/)                                                       |
|      Axure       | 原型图设计工具      | [https://www.axure.com](https://www.axure.com)                                                     |
|      iTerm2      | mac终端        | [https://iterm2.com](https://iterm2.com)                                                                     |
| windows terminal | win终端        | [https://learn.microsoft.com/en-us/windows/terminal/install](https://learn.microsoft.com/en-us/windows/terminal/install) |
|   SwitchHosts    | host管理       | [https://github.com/oldj/SwitchHosts/releases](https://github.com/oldj/SwitchHosts/releases)                 |


### 开发环境

|      工具       | 版本      | 下载                                                                                                                     |
|:-------------:|:--------|------------------------------------------------------------------------------------------------------------------------|
|      jdk      | 17+     | [https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) |
|     maven     | 3.5+    | [https://maven.apache.org/](https://maven.apache.org/)                                                                 |
|     mysql     | 8.0+    | [https://www.mysql.com/downloads/](https://www.mysql.com/downloads/)                                                   |
|     redis     | 6.0+    | [https://redis.io/download/](https://redis.io/download/)                                                               |
| elasticsearch | 8.0.0+  | [https://www.elastic.co/cn/downloads/elasticsearch](https://www.elastic.co/cn/downloads/elasticsearch)                 |
|     nginx     | 1.10+   | [https://nginx.org/en/download.html](https://nginx.org/en/download.html)                                               |
|   rabbitmq    | 3.12+   | [https://www.rabbitmq.com/news.html](https://www.rabbitmq.com/news.html)                                               |
|    ali-oss    | 3.15.1  | [https://help.aliyun.com/document_detail/31946.html](https://help.aliyun.com/document_detail/31946.html)               |
|      git      | 2.34.1  | [http://github.com/](http://github.com/)                                                                               |
|    docker     | 4.10.0+ | [https://docs.docker.com/desktop/](https://docs.docker.com/desktop/)                                                   |
| let's encrypt | https证书 | [https://letsencrypt.org/](https://letsencrypt.org/)                                                                   |

### 搭建步骤

#### 本地部署教程

> [本地开发环境手把手教程](docs/本地开发环境配置教程.md)

### 云服务器部署教程

> [环境搭建 & 基于源码的部署教程](docs/安装环境.md)
> [服务器启动教程](docs/服务器启动教程.md)



