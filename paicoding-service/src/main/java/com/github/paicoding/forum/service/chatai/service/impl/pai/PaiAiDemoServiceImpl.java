package com.github.paicoding.forum.service.chatai.service.impl.pai;

import com.github.paicoding.forum.api.model.enums.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

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
    public boolean doAnswer(String user, ChatItemVo chat) {
        String ans = chat.getQuestion().replace("吗", "");
        ans = StringUtils.replace(ans, "？", "!");
        ans = StringUtils.replace(ans, "?", "!");
        chat.initAnswer(ans);
        return true;
    }

    @Override
    public boolean doAsyncAnswer(String user, ChatRecordsVo response, BiConsumer<Boolean, ChatRecordsVo> consumer) {
        AsyncUtil.execute(() -> {
            AsyncUtil.sleep(1500);
            boolean ans = doAnswer(user, response.getRecords().get(0));
            consumer.accept(ans, response);
        });
        return true;
    }

    @Override
    protected int getMaxQaCnt(String user) {
        return 65535;
    }
}
