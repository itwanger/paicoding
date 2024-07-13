package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;

/**
 * 评论Service接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface CommentWriteService {

    /**
     * 更新/保存评论
     *
     * @param commentSaveReq
     * @return
     */
    Long saveComment(CommentSaveReq commentSaveReq);

    /**
     * 删除评论
     *
     * @param commentId
     * @throws Exception
     */
    void deleteComment(Long commentId, Long userId);

}
