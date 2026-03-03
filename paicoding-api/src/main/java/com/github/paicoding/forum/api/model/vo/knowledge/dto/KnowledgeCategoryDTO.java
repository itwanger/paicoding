package com.github.paicoding.forum.api.model.vo.knowledge.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KnowledgeCategoryDTO {
    private Long categoryId;
    private Long parentId;
    private Integer level;
    private String categoryName;
    private String slug;
    private Integer rank;
    private Integer status;
    private Long docCount;
    private List<KnowledgeCategoryDTO> children = new ArrayList<>();
}
