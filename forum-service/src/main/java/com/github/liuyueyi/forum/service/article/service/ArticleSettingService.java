package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.enums.OperateArticleEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.PageVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;

/**
 * 文章后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
public interface ArticleSettingService {

    /**
     * 获取文章总数
     *
     * @return
     */
    public Integer getArticleCount();

    /**
     * 获取文章列表
     *
     * @param pageParam
     * @return
     */
    PageVo<ArticleDTO> getArticleList(PageParam pageParam);


    /**
     * 操作文章
     *
     * @param articleId
     * @param operate
     */
    void operateArticle(Long articleId, OperateArticleEnum operate);
}
