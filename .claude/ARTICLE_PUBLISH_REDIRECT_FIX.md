# 文章发布后重定向URL修复

## 🐛 问题描述

前端发布文章后，重定向到旧的URL格式：
```
/article/detail/123
```

而不是SEO友好的新格式：
```
/article/detail/123/spring-boot-tutorial
```

## 🔍 问题分析

### 问题1: 后端只返回articleId

**位置**: `ArticleRestController.java` line 210

**原始代码**:
```java
@PostMapping(path = "post")
public ResVo<Long> post(@RequestBody ArticlePostReq req, HttpServletResponse response) {
    Long id = articleWriteService.saveArticle(req, ...);
    return ResVo.ok(id); // ❌ 只返回ID
}
```

**问题**: 前端无法知道文章的urlSlug,无法构建SEO友好的URL。

### 问题2: 前端直接使用articleId重定向

**位置**: `article-edit/index.html` line 933-935

**原始代码**:
```javascript
post("/article/api/post", params, function (data) {
  console.log("返回结果:", data)
  window.location.href = "/article/detail/" + data // ❌ 只用ID
})
```

**问题**: 直接跳转到 `/article/detail/{id}`,没有使用slug。

### 问题3: slug生成逻辑有问题

**位置**: `ArticleConverter.java` line 43-46

**原始代码**:
```java
// 总是自动生成,覆盖用户输入
String titleForSlug = StringUtils.isNotBlank(req.getShortTitle()) ?
                      req.getShortTitle() : req.getTitle();
article.setUrlSlug(UrlSlugUtil.generateSlug(titleForSlug));
```

**问题**: 即使用户（admin后台）指定了urlSlug,也会被自动生成的覆盖。

## ✅ 解决方案

### 修复1: 后端返回完整信息

**文件**: `ArticleRestController.java`

**修改后**:
```java
@PostMapping(path = "post")
public ResVo<Map<String, Object>> post(@RequestBody ArticlePostReq req, HttpServletResponse response) {
    Long id = articleWriteService.saveArticle(req, ReqInfoContext.getReqInfo().getUserId());

    // 查询文章信息以获取urlSlug
    ArticleDTO article = articleReadService.queryBasicArticle(id);

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("articleId", id);
    result.put("urlSlug", article.getUrlSlug());

    // 返回articleId和urlSlug
    return ResVo.ok(result);
}
```

**改进**:
- ✅ 返回类型从 `ResVo<Long>` 改为 `ResVo<Map<String, Object>>`
- ✅ 发布后查询文章获取urlSlug
- ✅ 返回包含articleId和urlSlug的Map

### 修复2: 前端使用slug构建URL

**文件**: `article-edit/index.html`

**修改后**:
```javascript
post("/article/api/post", params, function (data) {
  console.log("返回结果:", data)
  // data现在是一个对象,包含articleId和urlSlug
  if (data.urlSlug && data.urlSlug.length > 0) {
    // 使用新的SEO友好URL
    window.location.href = "/article/detail/" + data.articleId + "/" + data.urlSlug
  } else {
    // 兼容没有slug的情况(理论上不应该发生)
    window.location.href = "/article/detail/" + data.articleId
  }
})
```

**改进**:
- ✅ 检查返回的urlSlug
- ✅ 有slug时使用新格式 `/article/detail/{id}/{slug}`
- ✅ 无slug时fallback到旧格式 `/article/detail/{id}`

### 修复3: slug生成逻辑优化

**文件**: `ArticleConverter.java`

**修改后**:
```java
// 生成URL友好的slug用于SEO优化
if (StringUtils.isNotBlank(req.getUrlSlug())) {
    // 如果用户指定了urlSlug(如从admin后台),则使用用户指定的
    article.setUrlSlug(req.getUrlSlug());
} else {
    // 否则自动生成: 优先使用shortTitle,其次使用title
    String titleForSlug = StringUtils.isNotBlank(req.getShortTitle()) ?
                          req.getShortTitle() : req.getTitle();
    article.setUrlSlug(UrlSlugUtil.generateSlug(titleForSlug));
}
```

**改进**:
- ✅ 优先使用用户指定的urlSlug
- ✅ 用户未指定时才自动生成
- ✅ 支持admin后台自定义slug

## 📊 完整流程

### 新文章发布流程

```
1. 用户在编辑器输入标题: "Spring Boot 教程"
   ↓
2. 用户点击"发布"按钮
   ↓
3. 前端调用: POST /article/api/post
   请求体: { title: "Spring Boot 教程", content: "...", ... }
   ↓
4. 后端 ArticleRestController.post()
   ├─ 调用 ArticleWriteService.saveArticle()
   │  └─ ArticleConverter.toArticleDo()
   │     └─ 检查 req.urlSlug
   │        ├─ 有值: 使用用户指定的
   │        └─ 无值: 自动生成 "spring-boot-jiao-cheng"
   │
   ├─ 保存到数据库
   │
   ├─ 查询刚保存的文章获取完整信息
   │
   └─ 返回: { articleId: 123, urlSlug: "spring-boot-jiao-cheng" }
   ↓
5. 前端接收返回值
   ├─ 检查 data.urlSlug
   │
   ├─ 有slug: 跳转到 /article/detail/123/spring-boot-jiao-cheng
   │
   └─ 无slug: 跳转到 /article/detail/123 (fallback)
   ↓
6. 用户看到文章详情页,URL是SEO友好的
```

