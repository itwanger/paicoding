# Admin URL Slug 后端接口更新说明

## 📋 更新概述

为支持管理员界面的URL slug编辑和重复性验证功能,对后端接口进行了必要的修改。

## ✅ 已修改内容

### 1. 请求参数类 - `ArticlePostReq.java`

**文件**: `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/article/ArticlePostReq.java`

**新增字段**:
```java
/**
 * URL slug,用于SEO友好URL
 */
private String urlSlug;
```

**作用**: 允许管理员在创建或更新文章时指定自定义的URL slug

---

### 2. 文章查询请求类 - `SearchArticleReq.java`

**文件**: `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/article/SearchArticleReq.java`

**新增字段**:
```java
@ApiModelProperty("URL slug,用于SEO友好URL")
private String urlSlug;
```

**作用**: 支持通过URL slug进行文章查询,用于验证slug是否重复

---

### 3. 文章查询参数类 - `SearchArticleParams.java`

**文件**: `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/repository/params/SearchArticleParams.java`

**新增字段**:
```java
/**
 * URL slug,用于SEO友好URL
 */
private String urlSlug;
```

**作用**: 数据库查询参数,支持按slug查询

---

### 4. 管理端DTO - `ArticleAdminDTO.java`

**文件**: `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/article/dto/ArticleAdminDTO.java`

**新增字段**:
```java
/**
 * URL slug,用于SEO友好URL
 */
private String urlSlug;
```

**作用**: 在管理端文章列表中返回URL slug信息

---

### 5. MyBatis Mapper XML - `ArticleMapper.xml`

**文件**: `paicoding-service/src/main/resources/mapper/ArticleMapper.xml`

#### 修改1: 查询条件支持slug过滤

**位置**: `<sql id="articlesByParams">`

**新增内容**:
```xml
<if test="searchParams.urlSlug != null and searchParams.urlSlug != ''">
    and a.url_slug = #{searchParams.urlSlug}
</if>
```

**作用**: 允许按URL slug精确查询文章

#### 修改2: 查询结果包含url_slug

**位置**: `<select id="listArticlesByParams">`

**修改前**:
```xml
select a.id as article_id, a.title, a.short_title, a.offical_stat,
```

**修改后**:
```xml
select a.id as article_id, a.title, a.short_title, a.url_slug, a.offical_stat,
```

**作用**: 在管理端文章列表查询时返回URL slug字段

---

### 6. 文章设置服务 - `ArticleSettingServiceImpl.java`

**文件**: `paicoding-service/src/main/java/com/github/paicoding/forum/service/article/service/impl/ArticleSettingServiceImpl.java`

**修改位置**: `updateArticle()` 方法

**新增代码**:
```java
if (req.getUrlSlug() != null) {
    article.setUrlSlug(req.getUrlSlug());
}
```

**作用**: 支持管理员更新文章时修改URL slug

---

## 🎯 前端如何使用这些接口

### 1. 验证URL Slug是否重复

前端调用现有的 `getArticleListApi` 接口,传入 `urlSlug` 参数:

```javascript
// 前端验证逻辑(你已经实现)
const handleUrlSlugCheck = async () => {
  if (!editForm.urlSlug || editForm.urlSlug.trim() === '') {
    setUrlSlugError('');
    return;
  }

  setUrlSlugValidating(true);
  try {
    const response = await getArticleListApi({
      urlSlug: editForm.urlSlug.trim(),
      pageNumber: 1,
      pageSize: 10
    });

    // 排除当前文章ID
    const existingArticles = response.result?.list?.filter(
      (article: any) => article.articleId !== editForm.articleId
    );

    if (existingArticles && existingArticles.length > 0) {
      setUrlSlugError('该 URL slug 已被使用,请使用不同的值');
    } else {
      setUrlSlugError('');
    }
  } catch (error) {
    console.error('验证 URL slug 失败:', error);
  } finally {
    setUrlSlugValidating(false);
  }
};
```

### 2. 创建/更新文章时提交slug

调用 `/admin/article/save` 或 `/admin/article/update` 接口时,在请求body中包含 `urlSlug`:

```javascript
const handleSubmit = async () => {
  // 检查是否有重复错误
  if (urlSlugError) {
    message.error('请修正 URL slug 错误后再提交');
    return;
  }

  const requestBody = {
    articleId: editForm.articleId,
    title: editForm.title,
    shortTitle: editForm.shortTitle,
    urlSlug: editForm.urlSlug, // 新增字段
    // ... 其他字段
  };

  // 调用保存或更新接口
  await saveArticleApi(requestBody);
};
```

### 3. 文章列表会自动返回urlSlug

调用 `/admin/article/list` 接口时,返回的每个文章对象会自动包含 `urlSlug` 字段:

```javascript
// 响应示例
{
  "result": {
    "list": [
      {
        "articleId": 123,
        "title": "Spring Boot 教程",
        "shortTitle": "Spring Boot",
        "urlSlug": "spring-boot-tutorial", // 自动返回
        // ... 其他字段
      }
    ]
  }
}
```

---

## 🧪 测试验证

### 1. 测试slug重复验证

```bash
# 1. 创建一篇文章,设置slug为 "test-article"
curl -X POST http://localhost:8080/admin/article/save \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试文章1",
    "urlSlug": "test-article",
    "content": "测试内容",
    "status": 1
  }'

# 2. 查询该slug是否存在
curl -X POST http://localhost:8080/admin/article/list \
  -H "Content-Type: application/json" \
  -d '{
    "urlSlug": "test-article",
    "pageNumber": 1,
    "pageSize": 10
  }'

# 预期结果: 返回包含该文章的列表
```

