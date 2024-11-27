package com.github.paicoding.forum.service.chatai.service.impl.ali;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Slf4j
@Service
public class AliAiServiceImpl extends AbsChatService {

    @Autowired
    private AliIntegration aliIntegration;

    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        if (aliIntegration.directReturn(user, chat)) {
            return AiChatStatEnum.END;
        }
        return AiChatStatEnum.ERROR;
    }

    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        aliIntegration.streamReturn(user, chatRes, consumer);
        return AiChatStatEnum.IGNORE;
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.ALI_AI;
    }

    @Override
    public boolean asyncFirst() {
        // true 表示优先使用异步返回； false 表示同步等待结果
        return true;
    }

}