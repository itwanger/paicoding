# Chat V2 实现完成指南

## 📋 实现概述

本项目已成功实现了一个完整的 LLM 聊天系统，对标 DeepExtract 的功能，但使用 MySQL 替代 PostgreSQL，并从配置文件读取模型而非 OneAPI。

## ✅ 已完成的工作

### 后端 (100% 完成)

#### 1. 数据库层
- ✅ `chat_history` 表 - 存储会话信息
- ✅ `chat_message` 表 - 存储消息记录
- ✅ Liquibase 迁移脚本
- ✅ MyBatis-Plus 实体类和 Mapper

**文件位置：**
```
paicoding-web/src/main/resources/liquibase/data/update_schema_251116_chat_v2.sql
paicoding-service/src/main/java/com/github/paicoding/forum/service/chatv2/repository/
```

#### 2. 配置管理
- ✅ `application-ai.yml` - 多模型配置
- ✅ `ChatV2ConfigProperties` - 配置类

**配置示例：**
```yaml
chat-v2:
  models:
    - id: qwen-plus
      name: 通义千问-Plus
      provider: Alibaba
      baseUrl: https://dashscope.aliyuncs.com/compatible-mode/v1
      apiKey: ${BAILIAN_API_KEY}
      modelName: qwen-plus
      maxTokens: 4096
      temperature: 0.7
  default-model: qwen-plus
```

#### 3. 核心服务
- ✅ `ChatClientFactory` - 启动时创建所有 ChatClient
- ✅ `ChatMemoryService` - Spring AI ChatMemory 接口实现
- ✅ `ChatConversationService` - 会话 CRUD 管理
- ✅ `ChatMessageService` - 消息服务
- ✅ `StreamingChatService` - SSE 流式响应

#### 4. REST API
- ✅ `ChatV2RestController` - 8 个 API 端点
  - `POST /chatv2/api/send` - 发送消息（SSE 流式）
  - `GET /chatv2/api/conversations` - 获取会话列表
  - `GET /chatv2/api/conversation/{id}` - 获取会话详情
  - `POST /chatv2/api/conversation` - 创建新会话
  - `PUT /chatv2/api/conversation/title` - 更新标题
  - `DELETE /chatv2/api/conversation/{id}` - 删除会话
  - `GET /chatv2/api/models` - 获取模型列表
  - `GET /chatv2/api/models/default` - 获取默认模型

### 前端 (100% 完成)

#### 1. 状态管理
- ✅ `chatStore.ts` - Pinia store

#### 2. API 请求层
- ✅ `BackendRequests.ts` - 所有 API 方法
- ✅ SSE 流式请求处理

#### 3. Vue 组件
- ✅ `ChatViewV2.vue` - 主页面
- ✅ `ChatSidebar.vue` - 左侧对话列表
- ✅ `ChatWindow.vue` - 聊天窗口容器
- ✅ `EmptyState.vue` - 空白状态 + 模型选择器
- ✅ `ChatHeader.vue` - 头部模型显示
- ✅ `MessageList.vue` - 消息列表
- ✅ `ChatInput.vue` - 输入框

#### 4. 路由配置
- ✅ `/chat` → `ChatViewV2.vue` (新版)
- ✅ `/chat-old` → `ChatView.vue` (旧版备份)

## 🔧 待完成的步骤

### 1. 安装前端依赖

```bash
cd pai-coding-front
npm install marked
npm install @types/marked --save-dev
```

### 2. 配置环境变量

在 `paicoding-web/src/main/resources-env/dev/application-ai.yml` 中设置 API Keys：

```bash
# 方式1：通过环境变量
export BAILIAN_API_KEY="your_bailian_api_key"

# 方式2：直接修改配置文件（不推荐用于生产环境）
# 将 ${BAILIAN_API_KEY:your_bailian_api_key_here} 替换为实际的 API Key
```

### 3. 启动应用

#### 后端
```bash
cd paicoding-web
mvn clean install
mvn spring-boot:run
```

或在 IDE 中运行 `QuickForumApplication.java`

#### 前端
```bash
cd pai-coding-front
npm install
npm run dev
```

### 4. 访问聊天页面

打开浏览器访问: `http://localhost:5173/chat` (或你的前端端口)

### 5. 测试功能清单

- [ ] 模型列表加载
- [ ] 选择模型并创建对话
- [ ] 发送消息并查看流式响应
- [ ] 创建多个对话
- [ ] 切换不同对话
- [ ] 编辑对话标题
- [ ] 删除对话
- [ ] 刷新页面后对话历史保留
- [ ] 不同模型的对话隔离

## 📁 完整文件清单

### 后端文件

