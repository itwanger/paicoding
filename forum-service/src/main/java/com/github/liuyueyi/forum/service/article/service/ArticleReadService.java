package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.enums.HomeSelectEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.RecommendArticleDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;

public interface ArticleReadService {

    /**
     * 查询基础的文章信息
     *
     * @param articleId
     * @return
     */
    ArticleDO queryBasicArticle(Long articleId);


    /**
     * 查询文章详情，包括正文内容，分类、标签等信息
     *
     * @param articleId
     * @return
     */
    ArticleDTO queryDetailArticleInfo(Long articleId);

    /**
     * 查询文章所有的关联信息，正文，分类，标签，阅读计数+1，当前登录用户是否点赞、评论过
     *
     * @param articleId   文章id
     * @param currentUser 当前查看的用户ID
     * @return
     */
    ArticleDTO queryTotalArticleInfo(Long articleId, Long currentUser);

    /**
     * 查询某个分类下的文章，支持翻页
     *
     * @param categoryId
     * @param page
     * @return
     */
    PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page);

    /**
     * 根据查询条件查询文章列表，支持翻页
     *
     * @param key
     * @param page
     * @return
     */
    PageListVo<ArticleDTO> queryArticlesBySearchKey(String key, PageParam page);

    /**
     * 查询用户的文章列表
     *
     * @param userId
     * @param pageParam
     * @param select
     * @return
     */
    PageListVo<ArticleDTO> queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select);

    /**
     * 查询热门文章
     *
     * @param pageParam
     * @return
     */
    PageListVo<RecommendArticleDTO> queryHotArticlesForRecommend(PageParam pageParam);

    /**
     * 查询作者的文章数
     *
     * @param authorId
     * @return
     */
    int queryArticleCount(long authorId);
}
