package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleMapper;
import com.github.liuyueyi.forum.service.user.UserService;
import com.github.liuyueyi.forum.service.user.converter.UserConverter;
import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserRelationMapper userRelationMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserConverter userConverter;

    @Resource
    private UserFootServiceImpl userFootService;

    @Override
    public void saveUser(UserSaveReq req) {
        if (req.getUserId() == null || req.getUserId() == 0) {
            UserDO user = userConverter.toDO(req);
            userMapper.insert(user);
            req.setUserId(user.getId());
            return;
        }

        UserDO userDO = userMapper.selectById(req.getUserId());
        if (userDO == null) {
            throw new IllegalArgumentException("未查询到该用户");
        }
        userMapper.updateById(userConverter.toDO(req));
    }

    @Override
    public void saveUserInfo(UserInfoSaveReq req) {
        UserInfoDO userInfoDO = getUserInfoByUserId(req.getUserId());
        if (userInfoDO == null) {
            userInfoMapper.insert(userConverter.toDO(req));
            return;
        }

        UserInfoDO updateUserInfoDO = userConverter.toDO(req);
        updateUserInfoDO.setId(userInfoDO.getId());
        userInfoMapper.updateById(updateUserInfoDO);
    }

    @Override
    public UserInfoDO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    @Override
    public UserHomeDTO getUserHomeDTO(Long userId) {

        UserInfoDO userInfoDO = getUserInfoByUserId(userId);
        if (userInfoDO == null) {
            throw new IllegalArgumentException("用户不存在!");
        }

        // 获取关注数、粉丝数
        Integer followCount = userRelationMapper.queryUserFollowCount(userId, null);
        Integer fansCount = userRelationMapper.queryUserFansCount(userId, null);

        // 获取文章相关统计
        ArticleFootCountDTO articleFootCountDTO = userFootService.queryArticleCountByUserId(userId);

        // 获取发布文章总数
        LambdaQueryWrapper<ArticleDO> articleQuery = Wrappers.lambdaQuery();
        articleQuery.eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode());
        Long articleCount = articleMapper.selectCount(articleQuery);

        UserHomeDTO userHomeDTO = new UserHomeDTO();
        userHomeDTO.setUserId(userInfoDO.getUserId());
        userHomeDTO.setRole("normal");
        userHomeDTO.setUserName(userInfoDO.getUserName());
        userHomeDTO.setPhoto(userInfoDO.getPhoto());
        userHomeDTO.setProfile(userInfoDO.getProfile());
        userHomeDTO.setFollowCount(followCount);
        userHomeDTO.setFansCount(fansCount);
        if (articleFootCountDTO != null) {
            userHomeDTO.setPraiseCount(articleFootCountDTO.getPraiseCount());
            userHomeDTO.setReadCount(articleFootCountDTO.getReadCount());
            userHomeDTO.setCollectionCount(articleFootCountDTO.getCollectionCount());
        } else {
            userHomeDTO.setPraiseCount(0);
            userHomeDTO.setReadCount(0);
            userHomeDTO.setCollectionCount(0);
        }
        userHomeDTO.setArticleCount(articleCount.intValue());
        return userHomeDTO;
    }

}
