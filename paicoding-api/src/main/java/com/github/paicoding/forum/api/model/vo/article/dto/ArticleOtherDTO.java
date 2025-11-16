package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

@Data
public class ArticleOtherDTO {
    // 文章的阅读类型
    private Integer readType;
    // 教程的翻页
    private ColumnArticleFlipDTO flip;
}
