package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/14
 */
public interface ColumnService {
    /**
     * 专栏列表
     *
     * @param pageParam
     * @return
     */
    PageListVo<ColumnDTO> listColumn(PageParam pageParam);

    /**
     * 判断文章是否属于专栏内
     *
     * @param columnId
     * @param articleId
     * @return
     */
    Boolean checkColumnArticle(long columnId, long articleId);

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
}
