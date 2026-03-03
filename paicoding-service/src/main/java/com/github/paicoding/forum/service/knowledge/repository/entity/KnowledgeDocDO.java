package com.github.paicoding.forum.service.knowledge.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_doc")
public class KnowledgeDocDO extends BaseDO {
    private Long categoryId;
    private String title;
    private String description;
    private String contentMd;
    private Integer status;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date publishTime;
}
