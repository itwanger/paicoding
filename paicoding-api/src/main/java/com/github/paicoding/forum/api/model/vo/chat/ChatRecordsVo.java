package com.github.paicoding.forum.api.model.vo.chat;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 聊天记录
 *
 * @author YiHui
 * @date 2023/6/9
 */
@Data
@Accessors(chain = true)
public class ChatRecordsVo implements Serializable {
    private static final long serialVersionUID = -2666259615985932920L;
    /**
     * AI来源
     */
    private AISourceEnum source;

    /**
     * 当前用户最多可问答的次数
     */
    private int maxCnt;

    /**
     * 剩余可用的次数
     */
    private int avaliableCnt;

    /**
     * 聊天记录，最新的在前面；最多返回50条
     */
    private List<ChatItemVo> records;
}
