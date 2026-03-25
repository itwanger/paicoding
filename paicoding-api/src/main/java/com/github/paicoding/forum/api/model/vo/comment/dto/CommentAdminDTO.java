package com.github.paicoding.forum.api.model.vo.comment.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentAdminDTO implements Serializable {
    private static final long serialVersionUID = -8338375880093468731L;

    private Long commentId;

    private Long articleId;

    private String articleTitle;

    private Long userId;

    private String userName;

    @JsonSerialize(using = CdnImgSerializer.class)
    private String userAvatar;

    private String commentContent;

    private Long parentCommentId;

    private Long topCommentId;

    private String parentCommentContent;

    private String topCommentContent;

    private Integer commentType;

    private Long replyCount;

    private Long praiseCount;

    private String highlightInfo;

    private Date createTime;

    private Date updateTime;

    public CommentAdminDTO setUserAvatar(String userAvatar) {
        this.userAvatar = CdnUtil.autoTransCdn(userAvatar);
        return this;
    }
}
