package com.github.liuyueyi.forum.service.converter;

import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.common.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.common.enums.YesOrNoEnum;
import com.github.liuyueyi.forum.service.common.req.CommentReq;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 评论转换
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class CommentConverter {

    public CommentDO toDo(CommentReq req) {
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
