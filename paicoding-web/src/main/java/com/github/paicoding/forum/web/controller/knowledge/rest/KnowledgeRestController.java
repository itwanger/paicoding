package com.github.paicoding.forum.web.controller.knowledge.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeAgentAskReqVO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeAgentAskRespVO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeCategoryDTO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeDocDTO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeTagDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeAgentService;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeCategoryService;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeDocService;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("knowledge/api")
@RequiredArgsConstructor
public class KnowledgeRestController {

    private final KnowledgeCategoryService knowledgeCategoryService;
    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeTagService knowledgeTagService;
    private final KnowledgeAgentService knowledgeAgentService;

    @GetMapping("tree")
    public ResVo<List<KnowledgeCategoryDTO>> tree() {
        return ResVo.ok(knowledgeCategoryService.queryTreeForGuest());
    }

    @GetMapping("tags")
    public ResVo<List<KnowledgeTagDTO>> tags() {
        return ResVo.ok(knowledgeTagService.listAllOnlineTags());
    }

    @GetMapping("docs")
    public ResVo<PageVo<KnowledgeDocDTO>> docs(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Long tagId,
                                                @RequestParam(required = false) Long page,
                                                @RequestParam(required = false) Long size) {
        return ResVo.ok(knowledgeDocService.queryPublishedDocs(categoryId, tagId, null, page, size));
    }

    @GetMapping("search")
    public ResVo<PageVo<KnowledgeDocDTO>> search(@RequestParam String q,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) Long tagId,
                                                  @RequestParam(required = false) Long page,
                                                  @RequestParam(required = false) Long size) {
        return ResVo.ok(knowledgeDocService.queryPublishedDocs(categoryId, tagId, q, page, size));
    }

    @GetMapping("doc/{docId}")
    public ResVo<KnowledgeDocDTO> docDetail(@PathVariable Long docId) {
        KnowledgeDocDTO doc = knowledgeDocService.queryPublishedDocDetail(docId);
        if (doc == null) {
            return ResVo.fail(StatusEnum.RECORDS_NOT_EXISTS);
        }
        return ResVo.ok(doc);
    }

    @PostMapping("agent/ask")
    @Permission(role = UserRole.LOGIN)
    public ResVo<KnowledgeAgentAskRespVO> ask(@RequestBody KnowledgeAgentAskReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        boolean admin = ReqInfoContext.getReqInfo().getUser() != null
                && UserRole.ADMIN.name().equalsIgnoreCase(ReqInfoContext.getReqInfo().getUser().getRole());
        return ResVo.ok(knowledgeAgentService.ask(req, userId, admin));
    }
}
