package com.github.liuyueyi.forum.service.notify.service.impl;

import com.github.liueyueyi.forum.api.model.enums.NotifyStatEnum;
import com.github.liueyueyi.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.service.ArticleReadService;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.comment.service.CommentReadService;
import com.github.liuyueyi.forum.service.notify.repository.dao.NotifyMsgDao;
import com.github.liuyueyi.forum.service.notify.repository.entity.NotifyMsgDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Async
@Service
public class NotifyMsgListener<T> implements ApplicationListener<NotifyMsgEvent<T>> {
    private final ArticleReadService articleReadService;

    private final CommentReadService commentReadService;

    private final NotifyMsgDao notifyMsgDao;

    public NotifyMsgListener(ArticleReadService articleReadService,
                             CommentReadService commentReadService,
                             NotifyMsgDao notifyMsgDao) {
        this.articleReadService = articleReadService;
        this.commentReadService = commentReadService;
        this.notifyMsgDao = notifyMsgDao;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(NotifyMsgEvent<T> msgEvent) {
        switch (msgEvent.getNotifyType()) {
            case COMMENT:
                saveCommentNotify((NotifyMsgEvent<CommentDO>) msgEvent);
                break;
            case REPLY:
                saveReplyNotify((NotifyMsgEvent<CommentDO>) msgEvent);
                break;
            case PRAISE:
            case COLLECT:
                saveArticleNotify((NotifyMsgEvent<UserFootDO>) msgEvent);
                break;
            case FOLLOW:
                saveFollowNotify((NotifyMsgEvent<UserRelationDO>) msgEvent);
                break;
            default:
                // todo 系统消息
        }
    }

    /**
     * 评论 + 回复
     *
     * @param event
     */
    private void saveCommentNotify(NotifyMsgEvent<CommentDO> event) {
        NotifyMsgDO msg = new NotifyMsgDO();
        CommentDO comment = event.getContent();
        ArticleDO article = articleReadService.queryBasicArticle(comment.getArticleId());
        msg.setNotifyUserId(article.getUserId())
                .setOperateUserId(comment.getUserId())
                .setRelatedId(article.getId())
                .setType(event.getNotifyType().getType())
                .setState(NotifyStatEnum.UNREAD.getStat()).setMsg(comment.getContent());
        notifyMsgDao.save(msg);
    }

    /**
     * 评论回复消息
     *
     * @param event
     */
    private void saveReplyNotify(NotifyMsgEvent<CommentDO> event) {
        NotifyMsgDO msg = new NotifyMsgDO();
        CommentDO comment = event.getContent();
        CommentDO parent = commentReadService.queryComment(comment.getParentCommentId());
        msg.setNotifyUserId(parent.getUserId())
                .setOperateUserId(comment.getUserId())
                .setRelatedId(comment.getArticleId())
                .setType(event.getNotifyType().getType())
                .setState(NotifyStatEnum.UNREAD.getStat()).setMsg(comment.getContent());
        notifyMsgDao.save(msg);
    }

    /**
     * 点赞 + 收藏
     *
     * @param event
     */
    private void saveArticleNotify(NotifyMsgEvent<UserFootDO> event) {
        UserFootDO foot = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(foot.getDocumentId()).setNotifyUserId(foot.getDocumentUserId()).setOperateUserId(foot.getUserId()).setType(event.getNotifyType().getType()).setState(NotifyStatEnum.UNREAD.getStat()).setMsg("");
        notifyMsgDao.save(msg);
    }

    /**
     * 关注
     *
     * @param event
     */
    private void saveFollowNotify(NotifyMsgEvent<UserRelationDO> event) {
        UserRelationDO relation = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(0L).setNotifyUserId(relation.getUserId()).setOperateUserId(relation.getFollowUserId()).setType(event.getNotifyType().getType()).setState(NotifyStatEnum.UNREAD.getStat()).setMsg("");
        notifyMsgDao.save(msg);
    }

}
