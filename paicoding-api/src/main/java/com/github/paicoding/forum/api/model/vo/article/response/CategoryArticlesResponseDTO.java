package com.github.paicoding.forum.api.model.vo.article.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * @program: pai_coding
 * @description: 指定分类下的article响应DTO
 * @author: XuYifei
 * @create: 2024-10-21
 */

@Tag(name = "文章分类的响应对象", description = "查询指定分类下的article响应DTO")
public record CategoryArticlesResponseDTO(
        @Schema(description =  "文章列表") IPage<ArticleDTO> articles,
        @Schema(description =  "分类列表") List<CategoryDTO> categories,
        @Schema(description =  "当前分类下的指定article卡片") List<ArticleDTO> topArticles

) {}
