package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.SearchCommentReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.CommentAdminDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.comment.service.CommentSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Permission(role = UserRole.LOGIN)
@Api(value = "评论管理控制器", tags = "评论管理")
@RequestMapping(path = {"api/admin/comment/", "admin/comment/"})
public class CommentSettingRestController {

    @Autowired
    private CommentSettingService commentSettingService;

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("评论列表")
    @PostMapping(path = "list")
    public ResVo<PageVo<CommentAdminDTO>> list(@RequestBody SearchCommentReq req) {
        return ResVo.ok(commentSettingService.getCommentList(req));
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("评论详情")
    @GetMapping(path = "detail")
    public ResVo<CommentAdminDTO> detail(@RequestParam(name = "commentId") Long commentId) {
        return ResVo.ok(commentSettingService.getCommentDetail(commentId));
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("保存评论")
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody CommentSaveReq req) {
        if (req.getCommentContent() != null) {
            req.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        }
        commentSettingService.saveComment(req, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok();
    }

    @Permission(role = UserRole.ADMIN)
    @ApiOperation("删除评论")
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "commentId") Long commentId) {
        commentSettingService.deleteComment(commentId);
        return ResVo.ok();
    }
}
