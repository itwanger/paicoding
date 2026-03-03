package com.github.paicoding.forum.service.knowledge.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeDocDTO;
import com.github.paicoding.forum.service.knowledge.constants.KnowledgeConst;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeDocService;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeToolService {

    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeReviewService reviewService;
    private final ObjectMapper objectMapper;

    public List<KnowledgeDocDTO> searchKnowledgeDocs(String query, Long categoryId) {
        return knowledgeDocService.queryPublishedDocs(categoryId, null, query, 1L, 5L).getList();
    }

    public KnowledgeDocDTO getKnowledgeDoc(Long docId) {
        return knowledgeDocService.queryPublishedDocDetail(docId);
    }

    public Long proposeCreateKnowledgeDoc(KnowledgeDocReq req,
                                          String llmPrompt,
                                          String llmAnswer,
                                          String trace,
                                          Long proposerUserId) throws JsonProcessingException {
        return reviewService.createTask(
                KnowledgeConst.TASK_CREATE,
                null,
                objectMapper.writeValueAsString(req),
                llmPrompt,
                llmAnswer,
                trace,
                proposerUserId
        );
    }

    public Long proposeUpdateKnowledgeDoc(KnowledgeDocReq req,
                                          String llmPrompt,
                                          String llmAnswer,
                                          String trace,
                                          Long proposerUserId) throws JsonProcessingException {
        return reviewService.createTask(
                KnowledgeConst.TASK_UPDATE,
                req.getDocId(),
                objectMapper.writeValueAsString(req),
                llmPrompt,
                llmAnswer,
                trace,
                proposerUserId
        );
    }

    public Map<String, Object> toolResult(String tool, Object result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tool", tool);
        response.put("result", result);
        return response;
    }
}
