package com.github.liuyueyi.forum.web.front.comment;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.comment.impl.CommentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评论
 *
 * @author lvmenglou
 * @date : 2022/4/22 10:56
 **/
@Controller
@RequestMapping(path = "comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentServiceImpl commentService;

    /**
     * 评论列表页（TODO：异常需要捕获）
     *
     * @param articleId
     * @return
     */
    @ResponseBody
    @GetMapping(path = "list")
    public ResVo<List<TopCommentDTO>> list(Long articleId, Long pageNum, Long pageSize) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        pageNum = (pageNum == null) ? 1L : pageNum;
        pageSize = (pageSize == null) ? 10L : pageSize;
        List<TopCommentDTO> result = commentService.getArticleComments(articleId, PageParam.newPageInstance(pageNum, pageSize));
        return ResVo.ok(result);
    }

    /**
     * 保存评论（TODO：异常需要捕获）
     *
     * @param req
     * @return
     * @throws Exception
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "post")
    @ResponseBody
    public ResVo<Boolean> save(@RequestBody CommentSaveReq req) {
        if (req.getArticleId() == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        req.setUserId(ReqInfoContext.getReqInfo().getUserId());
        req.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        Long commentId = commentService.saveComment(req);
        return ResVo.ok(NumUtil.upZero(commentId));
    }

    /**
     * 删除评论（TODO：异常需要捕获）
     *
     * @param commentId
     * @return
     * @throws Exception
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "delete")
    public String delete(Long commentId) throws Exception {
        commentService.deleteComment(commentId);
        return "biz/comment/list";
    }
}
