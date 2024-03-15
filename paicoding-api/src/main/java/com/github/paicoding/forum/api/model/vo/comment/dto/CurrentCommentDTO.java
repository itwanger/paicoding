package com.github.paicoding.forum.api.model.vo.comment.dto;

import lombok.Data;

/**
 * 当前的评论实体
 *
 * @author YiHui
 * @date 2024/3/15
 */
@Data
public class CurrentCommentDTO extends BaseCommentDTO {

    /**
     * 父评论用户
     */
    private CurrentCommentDTO parentComment;

    private Long topCommentId;

}
