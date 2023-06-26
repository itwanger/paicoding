package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.user.StarSourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAiConditionEnum;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Repository
public class UserAiDao extends ServiceImpl<UserAiMapper, UserAiDO> {

    @Resource
    private UserAiMapper userAiMapper;

    @Resource
    private UserDao userDao;

    public UserAiDO getByStarNumber(String starNumber) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getStarNumber, starNumber)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userAiMapper.selectOne(queryUserAi);
    }

    public UserAiDO getByUserId(Long userId) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getUserId, userId)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userAiMapper.selectOne(queryUserAi);
    }

    public UserAiDO getByInviteCode(String inviteCode) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getInviteCode, inviteCode)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userAiMapper.selectOne(queryUserAi);
    }

    private void updateInviteCnt(Long id, int incr) {
        LambdaUpdateWrapper<UserAiDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAiDO::getId, id).setSql("invite_num = invite_num + " + incr);
        userAiMapper.update(null, updateWrapper);
    }

    /**
     * 更新userAi绑定信息
     *
     * @param ai
     * @param inviteCode
     */
    public void saveOrUpdateAiBindInfo(UserAiDO ai, String inviteCode) {
        int condition = ai.getCondition();
        if (StringUtils.isNotBlank(inviteCode)) {
            // 用户添加了邀请码，需要将邀请他的用户找出来，计数 + 1
            UserAiDO inviteUser = getByInviteCode(inviteCode);
            if (inviteUser != null) {
                ai.setInviterUserId(inviteUser.getUserId());
                updateInviteCnt(inviteUser.getId(), 1);
            }
            condition = UserAiConditionEnum.INVITE_USER.updateCondition(condition);
        }


        if (StringUtils.isNotBlank(ai.getStarNumber()) && Objects.equals(ai.getState(), UserAIStatEnum.FORMAL.getCode())) {
            // 绑定了星球，且审核通过
            if (ai.getStarType() == StarSourceEnum.JAVA_GUIDE.getSource()) {
                condition = UserAiConditionEnum.STAR_JAVA_GUIDE.updateCondition(condition);
            } else {
                condition = UserAiConditionEnum.STAR_TECH_PAI.updateCondition(condition);
            }
        }

        // 如果绑定了微信公众号
        UserDO user = userDao.getUserByUserId(ai.getUserId());
        if (StringUtils.isNotBlank(user.getThirdAccountId())) {
            condition = UserAiConditionEnum.WECHAT.updateCondition(condition);
        }

        ai.setCondition(condition);
        this.saveOrUpdate(ai);
    }
}
