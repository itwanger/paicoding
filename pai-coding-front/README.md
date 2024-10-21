## 写在前面

本项目是技术派项目的一个重构版本，搭配使用Spring Boot3重构的后端食用

## 配套服务

1. **前后端分离网址**：[https://www.xuyifei.site](http://www.xuyifei.site)
2. **技术派管理端源码**：[paicoding-admin](https://github.com/itwanger/paicoding-admin)

## 项目介绍

### 项目演示

### 前台用Vue3重构后的效果

可以直接浏览本人的[上线网站](https://www.xuyifei.site)（求各位不要进行攻击，友好交流，本人还只是个学森）

部分效果如下：

- 首页
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-1.png)
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-2.png)
- 教程（专栏）页
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-3.png)
- 大模型对话
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-4.png)
- 更新计划
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-5.png)

### 代码展示

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-front-code-1.png)
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/commons/tech-pai-front-code-2.png)


## 项目结构

### 组织结构
```
src
├── assets -- 主要是一些css
├── components -- 项目中拆分出来的组件，包括头部、底部、侧边栏等；以及不同页面下为了代码实现整洁而抽象出的模块
├── constants -- 一些常量的定义，便于在组件中实现类似的逻辑时可以直接引用常量
├── http -- 包含两部分，一部分是用于网络通信时，对后端响应做的类型定义；另一部分是用于发起请求的axios实例和函数
├── plugins -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等
├── router -- vue router的配置
├── stores -- 目前只是用于存储后端发挥的global信息，详见技术派的后端实现中的GlobalInfoVo
├── util -- 封装了一些工具函数
├── view -- 具体的不同vue页面

```

### 环境配置说明

- 本项目使用的是Vue3，所以需要安装Vue3的环境
- 后端的地址目前配置相对捡漏，需要在`src/http/URL.ts`中配置`BASE_URL`和`WS_URL`，分别对应后端的HTTP和WebSocket地址
- 其余配置只需要运行 
    ``` bash
    npm install
    ``` 
    即可

## 技术选型
### 前端主要使用的库如下

|         技术          | 说明                                                                             | 官网                                                                                                |
|:-------------------:|--------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
|        Vue3         | 强大的前端开发框架                                                                      | [https://cn.vuejs.org/](https://cn.vuejs.org/)                                                          |
|        Axios        | 一个基于 promise 的网络请求库，可以用于浏览器和 node.js                                           | [https://www.axios-http.cn/](https://www.axios-http.cn/)                  |
|    Element-Plus     | 基于 Vue 3，面向设计师和开发者的组件库                                                         | [https://element-plus.org/zh-CN/](https://element-plus.org/zh-CN/)                                                      |
|      data-fns       | 现代的JavaScript日期工具库                                                             | [https://date-fns.org/](https://date-fns.org/)                                                    |
|    md-editor-v3     | Markdown编辑器Vue3版本，使用jsx和typescript语法开发，支持切换主题、prettier美化文本等                    | [https://imzbf.github.io/md-editor-v3/en-US/index](https://imzbf.github.io/md-editor-v3/en-US/index) |
|    elasticsearch    | 近实时文本搜索                                                                        | [https://www.elastic.co/cn/elasticsearch/service](https://www.elastic.co/cn/elasticsearch/service) |
|       stompjs       | 为浏览器提供STOPM客户端的库                                                               | [https://www.npmjs.com/package/stompjs](https://www.npmjs.com/package/stompjs)                                                              |
|     tailwindcss     | 一个实用优先的 CSS 框架，包含了诸如 flex、pt-4、text-center 和 rotate-90 等类，可以直接在你的标记中组合来构建任何设计。 | [https://tailwindcss.com/](https://tailwindcss.com/)                                              |
|    node-vibrant     | 一个基于Node.js 的库，它利用智能算法从图像中抽取主要和次要色调，生成色彩丰富的色板                                  | [https://github.com/Vibrant-Colors/node-vibrant](https://github.com/Vibrant-Colors/node-vibrant)                                              |
|     vue-router      | Vue.js的官方路由                                                                    | [https://router.vuejs.org/zh/](https://router.vuejs.org/zh/)                                                            |
