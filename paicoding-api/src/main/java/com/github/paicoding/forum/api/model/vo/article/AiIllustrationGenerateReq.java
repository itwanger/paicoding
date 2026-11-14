package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("AI生成文章配图请求")
public class AiIllustrationGenerateReq {

    @ApiModelProperty(value = "文章标题", required = true)
    private String title;

    @ApiModelProperty(value = "文章正文内容", required = true)
    private String content;

    @ApiModelProperty(value = "生成配图数量，默认4张，范围1-8")
    private Integer count;
}