package com.github.liuyueyi.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户足迹mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserFootMapper extends BaseMapper<UserFootDO> {
    /**
     * 查询足迹信息
     *
     * @param documentId
     * @param type
     * @param userId
     * @return
     */
    UserFootDO queryFootByDocumentInfo(@Param("documentId") Long documentId, @Param("type") Integer type, @Param("userId") Long userId);

    /**
     * 查询文章计数信息
     *
     * @param articleId
     * @return
     */
    ArticleFootCountDTO queryCountByArticle(@Param("articleId") Long articleId);

    /**
     * 查询作者的文章统计
     *
     * @param author
     * @return
     */
    ArticleFootCountDTO queryArticleFootCount(@Param("userId") Long author);

    /**
     * 查询用户收藏的文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryCollectionArticleList(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);


    /**
     * 查询用户阅读的文章列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryReadArticleList(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);
}
