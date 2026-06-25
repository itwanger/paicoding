package com.github.paicoding.forum.api.model.vo.wx.mini;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信小程序评论分页响应。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class WxMiniCommentPageDTO extends PageListVo<WxMiniCommentDTO> implements Serializable {
    private static final long serialVersionUID = -7856874278797260127L;

    /**
     * 当前请求新创建的评论 ID；列表类、删除类和点赞类响应为空。
     */
    private Long submittedCommentId;
}
