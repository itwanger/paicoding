package com.github.liuyueyi.forum.service.comment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.BaseCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.SubCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.comment.CommentService;
import com.github.liuyueyi.forum.service.comment.converter.CommentConverter;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.comment.repository.mapper.CommentMapper;
import com.github.liuyueyi.forum.service.user.UserService;
import com.github.liuyueyi.forum.service.user.impl.UserFootServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论Service
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private CommentConverter commentConverter;

    @Resource
    private UserService userService;

    @Autowired
    private UserFootServiceImpl userFootService;

    @Override
    public List<TopCommentDTO> getArticleComments(Long articleId, PageParam page) {
        // 1.查询一级评论
        List<CommentDO> comments = queryTopCommentList(articleId, page);
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        // map 存 commentId -> 评论
        Map<Long, TopCommentDTO> maps = comments.stream().collect(Collectors.toMap(CommentDO::getId, commentConverter::toTopDto));

        // 2.查询非一级评论
        List<CommentDO> subComments = this.querySubCommentIdMappers(articleId, maps.keySet());
        Map<Long, SubCommentDTO> subCommentMap = subComments.stream().collect(Collectors.toMap(CommentDO::getId, commentConverter::toSubDto));

        // 3.构建一级评论的子评论
        subComments.forEach(comment -> {
            TopCommentDTO top = maps.get(comment.getTopCommentId());
            if (top != null) {
                SubCommentDTO sub = subCommentMap.get(comment.getId());
                top.getChildComments().add(sub);
                if (Objects.equals(comment.getTopCommentId(), comment.getParentCommentId())) {
                    return;
                }

                SubCommentDTO parent = subCommentMap.get(comment.getParentCommentId());
                if (parent == null) {
                    sub.setParentContent("~~已删除~~");
                } else {
                    sub.setParentContent(parent.getCommentContent());
                }
            }
        });


        // 4.挑出需要返回的数据，排序，并补齐对应的用户信息，最后排序返回
        List<TopCommentDTO> result = new ArrayList<>();
        comments.forEach(comment -> {
            TopCommentDTO dto = maps.get(comment.getId());
            fillCommentInfo(dto);
            dto.getChildComments().forEach(this::fillCommentInfo);
            Collections.sort(dto.getChildComments());
            result.add(dto);
        });

        // 返回结果根据时间进行排序
        Collections.sort(result);
        return result;
    }

    private void fillCommentInfo(BaseCommentDTO comment) {
        BaseUserInfoDTO userInfoDO = userService.getUserInfoByUserId(comment.getUserId());
        if (userInfoDO == null) {
            // 如果用户注销，给一个默认的用户
            comment.setUserName("默认用户");
            comment.setUserPhoto("");
            if (comment instanceof TopCommentDTO) {
                ((TopCommentDTO) comment).setCommentCount(0);
            }
        } else {
            comment.setUserName(userInfoDO.getUserName());
            comment.setUserPhoto(userInfoDO.getPhoto());
            if (comment instanceof TopCommentDTO) {
                ((TopCommentDTO) comment).setCommentCount(((TopCommentDTO) comment).getChildComments().size());
            }
        }

        // 查询点赞数
        Long praiseCount = userFootService.queryCommentPraiseCount(comment.getCommentId());
        comment.setPraiseCount(praiseCount.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentSaveReq commentSaveReq) {
        // 保存评论
        CommentDO comment;
        if (NumUtil.nullOrZero(commentSaveReq.getCommentId())) {
            comment = addComment(commentSaveReq);
        } else {
            comment = updateComment(commentSaveReq);
        }
        return comment.getId();
    }

    private CommentDO addComment(CommentSaveReq commentSaveReq) {
        // 0.获取父评论信息，校验是否存在
        Long parentCommentUser = getParentCommentUser(commentSaveReq.getParentCommentId());

        // 1. 保存评论内容
        CommentDO commentDO = commentConverter.toDo(commentSaveReq);
        commentDO.setCreateTime(new Date());
        commentDO.setUpdateTime(new Date());
        commentMapper.insert(commentDO);

        // 2. 保存足迹信息 : 文章的已评信息 + 评论的已评信息
        userFootService.saveCommentFoot(commentDO, commentSaveReq.getArticleId(), parentCommentUser);
        return commentDO;
    }

    private CommentDO updateComment(CommentSaveReq commentSaveReq) {
        // 更新评论
        CommentDO commentDO = commentMapper.selectById(commentSaveReq.getCommentId());
        if (commentDO == null) {
            throw new RuntimeException("未查询到该评论");
        }
        commentDO.setContent(commentSaveReq.getCommentContent());
        commentMapper.updateById(commentDO);
        return commentDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) throws Exception {
        CommentDO commentDO = commentMapper.selectById(commentId);
        if (commentDO == null) {
            throw new Exception("未查询到该评论");
        }
        commentDO.setDeleted(YesOrNoEnum.YES.getCode());
        commentMapper.updateById(commentDO);
        userFootService.deleteCommentFoot(commentDO, commentDO.getArticleId(), getParentCommentUser(commentDO.getParentCommentId()));
    }

    private Long getParentCommentUser(Long parentCommentId) {
        Long parentCommentAuthor = null;
        if (NumUtil.upZero(parentCommentId)) {
            CommentDO parent = commentMapper.selectById(parentCommentId);
            if (parent == null) {
                throw new IllegalStateException("父评论不存在!");
            }
            parentCommentAuthor = parent.getUserId();
        }
        return parentCommentAuthor;
    }

    /**
     * 获取评论列表
     *
     * @param pageParam
     * @return
     */
    private List<CommentDO> queryTopCommentList(Long articleId, PageParam pageParam) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CommentDO::getTopCommentId, 0)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(CommentDO::getId);
        return commentMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有的子评论
     *
     * @param articleId
     * @return
     */
    private List<CommentDO> querySubCommentIdMappers(Long articleId, Collection<Long> topCommentIds) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(CommentDO::getTopCommentId, topCommentIds)
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode());
        return commentMapper.selectList(queryWrapper);
    }


    /**
     * 查询有效评论数
     *
     * @param articleId
     * @return
     */
    @Override
    public int commentCount(Long articleId) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getDeleted, YesOrNoEnum.NO.getCode());
        return commentMapper.selectCount(queryWrapper).intValue();
    }
}
