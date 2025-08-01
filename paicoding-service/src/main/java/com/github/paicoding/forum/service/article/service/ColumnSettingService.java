package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.ColumnArticleGroupReq;
import com.github.paicoding.forum.api.model.vo.article.ColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.ColumnReq;
import com.github.paicoding.forum.api.model.vo.article.MoveColumnArticleOrGroupReq;
import com.github.paicoding.forum.api.model.vo.article.SearchColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.SearchColumnReq;
import com.github.paicoding.forum.api.model.vo.article.SortColumnArticleByIDReq;
import com.github.paicoding.forum.api.model.vo.article.SortColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleGroupDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleColumnDTO;

import java.util.List;

/**
 * 专栏后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
public interface ColumnSettingService {

    /**
     * 将文章保存到对应的专栏中
     *
     * @param articleId
     * @param columnId
     */
    void saveColumnArticle(Long articleId, Long columnId);

    /**
     * 保存专栏
     *
     * @param columnReq
     */
    void saveColumn(ColumnReq columnReq);

    /**
     * 保存专栏文章分组
     *
     * @param req
     * @return
     */
    void saveColumnArticleGroup(ColumnArticleGroupReq req);

    /**
     * 保存专栏文章
     *
     * @param req
     */
    void saveColumnArticle(ColumnArticleReq req);

    /**
     * 删除专栏
     *
     * @param columnId
     */
    void deleteColumn(Long columnId);

    /**
     * 删除专栏文章
     *
     * @param id
     */
    void deleteColumnArticle(Long id);

    /**
     * 通过关键词，从标题中找出相似的进行推荐，只返回主键 + 标题
     *
     * @param key
     * @return
     */
    List<SimpleColumnDTO> listSimpleColumnBySearchKey(String key);

    PageVo<ColumnDTO> getColumnList(SearchColumnReq req);

    PageVo<ColumnArticleDTO> getColumnArticleList(SearchColumnArticleReq req);

    void sortColumnArticleApi(SortColumnArticleReq req);

    void sortColumnArticleByIDApi(SortColumnArticleByIDReq req);



    /**
     * 获取专栏的分组情况
     *
     * @param columnId 专栏id
     * @return
     */
    List<ColumnArticleGroupDTO> getColumnGroups(Long columnId);


    boolean deleteColumnGroup(Long groupId);

    /**
     * 查询专栏下的文章信息
     *
     * @param columnId 专栏
     * @return
     */
    List<ColumnArticleGroupDTO> getColumnGroupAndArticles(Long columnId);


    boolean moveColumnArticleOrGroup(MoveColumnArticleOrGroupReq req);
}
