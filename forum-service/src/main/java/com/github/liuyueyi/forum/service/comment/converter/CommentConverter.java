package com.github.liuyueyi.forum.service.comment.converter;

import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liuyueyi.forum.service.comment.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 评论转换
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class CommentConverter {

    public CommentDO toDo(CommentSaveReq req) {
        if (req == null) {
            return null;
        }
        CommentDO commentDO = new CommentDO();
        commentDO.setId(req.getCommentId());
        commentDO.setArticleId(req.getArticleId());
        commentDO.setUserId(req.getUserId());
        commentDO.setContent(req.getCommentContent());
        commentDO.setParentCommentId(req.getParentCommentId());
        return commentDO;
    }

    public CommentTreeDTO toDTO(CommentDO commentDO) {
        CommentTreeDTO commentTreeDTO = new CommentTreeDTO();
        commentTreeDTO.setUserId(commentDO.getUserId());
        commentTreeDTO.setCommentContent(commentDO.getContent());
        commentTreeDTO.setCommentTime(commentDO.getUpdateTime());
        commentTreeDTO.setParentCommentId(commentDO.getParentCommentId());
        commentTreeDTO.setPraiseCount(0);
        commentTreeDTO.setCommentChilds(new HashMap<>());
        return commentTreeDTO;
    }
}
