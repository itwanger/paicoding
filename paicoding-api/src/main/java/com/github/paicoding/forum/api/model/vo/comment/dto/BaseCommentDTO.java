package com.github.paicoding.forum.api.model.vo.comment.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * 评论树状结构
 *
 * @author louzai
 * @since 2022/7/19
 */
@Data
public class BaseCommentDTO implements Comparable<BaseCommentDTO> {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String userPhoto;

    /**
     * 评论时间
     */
    private Long commentTime;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 点赞数量
     */
    private Integer praiseCount;

    /**
     * true 表示已经点赞
     */
    private Boolean praised;

    /**
     * 高亮信息
     */
    private HighlightDto highlight;

    @Override
    public int compareTo(@NotNull BaseCommentDTO o) {
        return Long.compare(o.getCommentTime(), this.commentTime);
    }

    public BaseCommentDTO setUserPhoto(String userPhoto) {
        this.userPhoto = CdnUtil.autoTransCdn( userPhoto);
        return this;
    }
}
