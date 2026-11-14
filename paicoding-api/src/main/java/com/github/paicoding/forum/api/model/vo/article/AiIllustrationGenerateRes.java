package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("AI生成文章配图响应")
public class AiIllustrationGenerateRes {

    @ApiModelProperty("生成的配图列表")
    private List<IllustrationItem> illustrations;

    @Data
    @ApiModel("配图项")
    public static class IllustrationItem {
        
        @ApiModelProperty("配图序号")
        private Integer index;

        @ApiModelProperty("配图主题")
        private String theme;

        @ApiModelProperty("核心意思")
        private String coreIdea;

        @ApiModelProperty("建议放置位置（段落序号）")
        private Integer placement;

        @ApiModelProperty("配图URL")
        private String imageUrl;
    }
}