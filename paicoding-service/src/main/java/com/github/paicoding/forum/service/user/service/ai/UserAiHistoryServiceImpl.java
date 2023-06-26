package com.github.paicoding.forum.service.user.service.ai;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.service.user.repository.dao.UserAiHistoryDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiHistoryDO;
import com.github.paicoding.forum.service.user.service.UserAiHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserAiHistoryServiceImpl implements UserAiHistoryService {
    @Resource
    private UserAiHistoryDao userAiHistoryDao;
    @Override
    public void pushChatItem(AISourceEnum source, Long user, ChatItemVo item) {
        UserAiHistoryDO userAiHistoryDO = new UserAiHistoryDO();
        userAiHistoryDO.setSource(source.ordinal());
        userAiHistoryDO.setUserId(user);
        userAiHistoryDO.setQa(item.getQuestion());
        userAiHistoryDO.setAnswer(item.getAnswer());
        userAiHistoryDao.save(userAiHistoryDO);
    }
}
