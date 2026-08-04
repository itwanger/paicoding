# Database Guidelines

> MyBatis-Plus + MySQL 数据访问模式（提取自上游真实代码）。

---

## 继承体系

三层固定结构（以 article 为例）：

1. **实体**：`XxxDO extends BaseDO`，`@Data @EqualsAndHashCode(callSuper = true) @TableName("article")`
   - `BaseDO`（`paicoding-api/.../api/model/entity/BaseDO.java`）：`@TableId(type = IdType.AUTO) Long id` + `Date createTime` + `Date updateTime`
   - 字段用 Java 驼峰，表列为下划线（MyBatis-Plus 默认 map-underscore）；表名小写下划线单数：`article`、`article_detail`、`user_foot`
   - 逻辑删除**不用** MP 的 `@TableLogic`，而是普通 `Integer deleted` 字段 + 查询手动过滤：
     `query.eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())`（`ArticleDao.getByUrlSlug`）
   - 状态类字段用 Integer + 枚举注释引用，如 `ArticleDO.status` 注释 `@see PushStatusEnum`

2. **Mapper**：`ArticleMapper extends BaseMapper<ArticleDO>`，复杂 SQL 走 XML：
   `paicoding-service/src/main/resources/mapper/ArticleMapper.xml`（仅 10 个 XML，多数查询用 wrapper 完成）

3. **Dao**：`@Repository public class ArticleDao extends ServiceImpl<ArticleMapper, ArticleDO>`
   （`service/article/repository/dao/ArticleDao.java`）——上游把「多表/带条件的 DB 操作」都封装在 Dao，Service 层不直接碰 Mapper。
   Dao 内典型写法：
   ```java
   public ArticleDO getByUrlSlug(String urlSlug) {
       return lambdaQuery()
               .eq(ArticleDO::getUrlSlug, urlSlug)
               .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
               .last("limit 1")
               .one();
   }
   ```

## 分页

**不用** MyBatis-Plus 的 `Page`/分页插件。上游自造 `PageParam`（`paicoding-api/.../api/model/vo/PageParam.java`）：

```java
PageParam.newPageInstance(page, size);          // 内部换算 offset/limit
PageParam.getLimitSql(pageParam);               // 生成 "limit x,y" 拼到 wrapper.last()
```

返回用 `PageListVo<T>`（带 hasMore）或 `PageVo<T>`；Controller 侧 `size = Math.min(size, PageParam.DEFAULT_PAGE_SIZE)` 限流（`ArticleRestController.recommend`）。

## 事务

- 注解式：`@Transactional(rollbackFor = Exception.class)` 加在 Service impl 的写方法上（如 `ArticleSettingServiceImpl.deleteArticle:362`、`ArticleWriteServiceImpl`、`CommentWriteServiceImpl`、`RegisterServiceImpl`）。
- 只在写路径用；读路径无事务注解。
- 无编程式事务惯例。

## 迁移（Liquibase）

- 入口：`spring.liquibase.change-log: classpath:liquibase/master.xml`（`paicoding-web/src/main/resources/application.yml`），启动时自动执行。
- `master.xml` include `liquibase/changelog/000_initial_schema.xml`；初始数据在 `liquibase/data/`。
- 新表/新列 = 新增 changelog 文件 + master.xml include（上游目前只有一个 000 初始文件，尚无增量迁移先例——新增迁移时按 `NNN_描述.xml` 顺延即可，**这是建议，非上游现状**）。

## 数据源配置

`resources-env/<env>/application-dal.yml`：支持单数据源与 dynamic 多数据源（master/slave）切换，Druid 连接池；数据库名取自 `${database.name}` 配置，密码走 `${PAICODING_DB_PASSWORD:}` 环境变量。
