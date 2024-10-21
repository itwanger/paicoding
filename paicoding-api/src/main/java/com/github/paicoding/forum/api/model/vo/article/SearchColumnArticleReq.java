package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Data
@Tag(name = "教程配套文章查询")
public class SearchColumnArticleReq {

    // 教程名称
    @Schema(description = "教程名称")
    private String column;

    // 教程 ID
    @Schema(description = "教程 ID")
    private Long columnId;

    // 文章标题
    @Schema(description = "文章标题")
    private String articleTitle;

    @Schema(description = "请求页数，从1开始计数")
    private long pageNumber;

    @Schema(description = "请求页大小，默认为 10")
    private long pageSize;
}
