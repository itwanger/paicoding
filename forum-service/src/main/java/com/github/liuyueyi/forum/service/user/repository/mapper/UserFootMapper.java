package com.github.liuyueyi.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;
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
     * 查询文章计数信息
     * @param articleId
     * @return
     */
    ArticleFootCountDTO queryCountByArticle(@Param("articleId")Long articleId);

    /**
     * 查询用户文章计数
     * @param userId
     * @return
     */
    ArticleFootCountDTO queryArticleFootCount(@Param("userId") Long userId);

    /**
     * 查询用户收藏的文章列表
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryCollectionArticleList(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);


    /**
     * 查询用户阅读的文章列表
     * @param userId
     * @param pageParam
     * @return
     */
    List<ArticleDO> queryReadArticleList(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);
}
