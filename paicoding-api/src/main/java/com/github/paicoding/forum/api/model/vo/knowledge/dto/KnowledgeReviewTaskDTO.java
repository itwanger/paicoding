package com.github.paicoding.forum.api.model.vo.knowledge.dto;

import lombok.Data;

import java.util.Date;

@Data
public class KnowledgeReviewTaskDTO {
    private Long taskId;
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
    private Date updateTime;
    private Date createTime;
}
