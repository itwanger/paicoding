package com.github.paicoding.forum.service.knowledge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeAgentAskReqVO;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeAgentAskRespVO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeDocDTO;
import com.github.paicoding.forum.service.knowledge.config.KnowledgeChatClientFactory;
import com.github.paicoding.forum.service.knowledge.constants.KnowledgeConst;
import com.github.paicoding.forum.service.knowledge.tool.KnowledgeToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeAgentService {

    private final KnowledgeChatClientFactory chatClientFactory;
    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeToolService toolService;
    private final ObjectMapper objectMapper;

    public KnowledgeAgentAskRespVO ask(KnowledgeAgentAskReqVO req, Long userId, boolean admin) {
        ChatClient chatClient = chatClientFactory.createClient();

        List<KnowledgeDocDTO> seedDocs = loadSeedDocs(req);
        List<Map<String, Object>> toolTrace = new ArrayList<>();

        ToolPlan plan = buildToolPlan(chatClient, req, seedDocs, admin);
        if (plan == null) {
            return buildAnswer(chatClient, req.getQuestion(), seedDocs, null, null);
        }

        String tool = plan.tool;
        if ("searchKnowledgeDocs".equals(tool)) {
            String query = asString(plan.arguments.get("query"));
            Long categoryId = asLong(plan.arguments.get("categoryId"));
            List<KnowledgeDocDTO> searched = toolService.searchKnowledgeDocs(query, categoryId);
            toolTrace.add(toolService.toolResult(tool, searched.stream().map(KnowledgeDocDTO::getDocId).toList()));
            return buildAnswer(chatClient, req.getQuestion(), searched, null, null);
        }

        if ("getKnowledgeDoc".equals(tool)) {
            Long docId = asLong(plan.arguments.get("docId"));
            if (docId != null) {
                KnowledgeDocDTO doc = toolService.getKnowledgeDoc(docId);
                List<KnowledgeDocDTO> docs = doc == null ? Collections.emptyList() : List.of(doc);
                toolTrace.add(toolService.toolResult(tool, docId));
                return buildAnswer(chatClient, req.getQuestion(), docs, null, null);
            }
            return buildAnswer(chatClient, req.getQuestion(), seedDocs, null, null);
        }

        if (("proposeCreateKnowledgeDoc".equals(tool) || "proposeUpdateKnowledgeDoc".equals(tool)) && admin
                && Boolean.TRUE.equals(req.getAllowMutation())) {
            return handleMutation(req, userId, plan, toolTrace);
        }

        return buildAnswer(chatClient, req.getQuestion(), seedDocs, null, null);
    }

    private KnowledgeAgentAskRespVO handleMutation(KnowledgeAgentAskReqVO req,
                                                    Long userId,
                                                    ToolPlan plan,
                                                    List<Map<String, Object>> toolTrace) {
        KnowledgeAgentAskRespVO resp = new KnowledgeAgentAskRespVO();
        try {
            KnowledgeDocReq docReq = objectMapper.convertValue(plan.arguments.get("doc"), KnowledgeDocReq.class);
            if (docReq == null) {
                resp.setAnswer("生成提案失败：缺少文档结构化内容。");
                return resp;
            }

            String trace = objectMapper.writeValueAsString(toolTrace);
            Long taskId;
            if ("proposeCreateKnowledgeDoc".equals(plan.tool)) {
                taskId = toolService.proposeCreateKnowledgeDoc(docReq, req.getQuestion(), plan.raw, trace, userId);
            } else {
                if (docReq.getDocId() == null || docReq.getDocId() <= 0) {
                    resp.setAnswer("更新提案失败：缺少目标文档ID。");
                    return resp;
                }
                taskId = toolService.proposeUpdateKnowledgeDoc(docReq, req.getQuestion(), plan.raw, trace, userId);
            }

            resp.setProposalTaskId(taskId);
            resp.setProposalStatus(KnowledgeConst.REVIEW_PENDING);
            resp.setAnswer("已生成知识库变更提案，等待管理员审核后发布。任务ID: " + taskId);
            return resp;
        } catch (JsonProcessingException e) {
            log.error("Create knowledge proposal failed", e);
            resp.setAnswer("创建提案失败，请重试。");
            return resp;
        }
    }

    private KnowledgeAgentAskRespVO buildAnswer(ChatClient client,
                                                String question,
                                                List<KnowledgeDocDTO> docs,
                                                Long proposalTaskId,
                                                String proposalStatus) {
        KnowledgeAgentAskRespVO resp = new KnowledgeAgentAskRespVO();
        if (docs == null || docs.isEmpty()) {
            resp.setAnswer("当前知识库没有检索到可用内容，我可以在你提供更具体关键词后继续检索。");
            return resp;
        }

        String context = docs.stream()
                .map(d -> "[docId=" + d.getDocId() + "] 标题: " + d.getTitle() + "\n描述: " + d.getDescription() + "\n内容: " + safeContent(d.getContentMd()))
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("");

        String prompt = "你是知识库问答助手。仅基于给定文档回答，若文档不足请明确说明。" +
                "回答使用中文，结尾给出引用文档ID列表。\n\n问题: " + question + "\n\n文档上下文:\n" + context;

        String answer = client.prompt().user(prompt).call().content();
        resp.setAnswer(answer);
        resp.setProposalTaskId(proposalTaskId);
        resp.setProposalStatus(proposalStatus);
        for (KnowledgeDocDTO doc : docs) {
            KnowledgeAgentAskRespVO.Citation citation = new KnowledgeAgentAskRespVO.Citation();
            citation.setDocId(doc.getDocId());
            citation.setTitle(doc.getTitle());
            resp.getCitations().add(citation);
        }
        return resp;
    }

    private ToolPlan buildToolPlan(ChatClient client,
                                   KnowledgeAgentAskReqVO req,
                                   List<KnowledgeDocDTO> seedDocs,
                                   boolean admin) {
        String docsHint = seedDocs.stream()
                .map(d -> "{\"docId\":" + d.getDocId() + ",\"title\":\"" + escape(d.getTitle()) + "\"}")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        String plannerPrompt = "你是知识库Agent。你必须在下列工具中选择一个并返回JSON，不要输出其他文本。\n" +
                "工具: searchKnowledgeDocs, getKnowledgeDoc, proposeCreateKnowledgeDoc, proposeUpdateKnowledgeDoc, finalAnswer\n" +
                "限制: 非管理员或allowMutation=false时，禁止选择propose*工具。\n" +
                "返回格式: {\"tool\":\"...\",\"arguments\":{...}}\n" +
                "问题: " + req.getQuestion() + "\n" +
                "allowMutation: " + Boolean.TRUE.equals(req.getAllowMutation()) + "\n" +
                "isAdmin: " + admin + "\n" +
                "候选文档: [" + docsHint + "]\n" +
                "若问题为检索问答优先使用searchKnowledgeDocs；若用户点名文档ID使用getKnowledgeDoc；" +
                "若是创建/更新知识文档请求并且有权限才可使用propose*。";

        String raw = client.prompt().user(plannerPrompt).call().content();
        String json = normalizeJson(raw);
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            ToolPlan plan = new ToolPlan();
            plan.raw = raw;
            plan.tool = asString(map.get("tool"));
            Object args = map.get("arguments");
            if (args instanceof Map<?, ?> m) {
                plan.arguments = (Map<String, Object>) m;
            } else {
                plan.arguments = new LinkedHashMap<>();
            }
            return plan;
        } catch (Exception e) {
            log.warn("Failed to parse tool plan, raw={}", raw, e);
            return null;
        }
    }

    private List<KnowledgeDocDTO> loadSeedDocs(KnowledgeAgentAskReqVO req) {
        if (req.getContextDocIds() != null && !req.getContextDocIds().isEmpty()) {
            return knowledgeDocService.queryDocByIds(req.getContextDocIds(), true);
        }
        return knowledgeDocService.queryPublishedDocs(null, null, req.getQuestion(), 1L, 5L).getList();
    }

    private String normalizeJson(String raw) {
        if (raw == null) {
            return "{}";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int first = trimmed.indexOf('{');
            int last = trimmed.lastIndexOf('}');
            if (first >= 0 && last > first) {
                return trimmed.substring(first, last + 1);
            }
        }
        return trimmed;
    }

    private String safeContent(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 1200 ? content.substring(0, 1200) + "..." : content;
    }

    private String escape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private Long asLong(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    private static class ToolPlan {
        private String raw;
        private String tool;
        private Map<String, Object> arguments = new LinkedHashMap<>();
    }
}
