package com.github.paicoding.forum.api.model.vo.comment.dto;

import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * 评论树状结构
 *
 * @author louzai
 * @since 2022/7/19
 */
@ToString(callSuper = true)
@Data
public class SubCommentDTO extends BaseCommentDTO {

    /**
     * 父评论内容
     */
    private String parentContent;


    @Override
    public int compareTo(@NotNull BaseCommentDTO o) {
        return Long.compare(this.getCommentTime(), o.getCommentTime());
    }
}
