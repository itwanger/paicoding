package com.github.paicoding.forum.service.notify.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.NotifyStatEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.paicoding.forum.service.notify.repository.entity.NotifyMsgDO;
import com.github.paicoding.forum.service.notify.repository.mapper.NotifyMsgMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Repository
public class NotifyMsgDao extends ServiceImpl<NotifyMsgMapper, NotifyMsgDO> {

    /**
     * 查询消息记录，用于幂等过滤
     *
     * @param msg
     * @return
     */
    public NotifyMsgDO getByUserIdRelatedIdAndType(NotifyMsgDO msg) {
        List<NotifyMsgDO> list = lambdaQuery().eq(NotifyMsgDO::getNotifyUserId, msg.getNotifyUserId())
                .eq(NotifyMsgDO::getOperateUserId, msg.getOperateUserId())
                .eq(NotifyMsgDO::getType, msg.getType())
                .eq(NotifyMsgDO::getRelatedId, msg.getRelatedId())
                .orderByDesc(NotifyMsgDO::getId)
                .page(new Page<>(0, 1))
                .getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }


    /**
     * 查询用户的消息通知数量
     *
     * @param userId
     * @return
     */
    public int countByUserIdAndStat(long userId, Integer stat) {
        return lambdaQuery()
                .eq(NotifyMsgDO::getNotifyUserId, userId)
                .eq(stat != null, NotifyMsgDO::getState, stat)
                .count().intValue();
    }

    /**
     * 查询用户各类型的未读消息数量
     *
     * @param userId
     * @return
     */
    public Map<Integer, Integer> groupCountByUserIdAndStat(long userId, Integer stat) {
        QueryWrapper<NotifyMsgDO> wrapper = new QueryWrapper<>();
        wrapper.select("type, count(*) as cnt");
        wrapper.eq("notify_user_id", userId);
        if (stat != null) {
            wrapper.eq("state", stat);
        }
        wrapper.groupBy("type");
        List<Map<String, Object>> map = listMaps(wrapper);
        Map<Integer, Integer> result = new HashMap<>();
        map.forEach(s -> {
            result.put(Integer.valueOf(s.get("type").toString()), Integer.valueOf(s.get("cnt").toString()));
        });
        return result;
    }

    /**
     * 查询用户消息列表
     *
     * @param userId
     * @param type
     * @return
     */
    public List<NotifyMsgDTO> listNotifyMsgByUserIdAndType(long userId, NotifyTypeEnum type, PageParam page) {
        switch (type) {
            case REPLY:
            case COMMENT:
            case COLLECT:
            case PRAISE:
                return baseMapper.listArticleRelatedNotices(userId, type.getType(), page);
            default:
                return baseMapper.listNormalNotices(userId, type.getType(), page);
        }
    }

    /**
     * 设置消息为已读
     *
     * @param list
     */
    public void updateNotifyMsgToRead(List<NotifyMsgDTO> list) {
        List<Long> ids = list.stream().filter(s -> s.getState() == NotifyStatEnum.UNREAD.getStat()).map(NotifyMsgDTO::getMsgId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            baseMapper.updateNoticeRead(ids);
        }
    }
}
