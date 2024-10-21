package com.github.paicoding.forum.web.controller.notice.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import lombok.Data;

import java.util.Map;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-07-11
 */
@Data
public class NoticeResultVo {

    /**
     * 消息通知列表
     */
    private IPage<NotifyMsgDTO> list;

    /**
     * 每个分类的未读数量
     */
    private Map<String, Integer> unreadCountMap;

    /**
     * 当前选中的消息类型
     */
    private String selectType;
}
