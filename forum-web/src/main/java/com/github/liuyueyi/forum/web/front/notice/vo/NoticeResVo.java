package com.github.liuyueyi.forum.web.front.notice.vo;

import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import lombok.Data;

import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/4
 */
@Data
public class NoticeResVo {
    /**
     * 消息通知列表
     */
    private PageListVo<NotifyMsgDTO> list;

    /**
     * 每个分类的未读数量
     */
    private Map<String, Integer> unreadCountMap;

    /**
     * 当前选中的消息类型
     */
    private String selectType;
}
