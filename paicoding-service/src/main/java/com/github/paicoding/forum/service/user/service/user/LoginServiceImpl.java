package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.user.LoginTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.api.model.vo.user.UserSaveReq;
import com.github.paicoding.forum.api.model.vo.user.UserZsxqLoginReq;
import com.github.paicoding.forum.core.util.StarNumberUtil;
import com.github.paicoding.forum.service.image.service.ImageService;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.UserAiService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.help.StarNumberHelper;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * 基于验证码、用户名密码的登录方式
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserAiService userAiService;
    @Autowired
    private ImageService imageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoRegisterWxUserInfo(String uuid) {
        UserSaveReq req = new UserSaveReq().setLoginType(0).setThirdAccountId(uuid);
        Long userId = registerOrGetUserInfo(req);
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userId;
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
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }

    /**
     * 给微信公众号的用户生成一个用于登录的会话
     *
     * @param userId 用户id
     * @return
     */
    @Override
    public String loginByWx(Long userId) {
        return userSessionHelper.genSession(userId);
    }

    /**
     * 用户名密码方式登录
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @Override
    public String loginByUserPwd(String username, String password) {
        UserDO user = userDao.getUserByUserName(username);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + username);
        }

        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        Long userId = user.getId();
        // 1. 为了兼容历史数据，对于首次登录成功的用户，初始化ai信息
        userAiService.initOrUpdateAiInfo(new UserPwdLoginReq().setUserId(userId).setUsername(username).setPassword(password));

        // 登录成功，返回对应的session
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genSession(userId);
    }


    /**
     * 用户名密码方式登录，若用户不存在，则进行注册
     *
     * @param loginReq 登录信息
     * @return
     */
    @Override
    public String registerByUserPwd(UserPwdLoginReq loginReq) {
        // 1. 前置校验
        registerPreCheck(loginReq);

        // 2. 判断当前用户是否登录，若已经登录，则直接走绑定流程
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        loginReq.setUserId(userId);
        if (userId != null) {
            // 如果星球编号已经绑定，且已经登录，应该跳转到个人中心页面
            // 2.1 如果用户已经登录，则走绑定用户信息流程
            userService.bindUserInfo(loginReq);
            return ReqInfoContext.getReqInfo().getSession();
        }


        // 3. 尝试使用用户名进行登录，若成功，则依然走绑定流程
        UserDO user = userDao.getUserByUserName(loginReq.getUsername());
        if (user != null) {
            if (!userPwdEncoder.match(loginReq.getPassword(), user.getPassword())) {
                // 3.1 用户名已经存在
                throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, loginReq.getUsername());
            }

            // 3.2 用户存在，尝试走绑定流程
            userId = user.getId();
            loginReq.setUserId(userId);
            userAiService.initOrUpdateAiInfo(loginReq);
        } else {
            //4. 走用户注册流程
            userId = registerService.registerByUserNameAndPassword(loginReq);
        }
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genSession(userId);
    }


    /**
     * 注册前置校验
     *
     * @param loginReq
     */
    private void registerPreCheck(UserPwdLoginReq loginReq) {
        if (StringUtils.isBlank(loginReq.getUsername()) || StringUtils.isBlank(loginReq.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        String starNumber = loginReq.getStarNumber();
        // 若传了星球信息，首先进行校验
        if (StringUtils.isNotBlank(starNumber)) {
            // 格式化星球编号
            starNumber = StarNumberUtil.formatStarNumber(starNumber);
            loginReq.setStarNumber(starNumber);

            if (Boolean.FALSE.equals(starNumberHelper.checkStarNumber(starNumber))) {
                // 星球编号校验不通过，直接抛异常
                throw ExceptionUtil.of(StatusEnum.USER_STAR_NOT_EXISTS, "星球编号=" + starNumber);
            }
        } else {
            throw ExceptionUtil.of(StatusEnum.USER_STAR_EMPTY);
        }

        UserAiDO userAi = userAiDao.getByStarNumber(loginReq.getStarNumber());
        if (userAi != null) {
            Long currentUserId = ReqInfoContext.getReqInfo().getUserId();
            
            if (currentUserId != null && userAi.getUserId().equals(currentUserId)) {
                return;
            }
            
            throw ExceptionUtil.of(StatusEnum.USER_STAR_REPEAT, loginReq.getStarNumber());
        }

        String invitationCode = loginReq.getInvitationCode();
        if (StringUtils.isNotBlank(invitationCode) && userAiDao.getByInviteCode(invitationCode) == null) {
            // 填写的邀请码不对, 找不到对应的用户
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "非法的邀请码【" + starNumber + "】");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String loginByZsxq(UserZsxqLoginReq req) {
        Long userId;
        // 1 若是全新的用户，则自动进行注册
        UserAiDO aiDO = userAiDao.getByStarNumber(req.getStarNumber());
        if (aiDO == null) {
            UserPwdLoginReq loginReq = new UserPwdLoginReq()
                    // 星球编号
                    .setStarNumber(req.getStarNumber())
                    // 使用知识星球的starNumber作为登录用户名，前缀为zsq_
                    .setUsername("zsxq_" + req.getStarNumber())
                    // 系统随机生成密码
                    .setPassword("zsxqp_" + req.getStarNumber())
                    // 使用知识星球的用户作为当前用户
                    .setDisplayName(StringUtils.isBlank(req.getDisplayName()) ? req.getUsername() : req.getDisplayName())
                    // 用户头像
                    .setAvatar(imageService.saveImg(req.getAvatar()))
                    // 过期时间
                    .setStarExpireTime(req.getExpireTime())
                    // 设置登录类型为知识星球登录
                    .setLoginType(LoginTypeEnum.ZSXQ.getType())
                    // 设置thirdAccountId为星球用户ID
                    .setThirdAccountId(String.valueOf(req.getStarUserId()));
            userId = registerService.registerByUserNameAndPassword(loginReq);

            if (System.currentTimeMillis() < req.getExpireTime()) {
                // 对于知识星球授权登录的情况，无需审核，直接成功
                userAiDao.updateUserStarState(userId, UserAIStatEnum.FORMAL.getCode());
            }
        } else {
            userId = aiDO.getUserId();
            // 2 若是已经存在的用户，则尝试更新对应的星球账号信息
            boolean needToUpdate = false;

            // 1. 更新过期时间（如果有变化）
            if (aiDO.getStarExpireTime() == null ||
                    Math.abs(req.getExpireTime() - aiDO.getStarExpireTime().getTime()) > 1000) { // 允许1秒误差
                aiDO.setStarExpireTime(new Date(req.getExpireTime()));
                needToUpdate = true;
            }

            // 2. 根据当前时间判断应该设置的状态
            long currentTime = System.currentTimeMillis();
            int expectedState = currentTime < req.getExpireTime() ?
                    UserAIStatEnum.FORMAL.getCode() :
                    UserAIStatEnum.EXPIRED.getCode(); // 假设有过期状态

            if (!Objects.equals(aiDO.getState(), expectedState)) {
                aiDO.setState(expectedState);
                needToUpdate = true;
            }

            if (needToUpdate) {
                aiDO.setUpdateTime(new Date());
                userAiDao.updateById(aiDO);
            }
        }

        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genSession(userId);
    }
}