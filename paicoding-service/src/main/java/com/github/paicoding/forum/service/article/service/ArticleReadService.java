package com.github.paicoding.forum.service.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;

import java.util.List;
import java.util.Map;

public interface ArticleReadService {

    /**
     * 查询基础的文章信息
     *
     * @param articleId
     * @return
     */
    ArticleDO queryBasicArticle(Long articleId);

    /**
     * 提前文章摘要
     *
     * @param content
     * @return
     */
    String generateSummary(String content);

    /**
     * 查询文章标签列表
     *
     * @param articleId
     * @return
     */
    PageVo<TagDTO> queryTagsByArticleId(Long articleId);

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
    ArticleDTO queryFullArticleInfo(Long articleId, Long currentUser);


    /**
     * 查询某个分类下的文章，支持翻页
     * @param categoryId
     * @param page
     * @return
     */
    PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page);

    /**
     * 使用mybatis-plus分页插件进行查询
     * @param currentPage
     * @param pageSize
     * @param category
     * @return
     */
    IPage<ArticleDTO> queryArticlesByCategoryPagination(int currentPage, int pageSize, String category);

    /**
     * 使用mybatis-plus分页插件进行查询
     * 查询某个标签下的文章
     * @param currentPage
     * @param pageSize
     * @param tagId
     * @return
     */
    IPage<ArticleDTO> queryArticlesByTagPagination(int currentPage, int pageSize, Long tagId);

    /**
     * 获取 Top 文章
     *
     * @param categoryId
     * @return
     */
    List<ArticleDTO> queryTopArticlesByCategory(Long categoryId);


    /**
     * 获取分类文章数
     *
     * @param categoryId
     * @return
     */
    Long queryArticleCountByCategory(Long categoryId);

    /**
     * 根据分类统计文章计数
     *
     * @return
     */
    Map<Long, Long> queryArticleCountsByCategory();

    /**
     * 查询某个标签下的文章，支持翻页
     *
     * @param tagId
     * @param page
     * @return
     */
    PageListVo<ArticleDTO> queryArticlesByTag(Long tagId, PageParam page);

    /**
     * 根据关键词匹配标题，查询用于推荐的文章列表，只返回 articleId + title
     *
     * @param key
     * @return
     */
    List<SimpleArticleDTO> querySimpleArticleBySearchKey(String key);

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
     * 使用mybatis-plus分页插件
     * 查询用户的历史浏览文章列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<ArticleDTO> queryHistoryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize);

    /**
     * 使用mybatis-plus分页插件
     * 查询用户的收藏列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<ArticleDTO> queryStarArticlesByUserIdPagination(Long userId, int currentPage, int pageSize);

    /**
     * 使用mybatis-plus分页插件
     * 查询用户的文章列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<ArticleDTO> queryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize);

    /**
     * 文章实体补齐统计、作者、分类标签等信息
     *
     * @param records
     * @param pageSize
     * @return
     */
    PageListVo<ArticleDTO> buildArticleListVo(List<ArticleDO> records, long pageSize);

    /**
     * 查询热门文章
     *
     * @param pageParam
     * @return
     */
    PageListVo<SimpleArticleDTO> queryHotArticlesForRecommend(PageParam pageParam);

    /**
     * 查询作者的文章数
     *
     * @param authorId
     * @return
     */
    int queryArticleCount(long authorId);

    /**
     * 返回总的文章计数
     *
     * @return
     */
    Long getArticleCount();
}
