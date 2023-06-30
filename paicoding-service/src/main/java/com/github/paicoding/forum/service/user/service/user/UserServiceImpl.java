package com.github.paicoding.forum.service.user.service.user;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.article.dto.YearArticleDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.user.converter.UserConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserAiDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserRelationDao;
import com.github.paicoding.forum.service.user.repository.entity.*;
import com.github.paicoding.forum.service.user.service.CountService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.help.UserSessionHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
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
    private ArticleReadService articleReadService;

    @Autowired
    private CountService countService;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private UserSessionHelper userSessionHelper;

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

        // 查询 user_ai
        UserAiDO userAiDO = userAiDao.getByUserId(userId);

        return UserConverter.toDTO(user, userAiDO);
    }

    @Override
    public BaseUserInfoDTO queryBasicUserInfo(Long userId) {
        UserInfoDO user = userDao.getByUserId(userId);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userId);
        }
        return UserConverter.toDTO(user);
    }

    public List<BaseUserInfoDTO> batchQueryBasicUserInfo(Collection<Long> userIds) {
        List<UserInfoDO> users = userDao.getByUserIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userId=" + userIds);
        }
        return users.stream().map(info -> UserConverter.toDTO(info)).collect(Collectors.toList());
    }

    @Override
    public UserStatisticInfoDTO queryUserInfoWithStatistic(Long userId) {
        BaseUserInfoDTO userInfoDTO = queryBasicUserInfo(userId);
        UserStatisticInfoDTO userHomeDTO = UserConverter.toUserHomeDTO(userInfoDTO);

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


        // 获取文章相关统计
        ArticleFootCountDTO articleFootCountDTO = countService.queryArticleCountInfoByUserId(userId);
        if (articleFootCountDTO != null) {
            userHomeDTO.setPraiseCount(articleFootCountDTO.getPraiseCount());
            userHomeDTO.setReadCount(articleFootCountDTO.getReadCount());
            userHomeDTO.setCollectionCount(articleFootCountDTO.getCollectionCount());
        } else {
            userHomeDTO.setPraiseCount(0);
            userHomeDTO.setReadCount(0);
            userHomeDTO.setCollectionCount(0);
        }

        // 获取发布文章总数
        int articleCount = articleReadService.queryArticleCount(userId);
        userHomeDTO.setArticleCount(articleCount);

        // 获取关注数
        Long followCount = userRelationDao.queryUserFollowCount(userId);
        userHomeDTO.setFollowCount(followCount.intValue());

        // 粉丝数
        Long fansCount = userRelationDao.queryUserFansCount(userId);
        userHomeDTO.setFansCount(fansCount.intValue());

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

    /**
     * 查询用户基本信息
     *
     * @param userIds
     * @return
     */
    @Override
    public List<BaseUserInfoDTO> queryBasicUserInfos(List<Long> userIds) {
        // 根据 userIds 查询
        List<UserInfoDO> users = userDao.getByUserIds(userIds);
        return users.stream().map(info -> UserConverter.toDTO(info)).collect(Collectors.toList());
    }

    @Override
    public Long getUserCount() {
        return this.userDao.getUserCount();
    }
}
