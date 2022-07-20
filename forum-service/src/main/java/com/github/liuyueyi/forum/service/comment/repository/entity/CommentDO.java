package com.github.liuyueyi.forum.service.comment.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liuyueyi.forum.service.common.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表
 *
 * @author louzai
 * @date 2022-07-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class CommentDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String version;

    /**
     * 父评论ID
     */
    private Integer parentCommentId;

    private Integer deleted;
}
