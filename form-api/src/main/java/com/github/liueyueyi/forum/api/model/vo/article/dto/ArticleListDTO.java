package com.github.liueyueyi.forum.api.model.vo.article.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章列表信息
 *
 * @author louzai
 * @date 2022/7/31
 */
@Data
public class ArticleListDTO {

    /**
     * 文章列表
     */
    List<ArticleDTO> articleList;

    /**
     * 是否有更多
     */
    private Boolean isMore;
}
