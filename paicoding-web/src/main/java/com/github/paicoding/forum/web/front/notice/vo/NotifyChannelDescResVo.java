package com.github.paicoding.forum.web.front.notice.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 聊天的返回信息
 *
 * @author YiHui
 * @date 2023/12/5
 */
@Data
@Accessors(chain = true)
public class NotifyChannelDescResVo implements Serializable {
    private static final long serialVersionUID = 4487138961031301913L;

    private String channel;

    private String title;
}