### 2. 测试更新slug

```bash
# 更新文章的slug
curl -X POST http://localhost:8080/admin/article/update \
  -H "Content-Type: application/json" \
  -d '{
    "articleId": 123,
    "urlSlug": "new-slug-name"
  }'

# 验证更新成功
curl -X GET "http://localhost:8080/admin/article/detail?articleId=123"
```

### 3. 测试文章列表返回slug

```bash
# 查询文章列表
curl -X POST http://localhost:8080/admin/article/list \
  -H "Content-Type: application/json" \
  -d '{
    "pageNumber": 1,
    "pageSize": 10
  }'

# 检查返回的每篇文章是否包含urlSlug字段
```

---

## 📊 API接口说明

### 现有接口支持情况

| 接口路径 | HTTP方法 | urlSlug支持 | 说明 |
|---------|---------|------------|------|
| `/admin/article/list` | POST | ✅ 查询条件 + 返回字段 | 可按slug查询,返回结果包含slug |
| `/admin/article/save` | POST | ✅ 请求参数 | 创建文章时可指定slug |
| `/admin/article/update` | POST | ✅ 请求参数 | 更新文章时可修改slug |
| `/admin/article/detail` | GET | ✅ 返回字段 | 文章详情包含slug |

---

## ⚠️ 重要提醒

### 1. Slug唯一性

**目前的实现方式**:
- 数据库层面: `url_slug` 字段有索引,但不是UNIQUE索引
- 应用层面: 前端通过查询验证唯一性

**建议**:
- 如果希望强制唯一性,可以考虑在数据库添加UNIQUE索引
- 但考虑到可能存在空slug的旧文章,当前方案更灵活

### 2. 空slug处理

- 如果管理员不填写slug,系统会自动生成(在 `ArticleConverter.toArticleDo()` 中)
- 管理员可以填写空字符串来清除slug(但不推荐)

### 3. Slug格式验证

**前端应该做的验证**:
```javascript
const validateSlug = (slug) => {
  // 1. 只允许小写字母、数字、连字符
  const pattern = /^[a-z0-9-]+$/;
  if (!pattern.test(slug)) {
    return '只能包含小写字母、数字和连字符';
  }

  // 2. 不能以连字符开头或结尾
  if (slug.startsWith('-') || slug.endsWith('-')) {
    return '不能以连字符开头或结尾';
  }

  // 3. 长度限制
  if (slug.length > 100) {
    return '长度不能超过100字符';
  }

  return '';
};
```

### 4. MapStruct自动映射

由于使用了MapStruct,`SearchArticleReq` 到 `SearchArticleParams` 的映射是自动的,无需手动添加映射代码。

---

## 🚀 部署步骤

### 1. 编译项目

```bash
cd /Users/itwanger/Documents/GitHub/paicoding
mvn clean install -DskipTests=true
```

### 2. 重启应用

```bash
cd paicoding-web
mvn spring-boot:run
```

### 3. 验证接口

使用上面的测试命令验证接口是否正常工作。

---

## 🔧 故障排查

### 问题1: 查询不到slug字段

**检查**:
- 确认 `ArticleMapper.xml` 的select语句包含 `a.url_slug`
- 确认 `ArticleAdminDTO` 有 `urlSlug` 属性

### 问题2: 按slug查询无效

**检查**:
- 确认 `ArticleMapper.xml` 的 `articlesByParams` 包含slug条件
- 确认 `SearchArticleParams` 有 `urlSlug` 属性
- 查看SQL日志确认条件是否生效

### 问题3: 更新slug不生效

**检查**:
- 确认 `ArticleSettingServiceImpl.updateArticle()` 包含设置slug的代码
- 确认 `ArticlePostReq` 有 `urlSlug` 属性

---

## 📝 后续优化建议

### 1. 添加专用的slug验证接口

虽然当前通过 `getArticleList` 可以验证,但创建专用接口更清晰:

```java
@GetMapping("/admin/article/slug/check")
public ResVo<Boolean> checkSlugAvailability(
    @RequestParam String slug,
    @RequestParam(required = false) Long excludeArticleId
) {
    // 检查slug是否可用
    boolean available = articleService.isSlugAvailable(slug, excludeArticleId);
    return ResVo.ok(available);
}
```

### 2. 添加Slug历史记录

考虑保存slug的修改历史,用于SEO分析和问题排查。

### 3. Slug自动建议

当标题修改时,自动建议新的slug(但不强制更新):

```javascript
const handleTitleChange = (newTitle) => {
  setTitle(newTitle);
  // 只在创建新文章时自动建议
  if (!articleId) {
    setSuggestedSlug(generateSlugFromTitle(newTitle));
  }
};
```

---

## 🎉 总结

本次后端更新完整支持了管理员界面的URL slug功能:

✅ **查询支持**: 可按slug查询文章,用于验证重复性
✅ **创建支持**: 创建文章时可指定自定义slug
✅ **更新支持**: 更新文章时可修改slug
✅ **列表返回**: 文章列表自动包含slug字段
✅ **向后兼容**: 不影响现有功能,旧数据依然可用

**前端可以直接使用现有的API接口,无需等待新接口开发!**

---

**更新时间**: 2025-11-10
**更新内容**: 完整支持管理员URL slug编辑和验证功能
**影响范围**: Admin后台管理接口
