package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.LoginTypeEnum;
import com.github.paicoding.forum.api.model.enums.user.StarSourceEnum;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.TransactionUtil;
import com.github.paicoding.forum.service.user.converter.UserAiConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserRandomGenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户注册服务
 *
 * @author YiHui
 * @date 2023/6/26
 */
@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserPwdEncoder userPwdEncoder;
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOldUser(String userName, String password, String starNumber, String invitationCode, Long userId) {
        // 之前已经检查过编号是否已经被绑定过了，那我们直接进行绑定
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        if (userAiDO == null) {
            userAiDO = UserAiConverter.initAi(userId);
        } else {
            // 之前有绑定信息，检查是否是同一个星球号
            if (!Objects.equals(starNumber, userAiDO.getStarNumber())) {
                // 不同时，更新星球号，并设置为试用
                userAiDO.setStarNumber(starNumber).setState(UserAIStatEnum.TRYING.getCode());
            } else {
                // 改变状态为试用
                userAiDO.setState(UserAIStatEnum.TRYING.getCode());
                // 改变登录用户的星球状态
                ReqInfoContext.getReqInfo().getUser().setStarStatus(UserAIStatEnum.TRYING.getCode());
            }
        }
        userAiDao.saveOrUpdateAiBindInfo(userAiDO, invitationCode);

        // 获取用户，对登录用户名和密码也进行更新，这样就可以通过用户名和密码登录了
        UserDO user = new UserDO();
        user.setUserName(userName);
        user.setId(userId);
        user.setPassword(userPwdEncoder.encPwd(password));
        userDao.updateUser(user);

        processAfterUserBind(user.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerByUserNameAndPassword(String username, String password, String star, String inviteCode) {
        // 保存用户登录信息
        UserDO user = new UserDO();
        user.setUserName(username);
        user.setPassword(userPwdEncoder.encPwd(password));
        user.setThirdAccountId("");
        user.setLoginType(LoginTypeEnum.USER_PWD.getType());
        userDao.saveUser(user);

        // 保存用户信息
        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(user.getId());
        userInfo.setUserName(username);
        userInfo.setPhoto(UserRandomGenHelper.genAvatar());
        userDao.save(userInfo);

        // 保存ai相互信息
        UserAiDO userAiDO = new UserAiDO();
        userAiDO.setUserId(user.getId());
        userAiDO.setStarNumber(star);
        // 先只支持Java进阶之路的星球绑定
        userAiDO.setStarType(StarSourceEnum.JAVA_GUIDE.getSource());
        userAiDO.setStrategy(0);
        userAiDO.setInviteNum(0);
        userAiDO.setDeleted(0);
        userAiDO.setInviteCode(UserRandomGenHelper.genInviteCode(user.getId()));
        userAiDO.setState(UserAIStatEnum.TRYING.getCode());
        userAiDao.saveOrUpdateAiBindInfo(userAiDO, inviteCode);

        processAfterUserRegister(user.getId());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerByWechat(String thirdAccount) {
        // 用户不存在，则需要注册
        // 保存用户登录信息
        UserDO user = new UserDO();
        user.setThirdAccountId(thirdAccount);
        user.setLoginType(LoginTypeEnum.WECHAT.getType());
        userDao.saveUser(user);


        // 初始化用户信息，随机生成用户昵称 + 头像
        UserInfoDO userInfo = new UserInfoDO();
        userInfo.setUserId(user.getId());
        userInfo.setUserName(UserRandomGenHelper.genNickName());
        userInfo.setPhoto(UserRandomGenHelper.genAvatar());
        userDao.save(userInfo);

        // 保存ai相互信息
        UserAiDO userAiDO = UserAiConverter.initAi(user.getId());
        userAiDao.saveOrUpdateAiBindInfo(userAiDO, null);

        processAfterUserRegister(user.getId());
        return user.getId();
    }


    /**
     * 用户注册完毕之后触发的动作
     *
     * @param userId
     */
    private void processAfterUserRegister(Long userId) {
        TransactionUtil.registryAfterCommitOrImmediatelyRun(new Runnable() {
            @Override
            public void run() {
                // 用户注册事件
                SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.REGISTER, userId));
            }
        });
    }

    /**
     * 用户绑定完毕之后触发的动作
     *
     * @param userId
     */
    private void processAfterUserBind(Long userId) {
        TransactionUtil.registryAfterCommitOrImmediatelyRun(new Runnable() {
            @Override
            public void run() {
                // 用户注册事件
                SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.BIND, userId));
            }
        });
    }
}
