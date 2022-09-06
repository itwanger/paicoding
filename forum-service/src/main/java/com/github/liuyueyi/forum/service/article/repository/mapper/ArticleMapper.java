package com.github.liuyueyi.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.RecommendArticleDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    /**
     * 根据阅读次数获取人们文章
     *
     * @param pageParam
     * @return
     */
    List<RecommendArticleDTO> listArticlesByReadCounts(@Param("pageParam") PageParam pageParam);

}
