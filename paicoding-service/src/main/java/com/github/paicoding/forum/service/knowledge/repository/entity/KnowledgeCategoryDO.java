package com.github.paicoding.forum.service.knowledge.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_category")
public class KnowledgeCategoryDO extends BaseDO {
    private Long parentId;
    private Integer level;
    private String categoryName;
    private String slug;
    private Integer rank;
    private Integer status;
    private Integer deleted;
}
