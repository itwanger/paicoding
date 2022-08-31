package com.github.liuyueyi.forum.service.article;

import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;

public interface ArticleService {

    ArticleDO querySimpleArticle(Long articleId);

    /**
     * 查询文章详情
     *
     * @param articleId
     * @param updateReadCnt true表示阅读计数+1； false则计数不变
     * @return
     */
    ArticleDTO queryArticleDetail(Long articleId, boolean updateReadCnt);

    /**
     * 保存or更新文章
     *
     * @param req
     * @return
     */
    Long saveArticle(ArticlePostReq req);

    /**
     * 查询某个分类下的文章，支持翻页
     *
     * @param categoryId
     * @param page
     * @return
     */
    ArticleListDTO queryArticlesByCategory(Long categoryId, PageParam page);

    /**
     * 根据查询条件查询文章列表，支持翻页
     *
     * @param key
     * @param page
     * @return
     */
    ArticleListDTO queryArticlesBySearchKey(String key, PageParam page);

    /**
     * 删除文章
     *
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 上线/下线文章
     *
     * @param articleId
     * @param pushStatusEnum
     */
    void operateArticle(Long articleId, PushStatusEnum pushStatusEnum);

    /**
     * 获取用户文章列表
     *
     * @param userId
     * @return
     */
    ArticleListDTO getArticleListByUserId(Long userId, PageParam pageSearchReq);


    /**
     * 获取用户收藏的文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    ArticleListDTO getCollectionArticleListByUserId(Long userId, PageParam pageParam);

    /**
     * 获取用户阅读的文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    ArticleListDTO getReadArticleListByUserId(Long userId, PageParam pageParam);
}
