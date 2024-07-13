package com.github.paicoding.forum.service.notify.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.NotifyStatEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.notify.repository.dao.NotifyMsgDao;
import com.github.paicoding.forum.service.notify.repository.entity.NotifyMsgDO;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.UserRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author XuYifei
 * @date 2024/07/11
 */
@Service
public class NotifyServiceImpl implements NotifyService {
    @Resource
    private NotifyMsgDao notifyMsgDao;

    @Resource
    private UserRelationService userRelationService;

    {
        System.out.println("####NotifyServiceImpl#####");
    }

    @Override
    public int queryUserNotifyMsgCount(Long userId) {
        return notifyMsgDao.countByUserIdAndStat(userId, NotifyStatEnum.UNREAD.getStat());
    }

    /**
     * 查询消息通知列表
     *
     * @return
     */
    @Override
    public PageListVo<NotifyMsgDTO> queryUserNotices(Long userId, NotifyTypeEnum type, PageParam page) {
        List<NotifyMsgDTO> list = notifyMsgDao.listNotifyMsgByUserIdAndType(userId, type, page);
        if (CollectionUtils.isEmpty(list)) {
            return PageListVo.emptyVo();
        }

        // 设置消息为已读状态
        notifyMsgDao.updateNotifyMsgToRead(list);
        // 更新全局总的消息数
        ReqInfoContext.getReqInfo().setMsgNum(queryUserNotifyMsgCount(userId));
        // 更新当前登录用户对粉丝的关注状态
        updateFollowStatus(userId, list);
        return PageListVo.newVo(list, page.getPageSize());
    }

    @Override
    public IPage<NotifyMsgDTO> queryUserNotices(Long userId, NotifyTypeEnum type, int currentPage, int pageSize) {
        IPage<NotifyMsgDTO> notifyMsgDTOIPage = notifyMsgDao.listNotifyMsgByUserIdAndType(userId, type, currentPage, pageSize);
        if (CollectionUtils.isEmpty(notifyMsgDTOIPage.getRecords())) {
            return notifyMsgDTOIPage;
        }
        // 设置消息为已读状态
        notifyMsgDao.updateNotifyMsgToRead(notifyMsgDTOIPage.getRecords());
        // 更新全局总的消息数
        ReqInfoContext.getReqInfo().setMsgNum(queryUserNotifyMsgCount(userId));
        // 更新当前登录用户对粉丝的关注状态
        updateFollowStatus(userId, notifyMsgDTOIPage.getRecords());

        return notifyMsgDTOIPage;
    }

    private void updateFollowStatus(Long userId, List<NotifyMsgDTO> list) {
        List<Long> targetUserIds = list.stream().filter(s -> s.getType() == NotifyTypeEnum.FOLLOW.getType()).map(NotifyMsgDTO::getOperateUserId).collect(Collectors.toList());
        if (targetUserIds.isEmpty()) {
            return;
        }

        // 查询userId已经关注过的用户列表；并将对应的msg设置为true，表示已经关注过了；不需要再关注
        Set<Long> followedUserIds = userRelationService.getFollowedUserId(targetUserIds, userId);
        list.forEach(notify -> {
            if (followedUserIds.contains(notify.getOperateUserId())) {
                notify.setMsg("true");
            } else {
                notify.setMsg("false");
            }
        });
    }

    @Override
    public Map<String, Integer> queryUnreadCounts(long userId) {
        Map<Integer, Integer> map = Collections.emptyMap();
        if (ReqInfoContext.getReqInfo() != null && NumUtil.upZero(ReqInfoContext.getReqInfo().getMsgNum())) {
            map = notifyMsgDao.groupCountByUserIdAndStat(userId, NotifyStatEnum.UNREAD.getStat());
        }
        // 指定先后顺序
        Map<String, Integer> ans = new LinkedHashMap<>();
        initCnt(NotifyTypeEnum.COMMENT, map, ans);
        initCnt(NotifyTypeEnum.REPLY, map, ans);
        initCnt(NotifyTypeEnum.PRAISE, map, ans);
        initCnt(NotifyTypeEnum.COLLECT, map, ans);
        initCnt(NotifyTypeEnum.FOLLOW, map, ans);
        initCnt(NotifyTypeEnum.SYSTEM, map, ans);
        return ans;
    }

    private void initCnt(NotifyTypeEnum type, Map<Integer, Integer> map, Map<String, Integer> result) {
        result.put(type.name().toLowerCase(), map.getOrDefault(type.getType(), 0));
    }

    @Override
    public void saveArticleNotify(UserFootDO foot, NotifyTypeEnum notifyTypeEnum) {
        NotifyMsgDO msg = new NotifyMsgDO().setRelatedId(foot.getDocumentId())
                .setNotifyUserId(foot.getDocumentUserId())
                .setOperateUserId(foot.getUserId())
                .setType(notifyTypeEnum.getType() )
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg("");
        NotifyMsgDO record = notifyMsgDao.getByUserIdRelatedIdAndType(msg);
        if (record == null) {
            // 若之前已经有对应的通知，则不重复记录；因为一个用户对一篇文章，可以重复的点赞、取消点赞，但是最终我们只通知一次
            notifyMsgDao.save(msg);
        }
    }

}
