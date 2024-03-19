package com.github.paicoding.forum.api.model.vo.feed.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 信息流转发引用的实体
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Data
@Accessors(chain = true)
public class FeedRefDTO implements Serializable {
    private static final long serialVersionUID = 3930399295876974284L;

    @ApiModelProperty("转发引用的文档")
    private Long id;

    @ApiModelProperty("转发的链接")
    private String url;

    @ApiModelProperty("转发内容标题")
    private String title;
    @ApiModelProperty("转发内容描述")
    private String content;
    @ApiModelProperty("转发内容封面图")
    private List<String> imgs;
    @ApiModelProperty("类型")
    private Integer type;
}
