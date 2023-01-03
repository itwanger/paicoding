INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, status, deleted)
VALUES(1, 1, 1, '技术派全方位视角解读', '关于技术派', '', '技术派的使用说明介绍', 1, 2, '', 1, 0);


INSERT INTO article_detail
(article_id, version, content)
VALUES(1, 2, '技术派
---

技术派是一个基于SpringBoot实现完全开源的技术论坛社区


## 本地部署教程

> [本地开发环境手把手教程](docs/本地开发环境配置教程.md)

## 云服务器部署教程

> [环境搭建 & 基于源码的部署教程](docs/安装环境.md)
> [服务器启动教程](docs/服务器启动教程.md)

## 项目结构说明

**当前项目工程模块**

- [forum-api](forum-api): 定义一些通用的枚举、实体类定义、DO\DTO\VO等
- [forum-core](forum-core): 核心工具组件相关的模块，如工具包util， 如通用的组件放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
- [forum-service](forum-service): 服务模块，业务相关的主要逻辑，db的操作都在这里
- [forum-ui](forum-ui): html前端资源
- [forum-web](forum-web): web模块，http入口，项目启动的入口，包括权限身份校验、全局异常处理等

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
    - liquibase: 由liquibase进行数据库表结构管理
- resources-env
    - xxx/application-dal.yml: 定义数据库相关的配置信息
    - xxx/application-image.yml: 定义上传图片的相关配置信息
    - xxx/application-web.yml: 定义web相关的配置信息

### 前端工程结构说明

#### 前端页面都放在 ui 模块中

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


#### 前端 css 全部放在 static/css 中

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
')
