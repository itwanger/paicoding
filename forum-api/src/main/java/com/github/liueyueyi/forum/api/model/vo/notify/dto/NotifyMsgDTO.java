package com.github.liueyueyi.forum.api.model.vo.notify.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author YiHui
 * @date 2022/9/4
 */
@Data
public class NotifyMsgDTO implements Serializable {
    private static final long serialVersionUID = 3833777672628522348L;

    private Long msgId;

    private String relatedId;

    private String relatedInfo;

    private Long operateUserId;

    private String operateUserName;

    private String operateUserPhoto;

    /**
     * 消息类型
     */
    private Integer type;

    private String msg;

    /**
     * 1 已读/ 0 未读
     */
    private Integer state;

    private Timestamp createTime;
}
