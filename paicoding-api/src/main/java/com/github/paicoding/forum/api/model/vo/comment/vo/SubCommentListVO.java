package com.github.paicoding.forum.api.model.vo.comment.vo;

import com.github.paicoding.forum.api.model.vo.comment.dto.SubCommentDTO;
import lombok.Data;

import java.util.List;

/**
 * 子评论列表响应
 *
 * @author claude
 * @date 2026/03/26
 */
@Data
public class SubCommentListVO {

    /**
     * 子评论列表
     */
    private List<SubCommentDTO> list;

    /**
     * 子评论总数
     */
    private Integer total;

    /**
     * 是否有更多
     */
    private Boolean hasMore;
}
