package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;

public interface UserAiHistoryService {
    public void pushChatItem(AISourceEnum source, Long user, ChatItemVo item);
}
