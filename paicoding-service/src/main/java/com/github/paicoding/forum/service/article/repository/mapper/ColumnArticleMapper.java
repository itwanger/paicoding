package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据教程 ID 文章名称查询文章列表
     * @param columnId
     * @param articleName
     * @return
     */
    List<ColumnArticleDTO> listColumnArticlesByColumnIdArticleName(@Param("columnId") Long columnId,
                                                                   @Param("articleTitle") String articleTitle,
                                                                   @Param("pageParam") PageParam pageParam);

    Long countColumnArticlesByColumnIdArticleName(@Param("columnId") Long columnId, @Param("articleTitle") String articleTitle);

    /**
     * 根据教程 ID 查询当前教程中最大的 section
     * @param columnId
     * @return
     */
    @Select("select max(section) from column_article where column_id = #{columnId}")
    Integer selectMaxSection(@Param("columnId") Long columnId);

}
