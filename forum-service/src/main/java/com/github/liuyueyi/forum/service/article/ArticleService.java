package com.github.liuyueyi.forum.service.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.ArticlePostReq;
import com.github.liuyueyi.forum.service.article.dto.ArticleDTO;
import com.github.liuyueyi.forum.service.article.dto.ArticleListDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;

public interface ArticleService {

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
     * @param req
     * @return
     */
    Long saveArticle(ArticlePostReq req);

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
     * 分页获取文章列表
     *
     * @param pageParam
     * @return
     */
    IPage<ArticleDO> getArticleByPage(PageParam pageParam);

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