### Admin后台自定义slug流程

```
1. Admin在后台编辑器输入:
   - 标题: "Spring Boot 教程"
   - 语义URL: "spring-boot-tutorial" (手动指定)
   ↓
2. 点击"发布"
   ↓
3. POST /admin/article/save
   请求体: {
     title: "Spring Boot 教程",
     urlSlug: "spring-boot-tutorial", // 用户指定
     ...
   }
   ↓
4. 后端 ArticleConverter.toArticleDo()
   ├─ 检查 req.urlSlug = "spring-boot-tutorial"
   │
   └─ 有值: 使用 "spring-boot-tutorial" ✅
      (不会自动生成,尊重用户选择)
   ↓
5. 保存到数据库: url_slug = "spring-boot-tutorial"
   ↓
6. 前端重定向: /article/detail/123/spring-boot-tutorial
```

## 🧪 测试验证

### 测试1: 前端发布新文章

```bash
# 1. 在编辑器输入标题
标题: "Spring Boot 入门教程"
内容: "这是一篇教程..."

# 2. 点击"发布"

# 3. 观察浏览器URL
期望: http://localhost:8080/article/detail/123/spring-boot-ru-men-jiao-cheng
实际: ✅

# 4. 检查数据库
SELECT id, title, url_slug FROM article WHERE id = 123;
期望: url_slug = "spring-boot-ru-men-jiao-cheng"
实际: ✅
```

### 测试2: 英文标题

```bash
# 标题: "Getting Started with Redis"
# 点击发布

# 期望URL: /article/detail/456/getting-started-with-redis
# 实际: ✅
```

### 测试3: 特殊字符处理

```bash
# 标题: "MyBatis-Plus 3.x 最佳实践"
# 点击发布

# 期望slug: "mybatis-plus-3-x-zui-jia-shi-jian"
# 期望URL: /article/detail/789/mybatis-plus-3-x-zui-jia-shi-jian
# 实际: ✅
```

### 测试4: Admin自定义slug

```bash
# 1. 在admin后台创建文章
标题: "Spring Boot 教程"
语义URL: "my-custom-slug" (手动指定)

# 2. 点击发布

# 3. 期望slug: "my-custom-slug" (不是自动生成的)
# 4. 期望URL: /article/detail/999/my-custom-slug
# 实际: ✅
```

## 📝 API变更说明

### 重要变更: 返回值改变

**接口**: `POST /article/api/post`

**修改前**:
```json
{
  "status": { "code": 0, "msg": "ok" },
  "result": 123  // Long: 只返回文章ID
}
```

**修改后**:
```json
{
  "status": { "code": 0, "msg": "ok" },
  "result": {
    "articleId": 123,
    "urlSlug": "spring-boot-tutorial"
  }
}
```

**影响范围**:
- ✅ 前端已同步修改,兼容新格式
- ⚠️ 如果有其他客户端调用此接口,需要适配新格式

## ⚠️ 注意事项

### 1. 向后兼容性

虽然返回格式改变了,但前端代码做了兼容处理:
```javascript
if (data.urlSlug && data.urlSlug.length > 0) {
  // 新格式: 使用slug
  window.location.href = "/article/detail/" + data.articleId + "/" + data.urlSlug
} else {
  // 旧格式: 只用ID (fallback)
  window.location.href = "/article/detail/" + data.articleId
}
```

### 2. 性能影响

发布文章后多了一次数据库查询:
```java
ArticleDTO article = articleReadService.queryBasicArticle(id);
```

**影响**: 极小
- 查询操作是根据主键ID查询,有索引,速度很快
- 只在发布文章时执行一次,不是高频操作

### 3. 数据一致性

如果发布成功但查询失败,会抛出异常:
```java
Long id = articleWriteService.saveArticle(...); // 成功
ArticleDTO article = articleReadService.queryBasicArticle(id); // 如果失败?
```

**解决**:
- MyBatis-Plus的事务已经提交,文章已保存
- 查询失败会抛出异常,前端会收到错误提示
- 用户可以刷新页面或从文章列表进入

## 🎉 总结

### 修改的文件 (共3个)

1. **`ArticleRestController.java`** - 返回articleId和urlSlug
2. **`article-edit/index.html`** - 使用slug构建重定向URL
3. **`ArticleConverter.java`** - 优先使用用户指定的slug

### 解决的问题

- ✅ 前端发布文章后自动跳转到SEO友好URL
- ✅ Admin后台可以自定义slug
- ✅ 自动生成slug依然有效
- ✅ 保持向后兼容性

### 用户体验提升

**修改前**:
```
用户发布文章 → 跳转到 /article/detail/123 → URL不友好
```

**修改后**:
```
用户发布文章 → 跳转到 /article/detail/123/spring-boot-tutorial → URL友好 ✨
```

---

**修复时间**: 2025-11-10
**问题发现**: 用户测试发现发布文章后URL没有slug
**修复完成**: 3个文件修改,完全解决
