package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

@Data
public class KnowledgeReviewReq {
    private Long taskId;
    private String reviewComment;
}
