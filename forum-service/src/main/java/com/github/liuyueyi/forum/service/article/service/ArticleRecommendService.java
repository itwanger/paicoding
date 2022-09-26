package com.github.liuyueyi.forum.service.article.service;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.recommend.SideBarDTO;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/26
 */
public interface ArticleRecommendService {

    List<SideBarDTO> recommend(ArticleDTO articleDO);


}
