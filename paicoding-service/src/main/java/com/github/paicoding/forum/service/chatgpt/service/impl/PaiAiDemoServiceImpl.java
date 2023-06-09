package com.github.paicoding.forum.service.chatgpt.service.impl;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.service.chatgpt.service.AbsChatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author YiHui
 * @date 2023/6/9
 */
@Service
public class PaiAiDemoServiceImpl extends AbsChatService {
    @Override
    public AISourceEnum source() {
        return AISourceEnum.PAI_AI;
    }

    @Override
    public boolean answer(String user, ChatItemVo chat) {
        String ans = chat.getQuestion().replace("吗", "");
        ans = StringUtils.replace(ans, "？", "!");
        ans = StringUtils.replace(ans, "?", "!");
        chat.initAnswer(ans);
        return true;
    }

    @Override
    protected int getMaxQaCnt(String user) {
        return 65535;
    }
}
