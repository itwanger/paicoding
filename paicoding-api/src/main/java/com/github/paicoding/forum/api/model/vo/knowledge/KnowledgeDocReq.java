package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeDocReq {
    private Long docId;
    private Long categoryId;
    private String title;
    private String description;
    private String contentMd;
    private Integer status;
    private List<Long> tagIds;
}
