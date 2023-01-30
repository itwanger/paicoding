package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.article.ArticlePostReq;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ArticleTagDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleWriteService;
import com.github.paicoding.forum.service.image.service.ImageService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

/**
 * 文章操作相关服务类
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class ArticleWriteServiceImpl implements ArticleWriteService {

    private final ArticleDao articleDao;

    private final ArticleTagDao articleTagDao;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private ImageService imageService;

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
        String content = imageService.mdImgReplace(req.getContent());
        if (NumUtil.nullOrZero(req.getArticleId())) {
            return insertArticle(article, content, req.getTagIds());
        } else {
            return updateArticle(article, content, req.getTagIds());
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
        if (article.getStatus() == PushStatusEnum.ONLINE.getCode()) {
            article.setStatus(PushStatusEnum.REVIEW.getCode());
        }
        articleDao.save(article);
        Long articleId = article.getId();

        articleDao.saveArticleContent(articleId, content);

        articleTagDao.batchSave(articleId, tags);

        // 发布文章，阅读计数+1
        userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(), article.getUserId(), OperateTypeEnum.READ);
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
        // 若文章处于审核状态，则直接更新上一条记录；否则新插入一条记录
        boolean review = article.getStatus().equals(PushStatusEnum.REVIEW.getCode());
        if (article.getStatus() == PushStatusEnum.ONLINE.getCode()) {
            article.setStatus(PushStatusEnum.REVIEW.getCode());
        }
        // 更新文章
        articleDao.updateById(article);

        // 更新内容
        articleDao.updateArticleContent(article.getId(), content, review);

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
    public void deleteArticle(Long articleId, Long loginUserId) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && !Objects.equals(dto.getUserId(), loginUserId)) {
            // 没有权限
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "请确认文章是否属于您!");
        }

        if (dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);
        }
    }
}
