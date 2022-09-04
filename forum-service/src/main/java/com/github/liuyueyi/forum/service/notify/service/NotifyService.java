package com.github.liuyueyi.forum.service.notify.service;

import com.github.liueyueyi.forum.api.model.enums.NotifyTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.notify.dto.NotifyMsgDTO;

import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/3
 */
public interface NotifyService {

    /**
     * 查询用户未读消息数量
     *
     * @param userId
     * @return
     */
    int queryUserNotifyMsgCount(Long userId);

    /**
     * 查询通知列表
     *
     * @param userId
     * @param type
     * @param page
     * @return
     */
    PageListVo<NotifyMsgDTO> queryUserNotices(Long userId, NotifyTypeEnum type, PageParam page);

    /**
     * 查询未读消息数
     * @param userId
     * @return
     */
    Map<String, Integer> queryUnreadCounts(long userId);
}
