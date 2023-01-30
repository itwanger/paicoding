package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/14
 */
public interface ColumnArticleMapper extends BaseMapper<ColumnArticleDO> {
    /**
     * 查询文章列表
     *
     * @param columnId
     * @return
     */
    List<Long> listColumnArticles(@Param("columnId") Long columnId);

    /**
     * 查询文章
     *
     * @param columnId
     * @param section
     * @return
     */
    Long getColumnArticle(@Param("columnId") Long columnId, @Param("section") Integer section);


    /**
     * 统计专栏的阅读人数
     * @param columnId
     * @return
     */
    Long countColumnReadUserNums(@Param("columnId") Long columnId);
}
