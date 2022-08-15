package com.github.liuyueyi.forum.web.param.vo;

import com.github.liueyueyi.forum.api.model.vo.comment.dto.CommentTreeDTO;
import lombok.Data;

import java.util.List;

/**
 * 评论返回结果
 * @author lvmenglou
 * @date : 2022/7/23 10:56
 */
@Data
public class CommentListVo {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 热门评论(一期先不做)
     */
    private List<CommentTreeDTO> hotCommentList;

    /**
     * 评论列表
     */
    private List<CommentTreeDTO> commentList;
}
