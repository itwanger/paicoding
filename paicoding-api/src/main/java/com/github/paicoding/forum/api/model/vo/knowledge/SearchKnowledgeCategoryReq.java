package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

@Data
public class SearchKnowledgeCategoryReq {
    private Long parentId;
    private Integer level;
    private String categoryName;
    private Long pageNumber;
    private Long pageSize;
}
