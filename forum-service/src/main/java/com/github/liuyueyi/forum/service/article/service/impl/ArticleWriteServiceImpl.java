package com.github.liuyueyi.forum.service.article.service.impl;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.article.conveter.ArticleConverter;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleDao;
import com.github.liuyueyi.forum.service.article.repository.dao.ArticleTagDao;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.service.ArticleWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 *  文章操作相关服务类
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleWriteServiceImpl implements ArticleWriteService {

    private final ArticleDao articleDao;

    private final ArticleTagDao articleTagDao;

    public ArticleWriteServiceImpl(ArticleDao articleDao, ArticleTagDao articleTagDao) {
        this.articleDao = articleDao;
        this.articleTagDao = articleTagDao;
    }

    /**
     * 保存文章，当articleId存在时，表示更新记录； 不存在时，表示插入
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveArticle(ArticlePostReq req, Long author) {
        ArticleDO article = ArticleConverter.toArticleDo(req, author);
        if (NumUtil.upZero(req.getArticleId())) {
            return insertArticle(article, req.getContent(), req.getTagIds());
        } else {
            return updateArticle(article, req.getContent(), req.getTagIds());
        }
    }

    /**
     * 新建文章
     *
     * @param article
     * @param content
     * @param tags
     * @return
     */
    private Long insertArticle(ArticleDO article, String content, Set<Long> tags) {
        // article + article_detail + tag  三张表的数据变更
        articleDao.save(article);
        Long articleId = article.getId();

        articleDao.saveArticleContent(articleId, content);

        articleTagDao.batchSave(articleId, tags);
        return articleId;
    }

    /**
     * 更新文章
     *
     * @param article
     * @param content
     * @param tags
     * @return
     */
    private Long updateArticle(ArticleDO article, String content, Set<Long> tags) {
        // 更新文章
        articleDao.updateById(article);

        // 更新内容
        articleDao.updateArticleContent(article.getId(), content);

        // 标签更新
        articleTagDao.updateTags(article.getId(), tags);
        return article.getId();
    }


    /**
     * 删除文章
     *
     * @param articleId
     */
    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);
        }
    }

    /**
     * 文章上下线
     *
     * @param articleId
     * @param pushStatusEnum
     */
    @Override
    public void operateArticle(Long articleId, PushStatusEnum pushStatusEnum) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && dto.getStatus() != pushStatusEnum.getCode()) {
            dto.setStatus(pushStatusEnum.getCode());
            articleDao.updateById(dto);
        }
    }
}
