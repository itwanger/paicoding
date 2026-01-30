package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI生成SEO标题和描述响应
 *
 * @author 沉默王二
 * @date 2026/1/28
 */
@Data
@ApiModel("AI生成SEO标题和描述响应")
public class AiSeoGenerateRes {
    
    @ApiModelProperty("长标题")
    private String title;
    
    @ApiModelProperty("描述")
    private String description;
}
