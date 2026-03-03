package com.github.paicoding.forum.web.admin.rest.knowledge;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.knowledge.KnowledgeReviewReq;
import com.github.paicoding.forum.api.model.vo.knowledge.dto.KnowledgeReviewTaskDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.knowledge.service.KnowledgeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@Permission(role = UserRole.LOGIN)
@RequestMapping(path = {"api/admin/knowledge/review/", "admin/knowledge/review/"})
@RequiredArgsConstructor
public class KnowledgeReviewSettingRestController {

    private final KnowledgeReviewService reviewService;

    @PostMapping("list")
    public ResVo<PageVo<KnowledgeReviewTaskDTO>> list(@RequestBody QueryReq req) {
        return ResVo.ok(reviewService.queryPage(req.getStatus(), req.getPageNumber(), req.getPageSize()));
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping("approve")
    public ResVo<String> approve(@RequestBody KnowledgeReviewReq req) {
        Long reviewerId = ReqInfoContext.getReqInfo().getUserId();
        boolean ok = reviewService.approveTask(req.getTaskId(), reviewerId, req.getReviewComment());
        return ok ? ResVo.ok("ok") : ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping("reject")
    public ResVo<String> reject(@RequestBody KnowledgeReviewReq req) {
        Long reviewerId = ReqInfoContext.getReqInfo().getUserId();
        boolean ok = reviewService.rejectTask(req.getTaskId(), reviewerId, req.getReviewComment());
        return ok ? ResVo.ok("ok") : ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
    }

    public static class QueryReq {
        private String status;
        private Long pageNumber;
        private Long pageSize;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(Long pageNumber) {
            this.pageNumber = pageNumber;
        }

        public Long getPageSize() {
            return pageSize;
        }

        public void setPageSize(Long pageSize) {
            this.pageSize = pageSize;
        }
    }
}
