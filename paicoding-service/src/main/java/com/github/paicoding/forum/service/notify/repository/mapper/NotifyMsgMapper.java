package com.github.paicoding.forum.service.notify.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.paicoding.forum.service.notify.repository.entity.NotifyMsgDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public interface NotifyMsgMapper extends BaseMapper<NotifyMsgDO> {

    /**
     * 查询文章相关的通知列表
     *
     * @param userId
     * @param type
     * @param page   分页
     * @return
     */
    List<NotifyMsgDTO> listArticleRelatedNotices(@Param("userId") long userId, @Param("type") int type, @Param("pageParam") PageParam page);

    /**
     * 查询关注、系统等没有关联id的通知列表
     *
     * @param userId
     * @param type
     * @param page   分页
     * @return
     */
    List<NotifyMsgDTO> listNormalNotices(@Param("userId") long userId, @Param("type") int type, @Param("pageParam") PageParam page);

    /**
     * 标记消息为已阅读
     *
     * @param ids
     */
    void updateNoticeRead(@Param("ids") List<Long> ids);

    /**
     * 分页查询文章相关的通知列表
     *
     * @param userId
     * @param type
     * @param page   分页
     * @return
     */
    IPage<NotifyMsgDTO> listArticleRelatedNoticesPagination(long userId, int type, Page<NotifyMsgDTO> page);

    IPage<NotifyMsgDTO> listNormalNoticesPagination(long userId, int type, Page<NotifyMsgDTO> page);
}
