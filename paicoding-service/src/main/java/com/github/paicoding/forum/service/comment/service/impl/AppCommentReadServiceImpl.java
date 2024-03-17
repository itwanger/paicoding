package com.github.paicoding.forum.service.comment.service.impl;

import com.github.paicoding.forum.api.model.entity.BaseDO;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.comment.dto.CurrentCommentDTO;
import com.github.paicoding.forum.service.comment.converter.CommentConverter;
import com.github.paicoding.forum.service.comment.repository.dao.CommentDao;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.AppCommentReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论Service
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class AppCommentReadServiceImpl extends BaseCommentService implements AppCommentReadService {
    @Autowired
    protected CommentDao commentDao;


    @Override
    public List<CurrentCommentDTO> queryLatestCommentsByUser(Long userId, PageParam page) {
        List<CommentDO> comments = commentDao.listRecentCommentsByUser(userId, page);
        return fillCurrentCommentInfo(comments);
    }

    /**
     * 查询最新的评论列表
     *
     * @param articleId 文章
     * @param page      分页
     * @return
     */
    public List<CurrentCommentDTO> queryLatestComments(Long articleId, PageParam page) {
        List<CommentDO> comments = commentDao.listRecentComments(articleId, page);
        return fillCurrentCommentInfo(comments);
    }

    private List<CurrentCommentDTO> fillCurrentCommentInfo(List<CommentDO> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // 找到父评论，查对应的内容
        Set<Long> parentCommentIds = comments.stream().map(CommentDO::getParentCommentId).filter(s -> s > 0)
                .collect(Collectors.toSet());
        Map<Long, CommentDO> parentCommentMap;
        if (!parentCommentIds.isEmpty()) {
            List<CommentDO> parent = commentDao.listByIds(parentCommentIds);
            parentCommentMap = parent.stream().collect(Collectors.toMap(BaseDO::getId, s -> s));
        } else {
            parentCommentMap = Collections.emptyMap();
        }

        // 将父评论信息，回写到当前评论中
        return comments.stream().map(cmt -> {
            CurrentCommentDTO comment = new CurrentCommentDTO();
            CommentConverter.parseDto(cmt, comment);
            comment.setTopCommentId(cmt.getTopCommentId());
            // 补齐评论的用户信息
            fillCommentInfo(comment);

            // 查询父评论
            CommentDO parentDO = parentCommentMap.get(cmt.getParentCommentId());
            if (parentDO != null) {
                CurrentCommentDTO parent = new CurrentCommentDTO();
                CommentConverter.parseDto(parentDO, parent);
                parent.setTopCommentId(parentDO.getTopCommentId());
                fillCommentInfo(parent);
                comment.setParentComment(parent);
            }
            return comment;
        }).collect(Collectors.toList());
    }

}
