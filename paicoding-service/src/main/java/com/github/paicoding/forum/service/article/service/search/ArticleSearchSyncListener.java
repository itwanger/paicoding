package com.github.paicoding.forum.service.article.service.search;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文章变更后同步 ES 索引。
 */
@Slf4j
@Component
public class ArticleSearchSyncListener {

    @Autowired
    private ArticleSearchService articleSearchService;

    @Async
    @EventListener(ArticleMsgEvent.class)
    public void onArticleEvent(ArticleMsgEvent<?> event) {
        Object content = event.getContent();
        if (!(content instanceof ArticleDO)) {
            return;
        }
        Long articleId = ((ArticleDO) content).getId();
        try {
            if (event.getType() == ArticleEventEnum.DELETE) {
                articleSearchService.deleteArticle(articleId);
            } else {
                articleSearchService.syncArticle(articleId);
            }
        } catch (Exception e) {
            log.warn("failed to handle article search sync event, articleId={}", articleId, e);
        }
    }
}
