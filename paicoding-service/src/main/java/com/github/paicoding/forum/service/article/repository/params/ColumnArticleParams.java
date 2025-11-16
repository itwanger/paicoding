package com.github.paicoding.forum.service.article.repository.params;

import lombok.Data;

@Data
public class ColumnArticleParams {
    // 教程 ID
    private Long columnId;
    // 文章 ID
    private Long articleId;
    // section
    private Integer section;
}
