package com.github.paicoding.forum.api.model.vo.article.response;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @program: pai_coding
 * @description: 给定文章id查询对应专栏信息的响应对象
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Tag(name = "文章专栏关联的响应对象", description = "给定文章id查询对应专栏信息的响应对象")
public record ArticleColumnRelationResponseDTO(
        Long columnId,
        Long articleId,
        Integer section,
        Integer readType
) {
}
