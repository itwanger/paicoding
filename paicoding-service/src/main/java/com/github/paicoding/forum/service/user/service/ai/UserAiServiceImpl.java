package com.github.paicoding.forum.service.user.service.ai;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAiStrategyEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.service.user.converter.UserAiConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserAiHistoryDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserAiHistoryDO;
import com.github.paicoding.forum.service.user.service.UserAiService;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
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

        // 星球用户 +100
        if (UserAiStrategyEnum.STAR_JAVA_GUIDE.match(strategy) || UserAiStrategyEnum.STAR_TECH_PAI.match(strategy)) {
            if (Objects.equals(ai.getState(), UserAIStatEnum.FORMAL.getCode())) {
                // 审核通过
                cnt += aiConfig.getMaxNum().getStar();
            } else if (Objects.equals(ai.getState(), UserAIStatEnum.TRYING.getCode())) {
                // 试用中
                cnt += aiConfig.getMaxNum().getStarTry();
            }
        } else {
            // 有星球走星球，无星球再走公众号
            // 微信公众号登录用户 +5次
            if (UserAiStrategyEnum.WECHAT.match(strategy)) {
                cnt += aiConfig.getMaxNum().getWechat();
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

    @Override
    public void initOrUpdateAiInfo(UserPwdLoginReq loginReq) {
        // 之前已经检查过编号是否已经被绑定过了，那我们直接进行绑定
        Long userId = loginReq.getUserId();
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        if (userAiDO == null) {
            // 初始化新的ai信息
            userAiDO = UserAiConverter.initAi(userId, loginReq.getStarNumber());
        } else if (StringUtils.isBlank(loginReq.getStarNumber()) && StringUtils.isBlank(loginReq.getInvitationCode())) {
            // 没有传递星球和邀请码时，直接返回，不用更新ai信息
            return;
        } else if (StringUtils.isNotBlank(loginReq.getStarNumber())) {
            // 之前有绑定信息，检查到与之前的不一致，则执行更新星球编号流程
            if (!Objects.equals(loginReq.getStarNumber(), userAiDO.getStarNumber())) {
                userAiDO.setStarNumber(loginReq.getStarNumber());
            }
            // 并设置为试用
            userAiDO.setState(UserAIStatEnum.TRYING.getCode());
            if (ReqInfoContext.getReqInfo().getUser() != null) {
                ReqInfoContext.getReqInfo().getUser().setStarStatus(UserAIStatEnum.TRYING);
            }
        }
        userAiDao.saveOrUpdateAiBindInfo(userAiDO, loginReq.getInvitationCode());
    }
}
