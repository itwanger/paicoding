package com.github.paicoding.forum.service.user.service.user;

import com.beust.ah.A;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.article.dto.YearArticleDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.api.model.vo.user.UserZsxqLoginReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.image.service.ImageService;
import com.github.paicoding.forum.service.statistics.service.CountService;
import com.github.paicoding.forum.service.user.converter.UserAiConverter;
import com.github.paicoding.forum.service.user.converter.UserConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserRelationDao;
import com.github.paicoding.forum.service.user.repository.entity.IpInfo;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import com.github.paicoding.forum.service.user.service.UserAiService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import com.github.paicoding.forum.service.user.service.help.UserPwdEncoder;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private UserAiDao userAiDao;

    @Resource
    private UserRelationDao userRelationDao;

    @Autowired
    private CountService countService;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private UserPwdEncoder userPwdEncoder;

    @Autowired
    private UserAiService userAiService;

    @Autowired
    private ImageService imageService;

    @Resource
    private AiConfig aiConfig;

    @Override
    public UserDO getWxUser(String wxuuid) {
        return userDao.getByThirdAccountId(wxuuid);
    }

    @Override
    public List<SimpleUserInfoDTO> searchUser(String userName) {
        List<UserInfoDO> users = userDao.getByUserNameLike(userName);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(s -> new SimpleUserInfoDTO()
                        .setUserId(s.getUserId())
                        .setName(s.getUserName())
                        .setAvatar(s.getPhoto())
                        .setProfile(s.getProfile())
                )
                .collect(Collectors.toList());
    }

    @Override
    public void saveUserInfo(UserInfoSaveReq req) {
        UserInfoDO userInfoDO = UserConverter.toDO(req);
        userDao.updateUserInfo(userInfoDO);
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
            // 常见于：session中记录的用户被删除了，直接移除缓存中的session，走重新登录流程
            userSessionHelper.removeSession(session);
            return null;
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

        // 查询 user_ai信息，标注用户是否为星球专属用户
        UserAiDO userAiDO = userAiDao.getByUserId(userId);
        this.autoUpdateUserStarState(userAiDO);
        return UserConverter.toDTO(user, userAiDO);
    }

    private void autoUpdateUserStarState(UserAiDO userAiDO) {
        if (userAiDO == null) {
            return;
        }
        if (userAiDO.getStarExpireTime() == null) {
            // 更新用户星球过期时间
            if (userAiDO.getState().equals(UserAIStatEnum.FORMAL.getCode())) {
                // 没有失效时间的星球用户，默认设置为当前时间往后 + 360天（一年）
                userAiDO.setStarExpireTime(new Date(System.currentTimeMillis() + aiConfig.getMaxNum().getExpireDays() * 24 * 60 * 60 * 1000L));
                userAiDO.setUpdateTime(new Date());
                userAiDao.updateById(userAiDO);
            }
        } else if (System.currentTimeMillis() >= userAiDO.getStarExpireTime().getTime()) {
            // 账号已过期
            if (!userAiDO.getState().equals(UserAIStatEnum.EXPIRED.getCode())) {
                userAiDO.setState(UserAIStatEnum.EXPIRED.getCode());
                userAiDO.setUpdateTime(new Date());
                userAiDao.updateById(userAiDO);
            }
        }
    }

    @Override
    public SimpleUserInfoDTO querySimpleUserInfo(Long userId) {
        UserInfoDO user = userDao.getByUserId(userId);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userId);
        }
        return UserConverter.toSimpleInfo(user);
    }

    @Override
    public BaseUserInfoDTO queryBasicUserInfo(Long userId) {
        UserInfoDO user = userDao.getByUserId(userId);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userId);
        }
        return UserConverter.toDTO(user);
    }

    @Override
    public List<SimpleUserInfoDTO> batchQuerySimpleUserInfo(Collection<Long> userIds) {
        List<UserInfoDO> users = userDao.getByUserIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserConverter::toSimpleInfo).collect(Collectors.toList());
    }

    @Override
    public List<BaseUserInfoDTO> batchQueryBasicUserInfo(Collection<Long> userIds) {
        List<UserInfoDO> users = userDao.getByUserIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userIds);
        }
        return users.stream().map(UserConverter::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserStatisticInfoDTO queryUserInfoWithStatistic(Long userId) {
        BaseUserInfoDTO userInfoDTO = queryBasicUserInfo(userId);
        UserAiDO aiDO = userAiDao.getByUserId(userId);
        if (aiDO != null) {
            userInfoDTO.setStarNumber(aiDO.getStarNumber());
            userInfoDTO.setExpireTime(aiDO.getStarExpireTime());
            userInfoDTO.setStarStatus(UserAIStatEnum.fromCode(aiDO.getState()));
        }

        UserStatisticInfoDTO userHomeDTO = countService.queryUserStatisticInfo(userId);
        userHomeDTO = UserConverter.toUserHomeDTO(userHomeDTO, userInfoDTO);

        // 用户资料完整度
        int cnt = 0;
        if (StringUtils.isNotBlank(userHomeDTO.getCompany())) {
            ++cnt;
        }
        if (StringUtils.isNotBlank(userHomeDTO.getPosition())) {
            ++cnt;
        }
        if (StringUtils.isNotBlank(userHomeDTO.getProfile())) {
            ++cnt;
        }
        userHomeDTO.setInfoPercent(cnt * 100 / 3);

        // 是否关注
        Long followUserId = ReqInfoContext.getReqInfo().getUserId();
        if (followUserId != null) {
            UserRelationDO userRelationDO = userRelationDao.getUserRelationByUserId(userId, followUserId);
            userHomeDTO.setFollowed((userRelationDO == null) ? Boolean.FALSE : Boolean.TRUE);
        } else {
            userHomeDTO.setFollowed(Boolean.FALSE);
        }

        // 加入天数
        int joinDayCount = (int) ((System.currentTimeMillis() - userHomeDTO.getCreateTime()
                .getTime()) / (1000 * 3600 * 24));
        userHomeDTO.setJoinDayCount(Math.max(1, joinDayCount));

        // 创作历程
        List<YearArticleDTO> yearArticleDTOS = articleDao.listYearArticleByUserId(userId);
        userHomeDTO.setYearArticleList(yearArticleDTOS);
        return userHomeDTO;
    }

    @Override
    public Long getUserCount() {
        return this.userDao.getUserCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserInfo(UserPwdLoginReq loginReq) {
        // 0. 绑定用户名 & 密码 前置校验
        UserDO user = userDao.getUserByUserName(loginReq.getUsername());
        if (user == null) {
            // 用户名不存在，则标识当前登录用户可以使用这个用户名
            user = new UserDO();
            user.setId(loginReq.getUserId());
        } else if (!Objects.equals(loginReq.getUserId(), user.getId())) {
            // 登录用户名已经存在了
            throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, loginReq.getUsername());
        }

        // 1. 更新用户名密码
        user.setUserName(loginReq.getUsername());
        user.setPassword(userPwdEncoder.encPwd(loginReq.getPassword()));
        userDao.saveUser(user);

        // 2. 更新ai相关信息
        userAiService.initOrUpdateAiInfo(loginReq);
    }

    @Override
    public BaseUserInfoDTO queryUserByLoginName(String uname) {
        UserDO user = userDao.getUserByUserName(uname);
        if (user == null) {
            return null;
        }

        return queryBasicUserInfo(user.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserInfo(UserZsxqLoginReq loginReq) {
        long userId = ReqInfoContext.getReqInfo().getUserId();

        // 总是尝试更新用户信息（昵称和头像），因为知识星球的信息应该是最新的
        boolean shouldUpdateUserInfo = loginReq.getUpdateUserInfo() != null ? loginReq.getUpdateUserInfo() : true;

        if (shouldUpdateUserInfo) {
            UserInfoDO user = new UserInfoDO();
            user.setUserId(userId);
            boolean hasUpdates = false;

            // 更新用户昵称：优先使用displayName，如果没有则使用username
            if (StringUtils.isNotBlank(loginReq.getDisplayName())) {
                user.setUserName(loginReq.getDisplayName());
                hasUpdates = true;
            } else if (StringUtils.isNotBlank(loginReq.getUsername())) {
                user.setUserName(loginReq.getUsername());
                hasUpdates = true;
            }

            // 更新头像（如果有的话）
            if (StringUtils.isNotBlank(loginReq.getAvatar())) {
                user.setPhoto(imageService.saveImg(loginReq.getAvatar()));
                hasUpdates = true;
            }

            // 只有当有实际更新内容时才调用更新方法
            if (hasUpdates) {
                userDao.updateUserInfo(user);
            }
        }

        // 更新用户绑定的星球账号信息
        UserAiDO aiDO = userAiDao.getByUserId(userId);
        if (aiDO == null) {
            // 插入当前用户的星球信息
            checkAndIllegalOtherStarNumber(userId, loginReq.getStarNumber());
            aiDO = UserAiConverter.initAi(userId);
            aiDO.setStarNumber(loginReq.getStarNumber());
            this.autoUpdateStarInfo(aiDO, loginReq);
        } else if (!aiDO.getStarNumber().equals(loginReq.getStarNumber())) {
            // 存在，且星球号不同，以接口为准
            checkAndIllegalOtherStarNumber(userId, loginReq.getStarNumber());
            this.autoUpdateStarInfo(aiDO, loginReq);
        } else {
            // 星球号相同，但需要检查过期时间和状态是否需要更新
            Date currentExpireTime = aiDO.getStarExpireTime();
            Date newExpireTime = new Date(loginReq.getExpireTime());

            // 如果过期时间不同，或者当前状态是过期但新的过期时间未过期，则需要更新
            boolean needUpdate = false;
            if (currentExpireTime == null || !currentExpireTime.equals(newExpireTime)) {
                needUpdate = true;
            }
            // 检查状态：如果用户当前是过期状态，但新的过期时间未过期，则需要更新状态为正常
            if (aiDO.getState().equals(UserAIStatEnum.EXPIRED.getCode()) &&
                System.currentTimeMillis() < loginReq.getExpireTime()) {
                needUpdate = true;
            }

            if (needUpdate) {
                this.autoUpdateStarInfo(aiDO, loginReq);
            }
        }
    }


    /**
     * 如果星球号被别的账号占用了，则下单之前的账号
     *
     * @param userId     当前用户
     * @param starNumber 星球号
     */
    private void checkAndIllegalOtherStarNumber(Long userId, String starNumber) {
        UserAiDO conflict = userAiDao.getByStarNumber(starNumber);
        if (conflict != null && !conflict.getUserId().equals(userId)) {
            // 知识星球回调的用户星球号，被其他账号绑定了的场景：去掉其他账号的星球号信息，以接口绑定的为准
            conflict.setStarNumber("");
            conflict.setState(UserAIStatEnum.NOT_PASS.getCode());
            userAiDao.updateById(conflict);
        }
    }

    /**
     * 更新用户星球号信息
     *
     * @param aiDO
     * @param loginReq
     */
    private void autoUpdateStarInfo(UserAiDO aiDO, UserZsxqLoginReq loginReq) {
        aiDO.setStarNumber(loginReq.getStarNumber());
        aiDO.setStarExpireTime(new Date(loginReq.getExpireTime()));
        if (System.currentTimeMillis() < loginReq.getExpireTime()) {
            aiDO.setState(UserAIStatEnum.FORMAL.getCode());
        } else {
            aiDO.setState(UserAIStatEnum.EXPIRED.getCode());
        }
        userAiDao.saveOrUpdateAiBindInfo(aiDO);
    }


    public UserDO getUserDO(Long userId) {
        return userDao.getUserByUserId(userId);
    }

    public UserInfoDO getUserInfo(Long userId) {
        return userDao.getByUserId(userId);
    }

    public UserAiDO getUserAiDO(Long userId) {
        return userAiDao.getByUserId(userId);
    }
}
