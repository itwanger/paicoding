package com.github.paicoding.forum.service.chatai.service.impl.doubao;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import com.volcengine.ApiException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class DoubaoAiServiceImpl extends AbsChatService {
    @Autowired
    private DoubaoIntegration doubaoIntegration;


    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        return doubaoIntegration.directAnswer(user, chat);
    }

    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo chatRes, BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        return doubaoIntegration.streamAsyncAnswer(user, chatRes, consumer);
    }

    @Override
    public AISourceEnum source() {
        return AISourceEnum.DOU_BAO_AI;
    }

    @Override
    public boolean asyncFirst() {
        return true;
    }


}