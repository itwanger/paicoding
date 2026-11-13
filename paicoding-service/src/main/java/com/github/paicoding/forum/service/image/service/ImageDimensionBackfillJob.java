package com.github.paicoding.forum.service.image.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.core.util.MarkdownConverter;
import com.github.paicoding.forum.core.util.StrUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 存量文章图片尺寸回填：启动后在后台低速遍历已发布文章，把正文图片的宽高探测出来落库。
 * 已收录的图片直接跳过，因此除首次外的启动几乎无额外开销。
 *
 * @author YiHui
 * @date 2026/7/9
 */
@Slf4j
@Component
public class ImageDimensionBackfillJob {
    private static final long STARTUP_DELAY_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long PROBE_INTERVAL_MS = 200;
    private static final int ARTICLE_BATCH_SIZE = 100;

    @Autowired
    private ArticleDao articleDao;

    /**
     * 回填开关，默认开启；已收录图片会跳过，重复启动开销很小
     */
    @Value("${image.dimension-backfill.enabled:true}")
    private Boolean backfillEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleBackfill() {
        if (!Boolean.TRUE.equals(backfillEnabled)) {
            return;
        }

        Thread worker = new Thread(this::backfillAllArticles, "paicoding-image-dimension-backfill");
        worker.setDaemon(true);
        worker.start();
    }

    private void backfillAllArticles() {
        try {
            Thread.sleep(STARTUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        long lastId = 0L;
        int probed = 0;
        int failed = 0;
        Set<String> visitedSrc = new HashSet<>();
        log.info("图片尺寸回填任务开始");
        while (true) {
            List<ArticleDO> articles = listOnlineArticlesAfter(lastId);
            if (articles.isEmpty()) {
                break;
            }

            for (ArticleDO article : articles) {
                lastId = Math.max(lastId, article.getId());
                for (String src : extractArticleImages(article.getId())) {
                    if (!visitedSrc.add(src)) {
                        continue;
                    }

                    try {
                        if (StrUtil.ensureImageDimension(src)) {
                            probed++;
                        } else {
                            failed++;
                        }
                        Thread.sleep(PROBE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (Exception e) {
                        failed++;
                    }
                }
            }
        }
        log.info("图片尺寸回填任务结束: 收录 {} 张, 失败 {} 张", probed, failed);
    }

    private List<ArticleDO> listOnlineArticlesAfter(long lastId) {
        try {
            return articleDao.list(Wrappers.<ArticleDO>lambdaQuery()
                    .select(ArticleDO::getId)
                    .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode())
                    .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                    .gt(ArticleDO::getId, lastId)
                    .orderByAsc(ArticleDO::getId)
                    .last("limit " + ARTICLE_BATCH_SIZE));
        } catch (Exception e) {
            log.warn("图片尺寸回填查询文章失败", e);
            return java.util.Collections.emptyList();
        }
    }

    private List<String> extractArticleImages(long articleId) {
        try {
            com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO detail = articleDao.queryArticleDetail(articleId);
            if (detail == null || StringUtils.isBlank(detail.getContent())) {
                return java.util.Collections.emptyList();
            }

            String html = MarkdownConverter.markdownToHtml(detail.getContent());
            return org.jsoup.Jsoup.parseBodyFragment(html).select("img").eachAttr("src");
        } catch (Exception e) {
            log.warn("图片尺寸回填解析文章失败: articleId={}", articleId, e);
            return java.util.Collections.emptyList();
        }
    }
}
