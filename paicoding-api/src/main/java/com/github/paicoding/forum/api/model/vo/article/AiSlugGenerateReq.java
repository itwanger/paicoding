package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 生成文章语义URL请求
 *
 * @author Codex
 * @date 2026/3/26
 */
@Data
@ApiModel("生成文章语义URL请求")
public class AiSlugGenerateReq {

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "文章短标题")
    private String shortTitle;

    @ApiModelProperty(value = "文章ID，更新文章时用于排除自身")
    private Long articleId;

    @ApiModelProperty(value = "所属专栏URL标识，用于生成更贴合专栏上下文的教程文章slug")
    private String columnUrlSlug;
}
