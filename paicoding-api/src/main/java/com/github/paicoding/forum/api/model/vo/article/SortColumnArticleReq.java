package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;


/**
 * @author xuyifei
 */
@Data
@Tag(name = "教程排序")
public class SortColumnArticleReq implements Serializable {
    // 排序前的文章 ID
    @Schema(description = "排序前的文章 ID")
    private Long activeId;

    // 排序后的文章 ID
    @Schema(description = "排序后的文章 ID")
    private Long overId;

}
