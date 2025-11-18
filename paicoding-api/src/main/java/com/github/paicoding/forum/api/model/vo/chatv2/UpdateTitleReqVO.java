package com.github.paicoding.forum.api.model.vo.chatv2;

import lombok.Data;

/**
 * 更新标题请求
 *
 * @author XuYifei
 * @date 2025-11-16
 */
@Data
public class UpdateTitleReqVO {

    /**
     * 会话ID
     */
    private Long historyId;

    /**
     * 新标题
     */
    private String title;
}
