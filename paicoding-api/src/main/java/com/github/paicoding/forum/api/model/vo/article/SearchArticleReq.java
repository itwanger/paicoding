package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Data
@Tag(name = "文章查询")
public class SearchArticleReq {

    // 文章标题
    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "作者ID")
    private Long userId;

    @Schema(description = "作者名称")
    private String userName;

    @Schema(description = "文章状态: 0-未发布，1-已发布，2-审核")
    private Integer status;

    @Schema(description = "是否官方: 0-非官方，1-官方")
    private Integer officalStat;

    @Schema(description = "是否置顶: 0-不置顶，1-置顶")
    private Integer toppingStat;

    @Schema(description = "请求页数，从1开始计数")
    private long pageNumber;

    @Schema(description = "请求页大小，默认为 10")
    private long pageSize;
}
