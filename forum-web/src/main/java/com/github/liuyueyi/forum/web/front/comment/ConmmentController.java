package com.github.liuyueyi.forum.web.front.comment;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentReq;
import com.github.liuyueyi.forum.service.comment.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.comment.impl.CommentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 评论
 * @author lvmenglou
 * @date : 2022/4/22 10:56
 **/
@Controller
@RequestMapping(path = "comment")
@Slf4j
public class ConmmentController {

    @Resource
    private CommentServiceImpl commentService;

    /**
     * 评论列表页（TODO：异常需要捕获）
     *
     * @param articleId
     * @return
     */
    @GetMapping(path = "list")
    public String list(Long articleId, Long pageNum, Long pageSize, Model model) throws Exception {
        if (articleId == null) {
            throw new Exception("入参错误");
        }
        pageNum = (pageNum == null) ? 1L : pageNum;
        pageSize = (pageSize == null) ? 10L : pageSize;
        Map<Long, CommentTreeDTO> commentTreeDTOMap = commentService.getCommentList(articleId, PageParam.newPageInstance(pageNum, pageSize));
        model.addAttribute("comment", commentTreeDTOMap);
        return "biz/comment/list";
    }

    /**
     * 保存评论（TODO：异常需要捕获）
     *
     * @param commentReq
     * @return
     * @throws Exception
     */
    @PostMapping(path = "save")
    public String save(CommentReq commentReq) throws Exception {
        commentService.saveComment(commentReq);
        return "biz/comment/list";
    }

    /**
     * 删除评论（TODO：异常需要捕获）
     *
     * @param commentId
     * @return
     * @throws Exception
     */
    @PostMapping(path = "delete")
    public String delete(Long commentId) throws Exception {
        commentService.deleteComment(commentId);
        return "biz/comment/list";
    }
}
