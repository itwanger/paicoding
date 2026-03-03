package com.github.paicoding.forum.api.model.vo.knowledge.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KnowledgeAgentAskRespVO {
    private String answer;
    private Long proposalTaskId;
    private String proposalStatus;
    private List<Citation> citations = new ArrayList<>();

    @Data
    public static class Citation {
        private Long docId;
        private String title;
    }
}
