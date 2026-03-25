package com.github.paicoding.forum.api.model.vo.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("后台评论查询")
public class SearchCommentReq {

    @ApiModelProperty("评论ID")
    private Long commentId;

    @ApiModelProperty("文章ID")
    private Long articleId;

    @ApiModelProperty("文章标题")
    private String articleTitle;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论类型: -1/空-全部, 1-顶级评论, 2-回复")
    private Integer commentType;

    @ApiModelProperty("请求页数，从1开始计数")
    private long pageNumber;

    @ApiModelProperty("请求页大小，默认为10")
    private long pageSize;
}
