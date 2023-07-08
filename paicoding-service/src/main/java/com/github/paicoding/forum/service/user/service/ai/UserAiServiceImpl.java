package com.github.paicoding.forum.service.user.service.ai;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAiStrategyEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserAiHistoryDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserAiHistoryDO;
import com.github.paicoding.forum.service.user.service.UserAiService;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class UserAiServiceImpl implements UserAiService {
    @Resource
    private UserAiHistoryDao userAiHistoryDao;

    @Resource
    private UserAiDao userAiDao;

    @Resource
    private AiConfig aiConfig;

    @Override
    public void pushChatItem(AISourceEnum source, Long user, ChatItemVo item) {
        UserAiHistoryDO userAiHistoryDO = new UserAiHistoryDO();
        userAiHistoryDO.setAiType(source.getCode());
        userAiHistoryDO.setUserId(user);
        userAiHistoryDO.setQuestion(item.getQuestion());
        userAiHistoryDO.setAnswer(item.getAnswer());
        userAiHistoryDao.save(userAiHistoryDO);
    }

    /**
     * 获取用户的最大使用次数
     *
     * @param userId
     * @return
     */
    public int getMaxChatCnt(Long userId) {
        UserAiDO ai = userAiDao.getOrInitAiInfo(userId);
        int strategy = ai.getStrategy();
        int cnt = 0;
        // 微信公众号登录用户 +5次
        if (UserAiStrategyEnum.WECHAT.match(strategy)) {
            cnt += aiConfig.getMaxNum().getWechat();
        }

        // 星球用户 +100
        if (UserAiStrategyEnum.STAR_JAVA_GUIDE.match(strategy) || UserAiStrategyEnum.STAR_TECH_PAI.match(strategy)) {
            if (Objects.equals(ai.getState(), UserAIStatEnum.FORMAL.getCode())) {
                // 审核通过
                cnt += aiConfig.getMaxNum().getStar();
            } else if (Objects.equals(ai.getState(), UserAIStatEnum.TRYING.getCode()) && (System.currentTimeMillis() - ai.getCreateTime().getTime()) <= DateUtil.THREE_DAY_MILL) {
                // 试用中
                cnt += aiConfig.getMaxNum().getStarTry();
            }
        }

        // 推荐机制，如果绑定了邀请码，则总次数 + 10%
        if (UserAiStrategyEnum.INVITE_USER.match(strategy)) {
            cnt = (int) (cnt + cnt * aiConfig.getMaxNum().getInvited());
        }

        // 根据推荐的人数，来进行增加
        if (ai.getInviteNum() > 0) {
            cnt = cnt + ai.getInviteNum() * ((int) (cnt * aiConfig.getMaxNum().getInviteNum()));
        }

        if (cnt == 0) {
            // 对于登录用户，给五次使用机会
            cnt = aiConfig.getMaxNum().getBasic();
        }
        return cnt;
    }

}
