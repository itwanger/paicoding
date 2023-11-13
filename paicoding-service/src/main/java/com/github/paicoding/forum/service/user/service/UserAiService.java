package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;

public interface UserAiService {
    /**
     * 保存聊天历史记录
     *
     * @param source
     * @param user
     * @param item
     */
    void pushChatItem(AISourceEnum source, Long user, ChatItemVo item);

    /**
     * 获取用户的最大聊天次数
     *
     * @param userId
     * @return
     */
    int getMaxChatCnt(Long userId);

    /**
     * 重建用户绑定的星球编号
     *
     * @param loginReq
     */

    void initOrUpdateAiInfo(UserPwdLoginReq loginReq);
}
