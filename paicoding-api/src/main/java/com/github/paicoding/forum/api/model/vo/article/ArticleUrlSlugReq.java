package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新文章URL Slug请求。
 *
 * @author Codex
 * @date 2026/5/25
 */
@Data
@ApiModel("更新文章URL Slug请求")
public class ArticleUrlSlugReq implements Serializable {

    private static final long serialVersionUID = -4584712605936187747L;

    @ApiModelProperty(value = "文章ID", required = true)
    private Long articleId;

    @ApiModelProperty(value = "专栏ID，用于校验文章归属")
    private Long columnId;

    @ApiModelProperty(value = "文章URL Slug", required = true)
    private String urlSlug;
}
