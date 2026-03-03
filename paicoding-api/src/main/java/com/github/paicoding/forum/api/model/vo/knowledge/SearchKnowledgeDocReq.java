package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

@Data
public class SearchKnowledgeDocReq {
    private Long categoryId;
    private Long tagId;
    private String keyword;
    private Integer status;
    private Long pageNumber;
    private Long pageSize;
}
