package com.github.paicoding.forum.api.model.vo.article.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @program: pai_coding
 * @description: 指定分类下的article响应DTO
 * @author: XuYifei
 * @create: 2024-10-21
 */

@ApiModel(value = "文章分类的响应对象", description = "查询指定分类下的article响应DTO")
public record CategoryArticlesResponseDTO(
        @ApiModelProperty(value = "文章列表") IPage<ArticleDTO> articles,
        @ApiModelProperty(value = "分类列表") List<CategoryDTO> categories,
        @ApiModelProperty(value = "当前分类下的指定article卡片") List<ArticleDTO> topArticles

) {}
