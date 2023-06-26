package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
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
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.service.LoginOutService;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.help.StarNumberHelper;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
@Slf4j
public class LoginOutServiceImpl implements LoginOutService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;


    @Autowired
    private UserSessionHelper userSessionHelper;
    @Autowired
    private StarNumberHelper starNumberHelper;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserPwdEncoder userPwdEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String autoRegisterAndGetVerifyCode(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        req.setUserId(registerOrGetUserInfo(req));
        return userSessionHelper.genVerifyCode(req.getUserId());
    }

    /**
     * 没有注册时，先注册一个用户；若已经有，则登录
     *
     * @param req
     */
    private Long registerOrGetUserInfo(UserSaveReq req) {
        UserDO user = userDao.getByThirdAccountId(req.getThirdAccountId());
        if (user == null) {
            return registerService.registerByWechat(req.getThirdAccountId());
        }
        return user.getId();
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
        UserDO user = userDao.getByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + username);
        }

        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        // 登录成功，返回对应的session
        ReqInfoContext.getReqInfo().setUserId(user.getId());
        return userSessionHelper.genSession(user.getId());
    }


    @Override
    public String register(String userName, String password, String starNumber, String invitationCode) {
        // 星球直接登录时，判断星球用户是否存在
        UserDO user = userDao.getByUserName(userName);
        if (user == null) {
            // 注册用户
            Long userId = registerService.registerByUserNameAndPassword(userName, password, String.valueOf(starNumber), invitationCode);
            return userSessionHelper.genSession(userId);
        }

        // 走登录绑定流程
        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, starNumber);
        }

        // 绑定星球号
        if (starNumber != null && Boolean.FALSE.equals(starNumberHelper.checkStarNumber(starNumber))) {
            // 星球编号校验不通过，直接抛异常
            throw ExceptionUtil.of(StatusEnum.USER_STAR_NOT_EXISTS, "星球编号=" + starNumber);
        }

        // 根据星球编号直接查询用户是否存在，存在则直接登录，不存在则进行注册
        UserAiDO userAiDO = userAiDao.getByUserId(user.getId());
        userAiDO.setStarNumber(starNumber);
        if (invitationCode != null) {
            UserAiDO invite = userAiDao.getByInviteCode(invitationCode);
            if (invite != null) {
                userAiDO.setInviterUserId(invite.getUserId());
                userAiDO.setCondition(userAiDO.getCondition() | 2);
            }
        }
        userAiDao.saveOrUpdate(userAiDO);
        return userSessionHelper.genSession(user.getId());
    }
}
