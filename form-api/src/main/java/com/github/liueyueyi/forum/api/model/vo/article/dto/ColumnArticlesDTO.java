package com.github.liueyueyi.forum.api.model.vo.article.dto;

import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
public class ColumnArticlesDTO {
    /**
     * 专栏详情
     */
    private ColumnDTO column;

    /**
     * 文章列表
     */
    private List<SimpleArticleDTO> articleList;
}
