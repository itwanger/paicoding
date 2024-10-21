package com.github.paicoding.forum.api.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xuyifei
 */
@Data
@Tag(name = "教程排序，根据 ID 和新填的排序")
public class SortColumnArticleByIDReq implements Serializable {
    // 要排序的 id
    @Schema(description = "要排序的 id")
    private Long id;
    // 新的排序
    @Schema(description = "新的排序")
    private Integer sort;
}
