package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.user.StarSourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAiStrategyEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.ZsxqUserInfoDTO;
import com.github.paicoding.forum.service.user.converter.UserAiConverter;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiMapper;
import com.github.paicoding.forum.service.user.repository.params.SearchZsxqWhiteParams;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class UserAiDao extends ServiceImpl<UserAiMapper, UserAiDO> {

    @Resource
    private UserAiMapper userAiMapper;

    @Resource
    private UserDao userDao;

    /**
     * 根据星球编号反查用户
     *
     * @param starNumber
     * @return
     */
    public UserAiDO getByStarNumber(String starNumber) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getStarNumber, starNumber)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last("limit 1");
        return userAiMapper.selectOne(queryUserAi);
    }

    public UserAiDO getByUserId(Long userId) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getUserId, userId)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userAiMapper.selectOne(queryUserAi);
    }


    /**
     * 查询用户的ai信息，若不存在，则初始化一个，主要用于存量的账号已存在的场景
     *
     * @param userId
     */
    public UserAiDO getOrInitAiInfo(Long userId) {
        UserAiDO ai = getByUserId(userId);
        if (ai != null) {
            return ai;
        }

        // 当不存在时，初始化一个
        ai = UserAiConverter.initAi(userId);
        saveOrUpdateAiBindInfo(ai, null);
        return ai;
    }

    /**
     * 根据邀请码，查找对应的邀请人
     *
     * @param inviteCode 邀请码
     * @return
     */
    public UserAiDO getByInviteCode(String inviteCode) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getInviteCode, inviteCode)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userAiMapper.selectOne(queryUserAi);
    }

    /**
     * 更新用户的邀请人数
     *
     * @param id
     * @param incr
     */
    private void updateInviteCnt(Long id, int incr) {
        LambdaUpdateWrapper<UserAiDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAiDO::getId, id).setSql("invite_num = invite_num + " + incr);
        userAiMapper.update(null, updateWrapper);
    }

    public void saveOrUpdateAiBindInfo(UserAiDO ai) {
        saveOrUpdateAiBindInfo(ai, null);
    }

    /**
     * 更新userAi绑定信息
     *
     * @param ai
     * @param inviteCode
     */
    public void saveOrUpdateAiBindInfo(UserAiDO ai, String inviteCode) {
        int strategy = ai.getStrategy();
        if (StringUtils.isNotBlank(inviteCode)) {
            // todo 待支持更新邀请绑定
            // 对于绑定邀请码的用户，需要将邀请他的用户找出来，计数 + 1
            UserAiDO inviteUser = getByInviteCode(inviteCode);
            if (inviteUser != null) {
                ai.setInviterUserId(inviteUser.getUserId());
                updateInviteCnt(inviteUser.getId(), 1);
                strategy = UserAiStrategyEnum.INVITE_USER.updateCondition(strategy);
            }
        }

        // 这里有点问题
        // 用户名密码注册的时候，还没有审核通过，所以即使有星球编号，也无法绑定 AI 策略
        // 去掉用户审核通过的判断，如果用户绑定了星球，就直接更新策略，默认为进阶之路
        // 后面获取册数的时候会根据用户的审核状态，计算次数
        if (StringUtils.isNotBlank(ai.getStarNumber())) {
            // 绑定了星球，且审核通过
            if (ai.getStarType() == StarSourceEnum.TECH_PAI.getSource()) {
                strategy = UserAiStrategyEnum.STAR_TECH_PAI.updateCondition(strategy);
            } else {
                strategy = UserAiStrategyEnum.STAR_JAVA_GUIDE.updateCondition(strategy);
            }
        } else {
            // 有星球编号就直接走上面的判断，不走这里公众号的判断了
            // 如果绑定了微信公众号
            UserDO user = userDao.getUserByUserId(ai.getUserId());
            if (StringUtils.isNotBlank(user.getThirdAccountId())) {
                strategy = UserAiStrategyEnum.WECHAT.updateCondition(strategy);
            }
        }

        ai.setStrategy(strategy);
        this.saveOrUpdate(ai);
    }

    public List<ZsxqUserInfoDTO> listZsxqUsersByParams(SearchZsxqWhiteParams params) {
        return userAiMapper.listZsxqUsersByParams(params,
                PageParam.newPageInstance(params.getPageNum(), params.getPageSize()));
    }

    public Long countZsxqUserByParams(SearchZsxqWhiteParams params) {
        return userAiMapper.countZsxqUsersByParams(params);
    }

    public void batchUpdateState(List<Long> ids, Integer code) {
        LambdaUpdateWrapper<UserAiDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(UserAiDO::getId, ids).set(UserAiDO::getState, code);
        userAiMapper.update(null, updateWrapper);
    }
}
