package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

@Data
public class KnowledgeCategoryReq {
    private Long categoryId;
    private Long parentId;
    private Integer level;
    private String categoryName;
    private String slug;
    private Integer rank;
    private Integer status;
}
