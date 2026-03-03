package com.github.paicoding.forum.api.model.vo.knowledge.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class KnowledgeDocDTO {
    private Long docId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String contentMd;
    private Integer status;
    private Long createUserId;
    private Long updateUserId;
    private Date publishTime;
    private Date updateTime;
    private List<KnowledgeTagDTO> tags;
}
