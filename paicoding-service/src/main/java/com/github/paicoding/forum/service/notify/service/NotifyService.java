package com.github.paicoding.forum.service.notify.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;

import java.util.Map;

/**
 * @author XuYifei
 * @date 2024-07-12
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
     * 分页查询通知列表
     *
     * @param userId
     * @param type
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<NotifyMsgDTO> queryUserNotices(Long userId, NotifyTypeEnum type, int currentPage, int pageSize);

    /**
     * 查询未读消息数
     * @param userId
     * @return
     */
    Map<String, Integer> queryUnreadCounts(long userId);

    /**
     * 保存通知
     *
     * @param foot
     * @param notifyTypeEnum
     */
    void saveArticleNotify(UserFootDO foot, NotifyTypeEnum notifyTypeEnum);
}
