package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ArticleRecommendService {
    /**
     * 文章关联推荐
     *
     * @param article
     * @param pageParam
     * @return
     */
    PageListVo<ArticleDTO> relatedRecommend(Long article, PageParam pageParam);
}
