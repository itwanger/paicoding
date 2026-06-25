package com.github.paicoding.forum.api.model.vo.wx.mini;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 微信小程序评论展示项。
 */
@Data
@Accessors(chain = true)
public class WxMiniCommentDTO implements Serializable {
    private static final long serialVersionUID = 4608136123105412238L;

    private Long commentId;
    private Long userId;
    private String userName;
    private String userPhoto;
    private Long commentTime;
    private String commentTimeStr;
    private String commentContent;
    private Integer praiseCount;
    private Boolean praised;
    private Integer childCommentCount;
    private Boolean hasMoreChild;
    private List<WxMiniCommentDTO> childComments;
}
