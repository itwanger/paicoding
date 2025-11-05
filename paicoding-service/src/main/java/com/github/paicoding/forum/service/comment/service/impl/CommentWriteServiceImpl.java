package com.github.paicoding.forum.service.comment.service.impl;

import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.dto.HighlightDto;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.comment.converter.CommentConverter;
import com.github.paicoding.forum.service.comment.repository.dao.CommentDao;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 评论Service
 *
 * @author louzai
 * @date 2022-07-24
 */
@Slf4j
@Service
public class CommentWriteServiceImpl implements CommentWriteService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private UserFootService userFootWriteService;
    @Autowired
    private AiBots aiBots;

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
        CommentDO parentComment = getParentCommentUser(commentSaveReq.getParentCommentId());
        Long parentUser = parentComment == null ? null : parentComment.getUserId();

        // 1. 保存评论内容
        CommentDO commentDO = CommentConverter.toDo(commentSaveReq);
        Date now = new Date();
        commentDO.setCreateTime(now);
        commentDO.setUpdateTime(now);
        commentDao.save(commentDO);

        // 2. 保存足迹信息 : 文章的已评信息 + 评论的已评信息
        ArticleDO article = articleReadService.queryBasicArticle(commentSaveReq.getArticleId());
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, commentSaveReq.getArticleId());
        }
        userFootWriteService.saveCommentFoot(commentDO, article.getUserId(), parentUser);

        // 3. 触发杠精机器人
        this.aiBotTrigger(commentDO, parentComment);

        // 4. 发布添加/回复评论事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.COMMENT, commentDO));
        if (NumUtil.upZero(parentUser)) {
            // 评论回复事件
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.REPLY, commentDO));
        }
        return commentDO;
    }

    private CommentDO updateComment(CommentSaveReq commentSaveReq) {
        // 更新评论
        CommentDO commentDO = commentDao.getById(commentSaveReq.getCommentId());
        if (commentDO == null) {
            throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, commentSaveReq.getCommentId());
        }
        commentDO.setContent(commentSaveReq.getCommentContent());
        commentDao.updateById(commentDO);
        return commentDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        CommentDO commentDO = commentDao.getById(commentId);
        // 1.校验评论，是否越权，文章是否存在
        if (commentDO == null) {
            throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "评论ID=" + commentId);
        }
        if (!Objects.equals(commentDO.getUserId(), userId)) {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "无权删除评论");
        }
        // 获取文章信息
        ArticleDO article = articleReadService.queryBasicArticle(commentDO.getArticleId());
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, commentDO.getArticleId());
        }

        // 2.删除评论、足迹
        commentDO.setDeleted(YesOrNoEnum.YES.getCode());
        commentDao.updateById(commentDO);
        CommentDO parentComment = getParentCommentUser(commentDO.getParentCommentId());
        userFootWriteService.removeCommentFoot(commentDO, article.getUserId(), parentComment == null ? null : parentComment.getUserId());

        // 3. 发布删除评论事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.DELETE_COMMENT, commentDO));
        if (NumUtil.upZero(commentDO.getParentCommentId())) {
            // 评论
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.DELETE_REPLY, commentDO));
        }
    }


    private CommentDO getParentCommentUser(Long parentCommentId) {
        if (NumUtil.nullOrZero(parentCommentId)) {
            return null;

        }
        CommentDO parent = commentDao.getById(parentCommentId);
        if (parent == null) {
            throw ExceptionUtil.of(StatusEnum.COMMENT_NOT_EXISTS, "父评论=" + parentCommentId);
        }
        return parent;
    }


    /**
     * 机器人回复
     *
     * @param comment 当前评论内容
     * @param parent  当前评论的父评论
     */
    private void aiBotTrigger(CommentDO comment, CommentDO parent) {
        boolean trigger = false;
        Long topCommentId = 0L;
        AiBotEnum botEnum = null;
        if (parent == null) {
            // 当前的评论就是顶级评论，根据回复内容是否有触发词来决定是否需要进行触发
            botEnum = aiBots.triggerAiBotKeyWord(comment.getContent());
            if (botEnum != null) {
                String tag = "@" + botEnum.getNickName();
                comment.setContent(StringUtils.replace(comment.getContent(), tag, ""));
                trigger = true;
            }
            topCommentId = comment.getId();
        } else {
            botEnum = aiBots.getAiBotByUserId(parent.getUserId());
            // 回复内容，根据回复的用户是否为机器人，来判定是否需要进行触发
            if (botEnum != null) {
                trigger = true;
            }
            topCommentId = comment.getTopCommentId();
        }

        // 评论中，@了机器人，那么开启评论对线模式
        if (trigger) {
            log.info("评论「{}」 开启了AI机器人:{}", comment, botEnum);
            // sourceBizId: 主要用于构建聊天对话，以顶级评论 + 用户id作为唯一标识
            // 避免出现一个顶级评论开启对线，后续的回复中有其他用户参与进来时，因为用户id不同，这样传递给大模型的上下文就不会出现交叉
            AiBotEnum finalBotEnum = botEnum;
            aiBots.trigger(botEnum, initQAUserPrompt(botEnum, comment)
                    , "comment:" + topCommentId + "_" + comment.getUserId()
                    , reply -> aiReply(finalBotEnum, reply, comment)
                    , initQABotSystemPrompt(botEnum, comment));
            log.info("任务已完成提交~");
        }
    }


    private String initQAUserPrompt(AiBotEnum bot, CommentDO comment) {
        if (bot == AiBotEnum.QA_BOT) {
            String prefix = "";
            if (StringUtils.isNotBlank(comment.getHighlightInfo())) {
                HighlightDto highlightDto = JsonUtil.toObj(comment.getHighlightInfo(), HighlightDto.class);
                if (StringUtils.isNotBlank(highlightDto.getSelectedText())) {
                    prefix = "这是我从参考资料中选择的一段文本：\"" + highlightDto.getSelectedText() + "\"\n";
                }
            }

            return prefix + comment.getContent();
        } else {
            return comment.getContent();
        }
    }

    /**
     * 初始化AI机器人的系统提示词
     *
     * @param bot     机器人枚举
     * @param comment 评论
     * @return 系统提示词
     */
    private Supplier<String> initQABotSystemPrompt(AiBotEnum bot, CommentDO comment) {
        if (bot == AiBotEnum.QA_BOT) {
            String article = articleReadService.queryArticleContentForAI(comment.getArticleId());
            return () -> bot.getPrompt() + "\n\n" + article;
        } else {
            return bot::getPrompt;
        }
    }

    private void aiReply(AiBotEnum aiBot, String replyContent, CommentDO parentComment) {
        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(parentComment.getArticleId());
        save.setCommentContent(replyContent);
        save.setUserId(aiBots.getBotUser(aiBot).getUserId());
        save.setParentCommentId(parentComment.getId());
        save.setTopCommentId(NumUtil.upZero(parentComment.getTopCommentId()) ? parentComment.getTopCommentId() : parentComment.getId());
        SpringUtil.getBean(CommentWriteService.class).saveComment(save);
    }
}
