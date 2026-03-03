package com.github.paicoding.forum.api.model.vo.knowledge.dto;

import lombok.Data;

@Data
public class KnowledgeTagDTO {
    private Long tagId;
    private String tagName;
    private Integer status;
}
