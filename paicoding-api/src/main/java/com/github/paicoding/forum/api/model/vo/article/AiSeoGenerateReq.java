package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI生成SEO标题和描述请求
 *
 * @author 沉默王二
 * @date 2026/1/28
 */
@Data
@ApiModel("AI生成SEO标题和描述请求")
public class AiSeoGenerateReq {
    
    @ApiModelProperty(value = "短标题", required = true)
    private String shortTitle;
    
    @ApiModelProperty(value = "正文前400字", required = true)
    private String content;
}
