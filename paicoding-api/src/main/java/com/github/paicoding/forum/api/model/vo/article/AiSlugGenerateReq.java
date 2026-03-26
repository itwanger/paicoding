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
}
