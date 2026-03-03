package com.github.paicoding.forum.api.model.vo.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeAgentAskReqVO {
    private String question;
    private List<Long> contextDocIds;
    private Boolean allowMutation;
}
