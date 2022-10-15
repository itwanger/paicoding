package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/26
 */
public interface ArticleRecommendService {

    /**
     * 文章详情页的侧边栏推荐
     *
     * @param articleDO
     * @return
     */
    List<SideBarDTO> recommend(ArticleDTO articleDO);


    /**
     * 文章关联推荐
     *
     * @param article
     * @return
     */
    PageListVo<ArticleDTO> relatedRecommend(Long article, PageParam pageParam);
}
