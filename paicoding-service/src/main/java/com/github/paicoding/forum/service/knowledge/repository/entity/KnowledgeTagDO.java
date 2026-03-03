package com.github.paicoding.forum.service.knowledge.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_tag")
public class KnowledgeTagDO extends BaseDO {
    private String tagName;
    private Integer status;
    private Integer deleted;
}
