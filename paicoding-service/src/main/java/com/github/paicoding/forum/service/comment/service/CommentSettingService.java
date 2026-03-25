package com.github.paicoding.forum.service.comment.service;

import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.SearchCommentReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.CommentAdminDTO;

public interface CommentSettingService {

    PageVo<CommentAdminDTO> getCommentList(SearchCommentReq req);

    CommentAdminDTO getCommentDetail(Long commentId);

    Long saveComment(CommentSaveReq req, Long operateUserId);

    void deleteComment(Long commentId);
}
