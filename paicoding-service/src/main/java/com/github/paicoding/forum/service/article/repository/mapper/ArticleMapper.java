package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleAdminDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.YearArticleDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ReadCountDO;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    /**
     * 通过id遍历文章, 用于生成sitemap.xml
     *
     * @param lastId
     * @param size
     * @return
     */
    List<SimpleArticleDTO> listArticlesOrderById(@Param("lastId") Long lastId, @Param("size") int size);

    /**
     * 根据阅读次数获取热门文章
     *
     * @param pageParam
     * @return
     */
    List<SimpleArticleDTO> listArticlesByReadCounts(@Param("pageParam") PageParam pageParam);

    /**
     * 查询作者的热门文章
     *
     * @param userId
     * @param pageParam
     * @return
     */
    List<SimpleArticleDTO> listArticlesByUserIdOrderByReadCounts(@Param("userId") Long userId, @Param("pageParam") PageParam pageParam);

    /**
     * 根据类目 + 标签查询文章
     *
     * @param category
     * @param tagIds
     * @param pageParam
     * @return
     */
    List<ReadCountDO> listArticleByCategoryAndTags(@Param("categoryId") Long category,
                                                   @Param("tags") List<Long> tagIds,
                                                   @Param("pageParam") PageParam pageParam);

    /**
     * 根据用户ID获取创作历程
     *
     * @param userId
     * @return
     */
    List<YearArticleDTO> listYearArticleByUserId(@Param("userId") Long userId);

    List<ArticleAdminDTO> listArticlesByParams(@Param("searchParams") SearchArticleParams searchArticleParams,
                                               @Param("pageParam") PageParam pageParam);

    /**
     * 根据用户id查询浏览记录
     * @param userId
     * @return
     */
    IPage<ArticleDO> listHistoryArticlesByUserId(Page<ArticleDO> page,  @Param("userId") Long userId);

    Long countArticlesByParams(@Param("searchParams") SearchArticleParams searchArticleParams);

    IPage<ArticleDO> listStarArticlesByUserId(Page<ArticleDO> page, Long userId);

    /**
     * 查询某个标签下的文章
     * @param page
     * @param tagId
     * @return
     */
    IPage<ArticleDO> selectArticlesByTag(Page<ArticleDO> page, Long tagId);
}
