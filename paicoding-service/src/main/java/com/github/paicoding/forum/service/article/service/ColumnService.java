package com.github.paicoding.forum.service.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ColumnService {
    /**
     * 根据文章id，构建对应的专栏详情地址
     *
     * @param articleId 文章主键
     * @return 专栏详情页
     */
    ColumnArticleDO getColumnArticleRelation(Long articleId);

    /**
     * 专栏列表
     *
     * @param pageParam
     * @return
     */
    PageListVo<ColumnDTO> listColumn(PageParam pageParam);

    /**
     * 使用mybatis-plus的分页
     * 专栏列表
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<ColumnDTO> listColumnByPage(Long currentPage, Long pageSize);

    /**
     * 获取专栏中的第N篇文章
     *
     * @param columnId
     * @param order
     * @return
     */
    ColumnArticleDO queryColumnArticle(long columnId, Integer order);


    /**
     * 只查询基本的专栏信息，不需要统计、作者等信息
     *
     * @param columnId
     * @return
     */
    ColumnDTO queryBasicColumnInfo(Long columnId);

    /**
     * 专栏详情
     *
     * @param columnId
     * @return
     */
    ColumnDTO queryColumnInfo(Long columnId);

    /**
     * 专栏 + 文章列表详情
     *
     * @param columnId
     * @return
     */
    List<SimpleArticleDTO> queryColumnArticles(long columnId);

    /**
     * 返回教程数量
     *
     * @return
     */
    Long getTutorialCount();
}
