package com.github.paicoding.forum.service.notify.service.impl;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.NotifyStatEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.notify.repository.dao.NotifyMsgDao;
import com.github.paicoding.forum.service.notify.repository.entity.NotifyMsgDO;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Slf4j
@Async
@Service
public class NotifyMsgListener<T> implements ApplicationListener<NotifyMsgEvent<T>> {
    private static final Long ADMIN_ID = 1L;
    private final ArticleReadService articleReadService;

    private final CommentReadService commentReadService;

    private final NotifyMsgDao notifyMsgDao;

    private final NotifyService notifyService;

    private final UserService userService;

    public NotifyMsgListener(ArticleReadService articleReadService,
                             CommentReadService commentReadService,
                             NotifyService notifyService,
                             NotifyMsgDao notifyMsgDao,
                             UserService userService) {
        this.articleReadService = articleReadService;
        this.commentReadService = commentReadService;
        this.notifyService = notifyService;
        this.notifyMsgDao = notifyMsgDao;
        this.userService = userService;
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
            case CANCEL_PRAISE:
            case CANCEL_COLLECT:
                removeArticleNotify((NotifyMsgEvent<UserFootDO>) msgEvent);
                break;
            case FOLLOW:
                saveFollowNotify((NotifyMsgEvent<UserRelationDO>) msgEvent);
                break;
            case CANCEL_FOLLOW:
                removeFollowNotify((NotifyMsgEvent<UserRelationDO>) msgEvent);
                break;
            case LOGIN:
                // todo 用户登录，判断是否需要插入新的通知消息，暂时先不做
                break;
            case REGISTER:
                // 首次注册，插入一个欢迎的消息
                saveRegisterSystemNotify((Long) msgEvent.getContent());
                break;
            case PAYING:
            case PAY:
                // 文章支付回调/支付中的消息通知
                savePayNotify((NotifyMsgEvent<ArticlePayRecordDO>) msgEvent);
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
        // 对于评论而言，支持多次评论；因此若之前有也不删除
        notifyMsgDao.save(msg);

        // 消息通知
        notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.COMMENT,
                String.format("您的文章《%s》收到一个新的评论，快去看看吧", article.getTitle()));
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
                .setCommentId(comment.getId())
                .setType(event.getNotifyType().getType())
                .setState(NotifyStatEnum.UNREAD.getStat()).setMsg(comment.getContent());
        // 回复同样支持多次回复,不做幂等校验
        notifyMsgDao.save(msg);

