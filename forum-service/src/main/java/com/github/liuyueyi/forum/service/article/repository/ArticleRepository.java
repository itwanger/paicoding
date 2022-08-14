package com.github.liuyueyi.forum.service.article.repository;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;

import java.util.List;
import java.util.Set;

/**
 * 文章相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface ArticleRepository {
    /**
     * 查询文章详情
     *
     * @param articleId
     * @return
     */
    ArticleDTO queryArticleDetail(Long articleId);

    /**
     * 保存or更新文章
     *
     * @param article
     * @param content
     * @param tags
     * @return
     */
    Long saveArticle(ArticleDO article, String content, Set<Long> tags);

    /**
     * 分页获取用户的文章列表
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByUserId(Long userId, PageParam pageParam);


    /**
     * 分页获取文章列表
     * @param categoryId
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByCategoryId(Long categoryId, PageParam pageParam);

    /**
     * 分页获取文章列表（根据查询条件）
     * @param key
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByBySearchKey(String key, PageParam pageParam);
}
