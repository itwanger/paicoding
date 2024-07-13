package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ColumnArticleMapper extends BaseMapper<ColumnArticleDO> {
    /**
     * 查询文章列表
     *
     * @param columnId
     * @return
     */
    List<SimpleArticleDTO> listColumnArticles(@Param("columnId") Long columnId);

    /**
     * 查询文章
     *
     * @param columnId
     * @param section
     * @return
     */
    ColumnArticleDO getColumnArticle(@Param("columnId") Long columnId, @Param("section") Integer section);


    /**
     * 统计专栏的阅读人数
     *
     * @param columnId
     * @return
     */
    Long countColumnReadUserNums(@Param("columnId") Long columnId);

    /**
     * 根据教程 ID 文章名称查询文章列表
     *
     * @param columnId
     * @param articleTitle
     * @return
     */
    List<ColumnArticleDTO> listColumnArticlesByColumnIdArticleName(@Param("columnId") Long columnId,
                                                                   @Param("articleTitle") String articleTitle,
                                                                   @Param("pageParam") PageParam pageParam);

    Long countColumnArticlesByColumnIdArticleName(@Param("columnId") Long columnId, @Param("articleTitle") String articleTitle);

    /**
     * 根据教程 ID 查询当前教程中最大的 section
     *
     * @param columnId
     * @return 教程内无文章时，返回0
     */
    @Select("select ifnull(max(section), 0) from column_article where column_id = #{columnId}")
    int selectMaxSection(@Param("columnId") Long columnId);
}
