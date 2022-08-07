quick-forum
---

社区工程原型

## 结构说明

- forum-web: web入口，权限身份校验，全局异常处理等
- forum-ui：前端资源包
- forum-service: 核心的服务包，db操作，服务封装在这里
- forum-core: 通用模块，如工具包util， 如通用的组件放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）

## 初始化说明

- 创建数据库, 命名为 forum
- 初始化表结构和demo数据, 可以直接导入 [test-data.sql](forum-web/src/main/resources/test-data.sql)