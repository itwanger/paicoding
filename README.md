<p align="center">
  <a href="https://paicoding.com/">
    <img src="https://paicoding.com/img/logo.svg" width="260px" alt="Java 程序员进阶之路">
  </a>
</p>
一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目👍 。
<br><br>
<p align="center">
  <a href="#公众号"><img src="https://img.shields.io/badge/公众号-楼仔-brightgreen.svg?style=for-the-badge"></a>
  <a href="#公众号"><img src="https://img.shields.io/badge/技术派-交流群-green.svg?style=for-the-badge"></a>
  <a href="https://paicoding.com/" target="_blank"><img src="https://img.shields.io/badge/技术派-首页-critical?style=for-the-badge"></a>
  <a href="https://github.com/itwanger/paicoding-admin" target="_blank"><img src="https://img.shields.io/badge/技术派-管理端-yellow.svg?style=for-the-badge"></a>
  <a href="https://gitee.com/itwanger/paicoding" target="_blank"><img src="https://img.shields.io/badge/码云-项目地址-blue.svg?style=for-the-badge"></a>
</p>

## 配套服务

1. **技术派首页**：[https://paicoding.com](https://paicoding.com)
2. **技术派全套学习教程**：[https://paicoding.com/column](https://paicoding.com/column) 不仅会更新本项目的文档，还会持续更新 Java、Spring、MySQL、Redis、操作系统、计算机网络、数据结构与算法、微服务&分布式、消息队列等方面的硬核内容。我们的宗旨是：**学编程，就上技术派**😁。
3. **技术派管理端**，基于 React 实现：[paicoding-admin](https://github.com/itwanger/paicoding-admin) 。
4. **专属学习圈子**：[不走弯路，少采坑](https://www.yuque.com/itwanger/ydx81p/nksgcaox959w7ie9) 。
5. **项目交流**：想要加群交流项目的朋友，可以加入[楼仔技术交流群](#公众号) 。
6. **码云仓库**：[https://gitee.com/itwanger/paicoding](https://gitee.com/itwanger/paicoding) （国内访问速度更快）

## 项目介绍

技术派（paicoding）是一个前后端分离的 Java 社区实战项目，基于 SpringBoot+MyBatis-Plus 实现，采用 Docker 容器化部署。包括前台社区系统和后台管理系统。前台社区系统包括社区首页、文章推荐、文章搜索、文章发布、文章详情、优质教程、个人中心等模块；后台管理系统包括文章管理、教程管理、统计报表、权限菜单管理、设置等模块。

### 项目演示

#### 前台社区系统

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding](https://github.com/itwanger/paicoding)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding](https://gitee.com/itwanger/paicoding)
- 项目演示地址：[https://paicoding.com](https://paicoding.com)

![](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-front.jpg)

#### 后台社区系统

- 项目仓库（GitHub）：[https://github.com/itwanger/paicoding-admin](https://github.com/itwanger/paicoding-admin)
- 项目仓库（码云）：[https://gitee.com/itwanger/paicoding-admin](https://gitee.com/itwanger/paicoding-admin)
- 项目演示地址：[https://paicoding.com/admin-view](https://paicoding.com/admin-view/)

![](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-admin.jpg)

### 架构图

#### 系统架构图

![](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-system.jpg)

#### 业务架构图

![](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-business.jpg)

#### 开发进度

![](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-plan.jpg)

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

#### 前端工程结构说明

页面放在 ui 模块中：

- resources/static: 静态资源文件，如css/js/image，放在这里
- resources/templates: html相关页面
  - views: 业务相关的页面
    - 定义：
      - 页面/index.html:  这个index.html表示的是这个业务对应的主页面
      - 页面/模块/xxx.html:  若主页面又可以拆分为多个模块页面进行组合，则在这个页面下，新建一个模块目录，下面放对应的html文件
    - article-category-list: 对应 分类文章列表页面，
    - article-detail: 对应文章详情页
      - side-float-action-bar: 文章详情，左边的点赞/收藏/评论浮窗
      - side-recommend-bar: 文章详情右边侧边栏的sidebar
    - article-edit: 对应文章发布页
    - article-search-list: 对应文章搜索页
    - article-tag-list: 对应标签文章列表
    - column-detail：对应专栏阅读详情页
    - column-home: 对应专栏首页
    - home: 全站主页
    - login: 登录页面
    - notice: 通知页面
    - user: 用户个人页
  - error: 错误页面
  - components: 公用的前端页面组件


css 放在 static/css 中：

- components: 公共组件的css
  - navbar: 导航栏样式
  - footer: 底部样式
  - article-item: 文章块展示样式
  - article-footer: 文章底部（点赞、评论等）
  - side-column: 侧边栏（公告等）
- views: 主页面css(直接在主页面内部引入)
  - home: 主页样式
  - article-detail: 详情页样式
  - ...
- three: 第三方css
  - index: 第三方css集合
  - ...
- common: 公共组件的css集合 （直接在公共组件components/layout/header/index.html内引入）
- global: 全局样式（全局的样式控制，注意覆盖问题，直接在公共组件components/layout/header/index.html内引入）


### 技术选型

## 环境搭建

### 开发工具

### 开发环境

### 搭建步骤

#### 本地部署教程

> [本地开发环境手把手教程](docs/本地开发环境配置教程.md)

### 云服务器部署教程

> [环境搭建 & 基于源码的部署教程](docs/安装环境.md)
> [服务器启动教程](docs/服务器启动教程.md)


## 公众号

微信搜索 **楼仔** 关注我们的原创公众号，后台回复「**加群**」即可加入技术交流群，有美团技术大佬、百度技术大佬、小米技术大佬，等你骚扰，学习不再走弯路。

![公众号图片](https://cdn.tobebetterjavaer.com/paicoding/main/paicoding-louzai.jpg)

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2022-2023 技术派（楼仔、一灰、小超、沉默王二）
