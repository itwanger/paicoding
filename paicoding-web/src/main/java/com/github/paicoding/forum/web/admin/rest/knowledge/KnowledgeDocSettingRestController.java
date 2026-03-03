package com.github.paicoding.forum.web.admin.rest.knowledge;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.SearchKnowledgeDocReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeDocDTO;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeTagDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeDocService;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Permission(role = UserRole.LOGIN)
@RequestMapping(path = {"api/admin/knowledge/doc/", "admin/knowledge/doc/"})
@RequiredArgsConstructor
public class KnowledgeDocSettingRestController {

    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeTagService knowledgeTagService;

    @PostMapping("list")
    public ResVo<PageVo<KnowledgeDocDTO>> list(@RequestBody SearchKnowledgeDocReq req) {
        return ResVo.ok(knowledgeDocService.queryAdminDocs(req));
    }

    @GetMapping("detail")
    public ResVo<KnowledgeDocDTO> detail(@RequestParam Long docId) {
        KnowledgeDocDTO detail = knowledgeDocService.queryAdminDocDetail(docId);
        if (detail == null) {
            return ResVo.fail(StatusEnum.RECORDS_NOT_EXISTS);
        }
        return ResVo.ok(detail);
    }

    @GetMapping("tags")
    public ResVo<List<KnowledgeTagDTO>> tags() {
        return ResVo.ok(knowledgeTagService.listAllOnlineTags());
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping("save")
    public ResVo<Long> save(@RequestBody KnowledgeDocReq req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(knowledgeDocService.saveDoc(req, userId));
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping("delete")
    public ResVo<String> delete(@RequestParam Long docId) {
        knowledgeDocService.deleteDoc(docId);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping("operate")
    public ResVo<String> operate(@RequestParam Long docId, @RequestParam Integer status) {
        knowledgeDocService.operateDoc(docId, status);
        return ResVo.ok("ok");
    }
}
