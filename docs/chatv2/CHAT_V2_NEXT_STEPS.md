# Chat V2 - 下一步操作指南

## ✅ 已完成的工作

### 后端实现 (100% 完成)
- ✅ 数据库迁移脚本创建和配置
- ✅ 实体类和 Mapper 创建
- ✅ 配置类和 ChatClient 工厂创建
- ✅ 所有服务层实现（ChatMemoryService, ChatConversationService, ChatMessageService, StreamingChatService）
- ✅ REST Controller 和 VO 类创建
- ✅ **所有编译错误已修复**
- ✅ **Maven 编译成功**
- ✅ **Mapper 扫描配置已添加**

### 前端实现 (100% 完成)
- ✅ Pinia store 创建
- ✅ API 请求方法实现
- ✅ 所有 Vue 组件创建
- ✅ 路由配置更新

## 📋 待完成步骤

### 1. 安装前端依赖

```bash
cd pai-coding-front
npm install marked
npm install @types/marked --save-dev
```

### 2. 配置 API Keys

**选项 A：使用环境变量（推荐）**
```bash
export QWEN_API_KEY="sk-xxxxxxxxxxxxxxxxxx"
export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxxxxxxx"
```

**选项 B：直接修改配置文件**
编辑 `paicoding-web/src/main/resources-env/dev/application-ai.yml`，将 API Key 占位符替换为实际的 Key：

```yaml
chat-v2:
  models:
    - id: qwen-plus
      name: 通义千问-Plus
      provider: Alibaba
      baseUrl: https://dashscope.aliyuncs.com/compatible-mode/v1
      apiKey: sk-your-actual-qwen-api-key-here  # 替换这里
      modelName: qwen-plus
      maxTokens: 4096
      temperature: 0.7
    - id: deepseek-chat
      name: DeepSeek-Chat
      provider: DeepSeek
      baseUrl: https://api.deepseek.com/v1
      apiKey: sk-your-actual-deepseek-api-key-here  # 替换这里
      modelName: deepseek-chat
      maxTokens: 4096
      temperature: 0.7
  default-model: qwen-plus
```

### 3. 启动应用

#### 启动后端
```bash
cd /Users/xuyifei/repos/pai_coding
mvn spring-boot:run
```

或在 IDE 中运行 `QuickForumApplication.java`

#### 启动前端
```bash
cd /Users/xuyifei/repos/pai_coding/pai-coding-front
npm install  # 如果还未安装依赖
npm run dev
```

### 4. 访问和测试

打开浏览器访问：`http://localhost:5173/chat`（或你的前端端口）

#### 测试清单
- [ ] 查看模型列表是否加载成功
- [ ] 选择一个模型并创建新对话
- [ ] 发送消息并查看流式响应
- [ ] 创建多个对话
- [ ] 在不同对话间切换
- [ ] 双击对话标题进行编辑
- [ ] 删除对话
- [ ] 刷新页面后检查对话历史是否保留
- [ ] 测试不同模型的对话隔离

## 🔧 可能遇到的问题

### 问题 1：数据库表不存在
**现象：** 启动时报错 "Table 'xxx.chat_history' doesn't exist"

**解决：**
1. 确保 Liquibase 配置正确
2. 检查 `application.yml` 中的 `spring.liquibase.enabled: true`
3. 手动执行 SQL 脚本：
```bash
mysql -u your_user -p your_database < paicoding-web/src/main/resources/liquibase/data/update_schema_251116_chat_v2.sql
```

### 问题 2：API Key 未配置
**现象：** 调用 LLM 时报 401 或认证错误

**解决：**
1. 检查环境变量是否设置正确：`echo $QWEN_API_KEY`
2. 或者直接在 `application-ai.yml` 中配置 API Key
3. 重启后端应用使配置生效

### 问题 3：前端 marked 依赖缺失
**现象：** 前端控制台报错 "Cannot find module 'marked'"

**解决：**
```bash
cd pai-coding-front
npm install marked
npm install @types/marked --save-dev
```

### 问题 4：CORS 跨域问题
**现象：** 浏览器控制台报 CORS 错误

**解决：**
检查后端的 CORS 配置，确保允许前端域名的跨域请求

### 问题 5：SSE 连接中断
**现象：** 流式响应中途断开

**解决：**
1. 检查网络连接
2. 增加 Nginx/代理服务器的超时时间
3. 检查后端日志查看详细错误信息

## 🎯 快速启动命令

```bash
# 1. 安装前端依赖
cd /Users/xuyifei/repos/pai_coding/pai-coding-front
npm install marked
npm install @types/marked --save-dev

# 2. 设置环境变量（替换为你的实际 API Key）
export QWEN_API_KEY="sk-xxxxxxxxxxxxxxxxxx"
export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxxxxxxx"

# 3. 启动后端（在项目根目录）
cd /Users/xuyifei/repos/pai_coding
mvn spring-boot:run

# 4. 启动前端（新终端窗口）
cd /Users/xuyifei/repos/pai_coding/pai-coding-front
npm run dev
```

## 📝 编译错误修复记录

### 修复 1: ChatMemoryService 缺少方法
**错误：** `不是抽象的, 并且未覆盖 ChatMemory 中的抽象方法 get(java.lang.String)`

**修复：** 添加了 `get(String conversationId)` 方法重载
```java
@Override
public List<Message> get(String conversationId) {
    return get(conversationId, MAX_MESSAGES);
}
```

### 修复 2: StreamingChatService 方法不存在
**错误：** `找不到符号: 方法 getContent()`

**修复：** 将 `getContent()` 改为 `getText()`，并添加了空值检查

### 修复 3: ChatV2RestController ResVo.fail() 方法签名错误
**错误：** `方法 ResVo.<T>fail(String)不适用`

**修复：** 使用正确的 StatusEnum 常量：
- 行 129-130: 使用 `StatusEnum.FORBID_ERROR_MIXED`
- 行 168-169: 使用 `StatusEnum.FORBID_ERROR_MIXED`
- 行 190-191: 使用 `StatusEnum.UNEXPECT_ERROR`

### 修复 4: Mapper 未扫描导致 Bean 创建失败
**错误：** `Parameter 0 of constructor in ChatConversationService required a bean of type 'ChatHistoryMapper' that could not be found`

**修复：** 在 `ServiceAutoConfig.java` 的 `@MapperScan` 注解中添加 chatv2 mapper 包：
```java
@MapperScan(basePackages = {
    // ... 其他包 ...
    "com.github.paicoding.forum.service.chatv2.repository.mapper",
})
```

## 🚀 后续可选扩展

1. **RAG 文档问答**
   - 添加文件上传功能
   - 集成向量数据库
   - 实现文档检索增强生成

2. **对话历史搜索**
   - 利用已创建的 FULLTEXT 索引
   - 实现全文搜索 API

3. **用户配额管理**
   - Token 计数
   - 用户级别限流

4. **多模态支持**
   - 图片理解
   - 语音对话

5. **优化旧代码**
   - 删除 `/chat-old` 相关的旧 WebSocket 代码
   - 清理不再使用的依赖

## 📞 获取帮助

如有问题，请检查：
1. 后端日志：查看控制台输出或日志文件
2. 前端控制台：F12 打开浏览器开发者工具
3. 数据库连接：确保 MySQL 服务正常运行
4. 网络请求：在浏览器 Network 标签查看 API 请求

---

**状态更新时间：** 2025-11-16 23:06
**编译状态：** ✅ BUILD SUCCESS
**下一步：** 安装前端依赖并配置 API Keys
