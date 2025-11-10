package com.github.paicoding.forum.service.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.core.util.UrlSlugUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章URL Slug数据迁移服务
 * 为现有文章生成SEO友好的URL标识
 *
 * @author Claude
 * @date 2025-11-10
 */
@Slf4j
@Service
public class ArticleSlugMigrationService {

    @Autowired
    private ArticleDao articleDao;

    /**
     * 为所有文章迁移生成slug
     * 该方法是幂等的,可以重复执行
     *
     * @return 处理的文章数量
     */
    public int migrateAllArticleSlugs() {
        log.info("========================================");
        log.info("开始迁移文章URL slugs...");
        log.info("========================================");

        // 查询所有没有slug或slug为空的文章
        LambdaQueryWrapper<ArticleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(ArticleDO::getUrlSlug)
                .or()
                .eq(ArticleDO::getUrlSlug, "")
        );

        List<ArticleDO> articles = articleDao.list(queryWrapper);

        if (articles.isEmpty()) {
            log.info("没有需要迁移的文章!");
            return 0;
        }

        log.info("发现 {} 篇需要生成slug的文章", articles.size());

        int successCount = 0;
        int failCount = 0;

        for (ArticleDO article : articles) {
            try {
                // 优先使用shortTitle,其次使用title
                String titleForSlug = StringUtils.isNotBlank(article.getShortTitle())
                        ? article.getShortTitle()
                        : article.getTitle();

                if (StringUtils.isBlank(titleForSlug)) {
                    log.warn("文章 ID={} 标题为空,跳过", article.getId());
                    failCount++;
                    continue;
                }

                String slug = UrlSlugUtil.generateSlug(titleForSlug);
                article.setUrlSlug(slug);

                articleDao.updateById(article);
                successCount++;

                if (successCount % 100 == 0) {
                    log.info("进度: 已处理 {}/{} 篇文章", successCount, articles.size());
                }

                // 打印前几篇的示例
                if (successCount <= 5) {
                    log.info("示例 #{}: 标题=\"{}\" -> slug=\"{}\"", successCount, titleForSlug, slug);
                }

            } catch (Exception e) {
                log.error("处理文章 ID={} 时发生错误", article.getId(), e);
                failCount++;
            }
        }

        log.info("========================================");
        log.info("URL slug迁移完成!");
        log.info("总计: {} 篇", articles.size());
        log.info("成功: {} 篇", successCount);
        log.info("失败: {} 篇", failCount);
        log.info("========================================");

        return successCount;
    }

    /**
     * 为指定文章ID重新生成slug
     * 用于修正特定文章的slug
     *
     * @param articleId 文章ID
     * @return 是否成功
     */
    public boolean regenerateSlug(Long articleId) {
        ArticleDO article = articleDao.getById(articleId);
        if (article == null) {
            log.warn("文章 ID={} 不存在", articleId);
            return false;
        }

        String titleForSlug = StringUtils.isNotBlank(article.getShortTitle())
                ? article.getShortTitle()
                : article.getTitle();

        if (StringUtils.isBlank(titleForSlug)) {
            log.warn("文章 ID={} 标题为空", articleId);
            return false;
        }

        String oldSlug = article.getUrlSlug();
        String newSlug = UrlSlugUtil.generateSlug(titleForSlug);

        article.setUrlSlug(newSlug);
        articleDao.updateById(article);

        log.info("文章 ID={} slug更新: \"{}\" -> \"{}\"", articleId, oldSlug, newSlug);
        return true;
    }

    /**
     * 统计需要迁移的文章数量
     *
     * @return 需要迁移的文章数量
     */
    public long countArticlesNeedMigration() {
        LambdaQueryWrapper<ArticleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(ArticleDO::getUrlSlug)
                .or()
                .eq(ArticleDO::getUrlSlug, "")
        );

        return articleDao.count(queryWrapper);
    }
}