        // 消息通知
        notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.REPLY,
                String.format("您的评价《%s》收到一个新的回复，快去看看吧", parent.getContent()));
    }

    /**
     * 点赞 + 收藏
     *
     * @param event
     */
    private void saveArticleNotify(NotifyMsgEvent<UserFootDO> event) {
        UserFootDO foot = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(foot.getDocumentId())
                .setNotifyUserId(foot.getDocumentUserId())
                .setOperateUserId(foot.getUserId())
                .setType(event.getNotifyType().getType())
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg("");
        if (Objects.equals(foot.getDocumentType(), DocumentTypeEnum.COMMENT.getCode())) {
            // 点赞评论时，详情内容中显示评论信息
            CommentDO comment = commentReadService.queryComment(foot.getDocumentId());
            ArticleDO article = articleReadService.queryBasicArticle(comment.getArticleId());
            msg.setMsg(String.format("赞了您在文章 <a href=\"/article/detail/%d\">%s</a> 下的评论 <span style=\"color:darkslategray;font-style: italic;font-size: 0.9em\">%s</span>", article.getId(), article.getTitle(), comment.getContent()));
        }

        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record == null) {
            // 若之前已经有对应的通知，则不重复记录；因为一个用户对一篇文章，可以重复的点赞、取消点赞，但是最终我们只通知一次
            notifyMsgDao.save(msg);
            // 消息通知
            notifyService.notifyToUser(msg.getNotifyUserId(), event.getNotifyType(),
                    String.format("太棒了，您的%s %s数+1!!!",
                            Objects.equals(foot.getDocumentType(), DocumentTypeEnum.ARTICLE.getCode()) ? "文章" : "评论",
                            event.getNotifyType().getMsg()));
        }
    }

    public void saveArticleNotify(UserFootDO foot, NotifyTypeEnum notifyTypeEnum) {
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(foot.getDocumentId())
                .setNotifyUserId(foot.getDocumentUserId())
                .setOperateUserId(foot.getUserId())
                .setType(notifyTypeEnum.getType())
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg("");
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record == null) {
            // 若之前已经有对应的通知，则不重复记录；因为一个用户对一篇文章，可以重复的点赞、取消点赞，但是最终我们只通知一次
            notifyMsgDao.save(msg);
        }
    }

    /**
     * 取消点赞，取消收藏
     *
     * @param event
     */
    private void removeArticleNotify(NotifyMsgEvent<UserFootDO> event) {
        UserFootDO foot = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO()
                .setRelatedId(foot.getDocumentId())
                .setNotifyUserId(foot.getDocumentUserId())
                .setOperateUserId(foot.getUserId())
                .setType(event.getNotifyType().getType())
                .setMsg("");
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record != null) {
            notifyMsgDao.removeById(record.getId());
        }
    }

    /**
     * 关注
     *
     * @param event
     */
    private void saveFollowNotify(NotifyMsgEvent<UserRelationDO> event) {
        UserRelationDO relation = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(0L)
                .setNotifyUserId(relation.getUserId())
                .setOperateUserId(relation.getFollowUserId())
                .setType(event.getNotifyType().getType())
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg("");
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record == null) {
            // 若之前已经有对应的通知，则不重复记录；因为用户的关注是一对一的，可以重复的关注、取消，但是最终我们只通知一次
            notifyMsgDao.save(msg);

            notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.FOLLOW, "恭喜您获得一枚新粉丝~");
        }
    }

    /**
     * 取消关注
     *
     * @param event
     */
    private void removeFollowNotify(NotifyMsgEvent<UserRelationDO> event) {
        UserRelationDO relation = event.getContent();
        NotifyMsgDO msg = new NotifyMsgDO()
                .setRelatedId(0L)
                .setNotifyUserId(relation.getUserId())
                .setOperateUserId(relation.getFollowUserId())
                .setType(event.getNotifyType().getType())
                .setMsg("");
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record != null) {
            notifyMsgDao.removeById(record.getId());
        }
    }

    private void saveRegisterSystemNotify(Long userId) {
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(0L)
                .setNotifyUserId(userId)
                .setOperateUserId(ADMIN_ID)
                .setType(NotifyTypeEnum.REGISTER.getType())
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg(SpringUtil.getConfig("view.site.welcomeInfo"));
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record == null) {
            // 若之前已经有对应的通知，则不重复记录；因为用户的关注是一对一的，可以重复的关注、取消，但是最终我们只通知一次
            notifyMsgDao.save(msg);

            notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.SYSTEM, "您有一个新的系统通知消息，请注意查收");
        }
    }

    private void savePayNotify(NotifyMsgEvent<ArticlePayRecordDO> pay) {
        ArticlePayRecordDO record = pay.getContent();
        ArticleDO article = articleReadService.queryBasicArticle(record.getArticleId());

        NotifyMsgDO msg;
        PayStatusEnum payStatus = PayStatusEnum.statusOf(record.getPayStatus());
        if (PayStatusEnum.PAYING == payStatus) {
            // 支付中，给作者发起一个通知
            BaseUserInfoDTO payUser = userService.queryBasicUserInfo(record.getPayUserId());

            msg = new NotifyMsgDO().setRelatedId(record.getArticleId())
                    .setNotifyUserId(record.getReceiveUserId())
                    .setOperateUserId(record.getPayUserId())
                    .setType(NotifyTypeEnum.PAY.getType())
                    .setState(NotifyStatEnum.UNREAD.getStat())
                    .setMsg(String.format("您的文章 <a href=\"/article/detail/%d\">%s</a> 收到一份来自 <a href=\"/user/home?userId=%d\">%s</a> 的 [%s] 打赏，点击 <a href=\"/article/payConfirm?payId=%d\">去确认~</a>",
                            record.getArticleId(), article.getTitle(),
                            payUser.getUserId(), payUser.getUserName(),
                            StringUtils.isBlank(record.getPayWay()) || Objects.equals(record.getPayWay(), ThirdPayWayEnum.EMAIL.getPay()) ? "个人收款码" : "微信支付",
                            record.getId()));
        } else {
            // 作者执行的支付结果回调通知付费用户
            msg = new NotifyMsgDO().setRelatedId(record.getArticleId())
                    .setNotifyUserId(record.getPayUserId())
                    .setOperateUserId(record.getReceiveUserId())
                    .setType(NotifyTypeEnum.PAY.getType())
                    .setState(NotifyStatEnum.UNREAD.getStat())
                    .setMsg(
                            PayStatusEnum.SUCCEED == payStatus
                                    ? String.format("您对 <a href=\"/article/detail/%d\">%s</a> 的支付已完成~", record.getArticleId(), article.getTitle())
                                    : String.format("您对 <a href=\"/article/detail/%d\">%s</a> 的支付未完成哦~", record.getArticleId(), article.getTitle())
                    );
        }

        NotifyMsgDO dbMsg = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (dbMsg == null) {
            // 未通知过，则新增一条通知记录
            notifyMsgDao.save(msg);
        } else if (!Objects.equals(dbMsg.getMsg(), msg.getMsg())) {
            // 由于可能出现第一次支付失败，然后第二次支付成功的场景，因此我们需要再新增一个消息通知
            notifyMsgDao.save(msg);
        } else if (payStatus == PayStatusEnum.PAYING && Objects.equals(dbMsg.getState(), NotifyStatEnum.UNREAD.getStat())) {
            // 根据作者是否看过通知，来决定是否需要重新给作者发送一个消息通知
            notifyMsgDao.save(msg);
        }

        if (payStatus == PayStatusEnum.PAYING) {
            // 支付中
            notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.SYSTEM,
                    String.format("您的文章《%s》收到一份打赏，请及时确认~", article.getTitle()));
        } else if (payStatus == PayStatusEnum.SUCCEED) {
            // 支付成功
            notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.SYSTEM,
                    String.format("您对文章《%s》的支付已完成，刷新即可阅读全文哦~", article.getTitle()));
        } else if (payStatus == PayStatusEnum.FAIL) {
            // 支付失败
            notifyService.notifyToUser(msg.getNotifyUserId(), NotifyTypeEnum.SYSTEM,
                    String.format("您对文章《%s》的支付未成功，请重试一下吧~", article.getTitle()));
        }
    }
}
