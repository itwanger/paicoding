package com.github.paicoding.forum.service.knowledge.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_change_task")
public class KnowledgeChangeTaskDO extends BaseDO {
    private String taskType;
    private Long targetDocId;
    private String payloadJson;
    private String llmPrompt;
    private String llmAnswer;
    private String toolTraceJson;
    private Long proposerUserId;
    private String status;
    private Long reviewerUserId;
    private String reviewComment;
}
