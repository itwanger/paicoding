package com.github.paicoding.forum.service.user.service.user;

import cn.hutool.core.date.DateUtil;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.UserAIStatEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.service.user.converter.UserConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.IpInfo;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.service.SessionService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.help.StarNumberHelper;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
@Slf4j
public class SessionServiceImpl implements SessionService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;


    @Autowired
    private UserSessionHelper userSessionHelper;
    @Autowired
    private StarNumberHelper starNumberHelper;

    @Override
    public String autoRegisterAndGetVerifyCode(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        userService.registerOrGetUserInfo(req);
        return userSessionHelper.genVerifyCode(req.getUserId());
    }

    @Override
    public String login(String code) {
        Long userId = userSessionHelper.getUserIdByCode(code);
        if (userId == null) {
            return null;
        }
        return userSessionHelper.codeVerifySucceed(code, userId);
    }

    @Override
    public String login(Long userId) {
        return userSessionHelper.codeVerifySucceed("", userId);
    }

    @Override
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }


    @Override
    public BaseUserInfoDTO getAndUpdateUserIpInfoBySessionId(String session, String clientIp) {
        if (StringUtils.isBlank(session)) {
            return null;
        }

        Long userId = userSessionHelper.getUserIdBySession(session);
        if (userId == null) {
            return null;
        }

        // 查询用户信息，并更新最后一次使用的ip
        UserInfoDO user = userDao.getByUserId(userId);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userId);
        }

        IpInfo ip = user.getIp();
        if (clientIp != null && !Objects.equals(ip.getLatestIp(), clientIp)) {
            // ip不同，需要更新
            ip.setLatestIp(clientIp);
            ip.setLatestRegion(IpUtil.getLocationByIp(clientIp).toRegionStr());

            if (ip.getFirstIp() == null) {
                ip.setFirstIp(clientIp);
                ip.setFirstRegion(ip.getLatestRegion());
            }
            userDao.updateById(user);
        }
        return UserConverter.toDTO(user);
    }

    @Override
    public String login(String username, String password) {
        // 用户名和密码登录
        BaseUserInfoDTO user = userService.passwordLogin(username, password);
        ReqInfoContext.getReqInfo().setUserId(user.getUserId());
        ReqInfoContext.getReqInfo().setUser(user);
        return login(user.getUserId());
    }


    @Override
    public String register(String starNumber, String password, String invitationCode) {
        // 先校验星球编号，校验通过
        if (starNumberHelper.checkStarNumber(starNumber)) {
            // 根据星球编号直接查询用户是否存在，存在则直接登录，不存在则进行注册
            UserAiDO userAiDO = userAiDao.getByStarNumber(starNumber);
            // 如果用户存在，且已经审核
            if (userAiDO != null) {
                if (UserAIStatEnum.FORMAL.getCode().equals(userAiDO.getState())) {
                    // 直接登录
                    return this.login(userAiDO.getUserId());
                } else if (UserAIStatEnum.TRYING.getCode().equals(userAiDO.getState())) {
                    // 试用期内，直接登录
                    // 如果超过 3 天还没有审核，则提示用户等待审核
                    if (DateUtil.offsetDay(userAiDO.getCreateTime(), 3).isAfter(DateUtil.date())) {
                        throw ExceptionUtil.of(StatusEnum.USER_NOT_AUDIT, "星球编号=" + starNumber);
                    } else {
                        return this.login(userAiDO.getUserId());
                    }

                } else {
                    // 等待审核
                    throw ExceptionUtil.of(StatusEnum.USER_NOT_AUDIT, "星球编号=" + starNumber);
                }
            } else {
                // 用户不存在，进行注册，注册完进入试用
                UserAiDO registerUserAI = userDao.registerUser(starNumber, password);
                return this.login(registerUserAI.getUserId());
            }

        } else {
            // 星球编号校验不通过
            throw ExceptionUtil.of(StatusEnum.USER_STAR_NOT_EXISTS, "星球编号=" + starNumber);
        }
    }

    @Override
    public void registerUser(String username, String password) {
        userDao.registerUser(username, password);
    }

}