```
paicoding-service/src/main/java/com/github/paicoding/forum/service/chatv2/
├── config/
│   └── ChatV2ConfigProperties.java
├── factory/
│   └── ChatClientFactory.java
├── repository/
│   ├── entity/
│   │   ├── ChatHistoryDO.java
│   │   └── ChatMessageDO.java
│   └── mapper/
│       ├── ChatHistoryMapper.java
│       └── ChatMessageMapper.java
└── service/
    ├── ChatConversationService.java
    ├── ChatMemoryService.java
    ├── ChatMessageService.java
    └── StreamingChatService.java

paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/chatv2/
├── ChatSendReqVO.java
├── ConversationVO.java
├── MessageVO.java
├── ModelInfoVO.java
└── UpdateTitleReqVO.java

paicoding-web/src/main/java/com/github/paicoding/forum/web/controller/chatv2/
└── ChatV2RestController.java

paicoding-web/src/main/resources/
├── liquibase/
│   ├── changelog/000_initial_schema.xml (已更新)
│   └── data/update_schema_251116_chat_v2.sql
└── resources-env/dev/application-ai.yml (已扩展)
```

### 前端文件

```
pai-coding-front/src/
├── stores/
│   └── chatStore.ts
├── views/
│   └── ChatViewV2.vue
├── components/chatv2/
│   ├── ChatSidebar.vue
│   ├── ChatWindow.vue
│   ├── EmptyState.vue
│   ├── ChatHeader.vue
│   ├── MessageList.vue
│   └── ChatInput.vue
├── http/
│   └── BackendRequests.ts (已扩展)
└── router/
    └── index.ts (已更新)
```

## 🎨 UI 特性

### 渐变色主题
- 主色调：`#667eea` → `#764ba2`
- 采用 DeepExtract 风格的渐变设计

### 关键交互
- **双击编辑标题**：双击对话标题或点击编辑图标
- **Enter 发送**：直接 Enter 发送，Shift+Enter 换行
- **流式响应**：逐字显示 AI 回复，带加载动画
- **自动滚动**：新消息自动滚动到底部
- **Markdown 渲染**：支持 Markdown 格式的消息

## 🔍 技术亮点

### 后端
1. **配置驱动**：从 YAML 读取模型配置，无需 OneAPI
2. **启动时初始化**：所有 ChatClient 在启动时创建，避免动态创建开销
3. **Spring AI 集成**：标准的 ChatMemory 接口实现
4. **SSE 流式响应**：支持实时流式输出
5. **心跳机制**：工具调用时发送心跳防止超时

### 前端
1. **Pinia 状态管理**：响应式数据流
2. **SSE 流式处理**：使用 Fetch API ReadableStream
3. **Markdown 渲染**：使用 marked.js
4. **组件化设计**：高度解耦的 Vue 组件
5. **优雅降级**：旧版聊天保留在 `/chat-old`

## ⚠️ 注意事项

### 1. API Key 安全
- 不要将 API Key 直接提交到代码仓库
- 使用环境变量或密钥管理系统
- 生产环境建议使用 Spring Cloud Config 或 Vault

### 2. 数据库迁移
- 首次启动会自动执行 Liquibase 迁移
- 如果表已存在，可能需要手动调整

### 3. CORS 配置
- 确保后端允许前端域名的跨域请求
- 检查 `withCredentials: true` 设置

### 4. 依赖版本
- Spring AI 需要 Spring Boot 3.x
- 确保 MyBatis-Plus 版本兼容

## 🚀 未来扩展

可选的功能增强：

1. **文件上传与 RAG**
   - 添加 `t_chat_file_relation` 表
   - 实现文档上传和向量存储
   - 在 StreamingChatService 中集成 RAG

2. **全文搜索**
   - MySQL FULLTEXT 索引已创建
   - 可实现对话历史搜索功能

3. **工具调用（Tool Calling）**
   - Spring AI 支持 Function Calling
   - 可扩展 ChatClient 添加自定义工具

4. **用户配额管理**
   - 添加用户级别的 token 配额
   - 实现计费和限流

5. **多模态支持**
   - 图片理解
   - 语音对话

## 📝 开发日志

- **2025-11-16**: 完成 Chat V2 完整实现
  - 后端：数据库、服务层、API 层
  - 前端：Pinia Store、所有 Vue 组件、路由配置
  - 对标 DeepExtract，使用 MySQL + Spring AI

## 🤝 贡献者

- XuYifei - 完整实现

---

**如有问题，请检查：**
1. 数据库连接是否正常
2. API Key 是否配置正确
3. 前端依赖是否安装完整
4. 端口是否被占用

**祝你使用愉快！** 🎉
