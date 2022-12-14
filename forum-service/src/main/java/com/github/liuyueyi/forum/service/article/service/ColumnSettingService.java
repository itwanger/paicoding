package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnArticleReq;
import com.github.liueyueyi.forum.api.model.vo.article.ColumnReq;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.SimpleArticleDTO;

import java.util.List;

/**
 * 专栏后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
public interface ColumnSettingService {

    /**
     * 保存专栏
     *
     * @param columnReq
     */
    void saveColumn(ColumnReq columnReq);

    /**
     * 保存专栏文章
     *
     * @param req
     */
    void saveColumnArticle(ColumnArticleReq req);

    /**
     * 专栏文章排序
     *
     * @param columnArticleReqs
     */
    void sortColumnArticle(List<ColumnArticleReq> columnArticleReqs);

    /**
     * 删除专栏
     *
     * @param columnId
     */
    void deleteColumn(Integer columnId);

    /**
     * 删除专栏文章
     *
     * @param id
     */
    void deleteColumnArticle(Integer id);

    /**
     * 专栏列表
     *
     * @param pageParam
     * @return
     */
    PageVo<ColumnDTO> listColumn(PageParam pageParam);

    /**
     * 查询专栏的文章详情
     *
     * @param columnId
     * @return
     */
    PageVo<ColumnArticleDTO> queryColumnArticles(long columnId, PageParam pageParam) throws Exception;
}
