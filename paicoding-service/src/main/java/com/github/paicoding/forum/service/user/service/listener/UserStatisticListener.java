package com.github.paicoding.forum.service.user.service.listener;

import com.github.paicoding.forum.api.model.enums.ArticleEventEnum;
import com.github.paicoding.forum.api.model.event.ArticleMsgEvent;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import com.github.paicoding.forum.service.user.service.constants.UserConstants;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户活跃相关的消息监听器
 *
 * @author YiHui
 * @date 2023/8/19
 */
@Component
public class UserStatisticListener {
    /**
     * 用户操作行为，增加对应的积分
     *
     * @param msgEvent
     */
    @EventListener(classes = NotifyMsgEvent.class)
    @Async
    public void notifyMsgListener(NotifyMsgEvent msgEvent) {
        switch (msgEvent.getNotifyType()) {
            case COMMENT:
            case REPLY:
                break;
            case COLLECT:
                UserFootDO foot = (UserFootDO) msgEvent.getContent();
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + foot.getDocumentUserId(), UserConstants.COLLECTION_COUNT, 1);
                break;
            case CANCEL_COLLECT:
                foot = (UserFootDO) msgEvent.getContent();
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + foot.getDocumentUserId(), UserConstants.COLLECTION_COUNT, -1);
                break;
            case PRAISE:
                foot = (UserFootDO) msgEvent.getContent();
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + foot.getDocumentUserId(), UserConstants.PRAISE_COUNT, 1);
                break;
            case CANCEL_PRAISE:
                foot = (UserFootDO) msgEvent.getContent();
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + foot.getDocumentUserId(), UserConstants.PRAISE_COUNT, -1);
                break;
            case FOLLOW:
                UserRelationDO relation = (UserRelationDO) msgEvent.getContent();
                // 主用户粉丝数 + 1
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + relation.getUserId(), UserConstants.FANS_COUNT, 1);
                // 粉丝的关注数 + 1
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + relation.getFollowUserId(), UserConstants.FOLLOW_COUNT, 1);
                break;
            case CANCEL_FOLLOW:
                relation = (UserRelationDO) msgEvent.getContent();
                // 主用户粉丝数 + 1
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + relation.getUserId(), UserConstants.FANS_COUNT, -1);
                // 粉丝的关注数 + 1
                RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + relation.getFollowUserId(), UserConstants.FOLLOW_COUNT, -1);
                break;
            default:
        }
    }

    /**
     * 发布文章，更新对应的文章计数
     *
     * @param event
     */
    @Async
    @EventListener(ArticleMsgEvent.class)
    public void publishArticleListener(ArticleMsgEvent<ArticleDO> event) {
        ArticleEventEnum type = event.getType();
        if (type == ArticleEventEnum.ONLINE) {
            RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + event.getContent().getUserId(), UserConstants.ARTICLE_COUNT, 1);
        } else if (type == ArticleEventEnum.OFFLINE) {
            RedisClient.hIncr(UserConstants.USER_STATISTIC_INFO + event.getContent().getUserId(), UserConstants.ARTICLE_COUNT, -1);
        }
    }

}
