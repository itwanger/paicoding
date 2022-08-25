package com.github.liuyueyi.forum.service.article.repository;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
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
     * 查询文章详情, 包含正文，分类，标签等全量信息
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
     * 获取文章基本信息，不包含正文、分类、标签等
     *
     * @param articleId
     * @return
     */
    ArticleDO getSimpleArticle(Long articleId);

    /**
     * 分页获取用户的文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByUserId(Long userId, PageParam pageParam);


    /**
     * 分页获取文章列表
     *
     * @param categoryId
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByCategoryId(Long categoryId, PageParam pageParam);

    /**
     * 分页获取文章列表（根据查询条件）
     *
     * @param key
     * @param pageParam
     * @return
     */
    List<ArticleDO> getArticleListByBySearchKey(String key, PageParam pageParam);

    /**
     * 访问计数
     * @param articleId
     * @return
     */
    int count(Long articleId);
}
