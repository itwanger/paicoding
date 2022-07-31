package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.github.liuyueyi.forum.service.user.dto.UserPageDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserRelationMapper userRelationMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserFootMapper userFootMapper;

    @Resource
    private UserConverter userConverter;

    @Override
    public void saveUser(UserSaveReq req) throws Exception {
        if (req.getUserId() == null || req.getUserId() == 0) {
            userMapper.insert(userConverter.toDO(req));
            return;
        }

        UserDO userDO = userMapper.selectById(req.getUserId());
        if (userDO == null) {
            throw new Exception("未查询到该用户");
        }
        userMapper.updateById(userConverter.toDO(req));
    }


    @Override
    public void deleteUser(Long userId) throws Exception {
        UserDO updateUser = userMapper.selectById(userId);
        if (updateUser == null) {
            throw new Exception("未查询到该用户");
        }
        updateUser.setDeleted(YesOrNoEnum.YES.getCode());
        userMapper.updateById(updateUser);
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
    public void deleteUserInfo(Long userId) {
        UserInfoDO updateUserInfo = userInfoMapper.selectById(userId);
        if (updateUserInfo != null) {
            updateUserInfo.setDeleted(YesOrNoEnum.YES.getCode());
            userInfoMapper.updateById(updateUserInfo);
        }
    }

    @Override
    public UserInfoDO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    @Override
    public UserPageDTO getUserPageDTO(Long userId) throws Exception {

        UserInfoDO userInfoDO = getUserInfoByUserId(userId);
        if (userInfoDO == null) {
            throw new Exception("未查询到该用户");
        }

        // 获取关注数、粉丝数
        Integer followCount = userRelationMapper.queryUserFollowCount(userId, null);
        Integer fansCount = userRelationMapper.queryUserFansCount(userId, null);

        // 获取文章相关统计
        ArticleFootCountDTO articleFootCountDTO = userFootMapper.queryArticleFootCount(userId);

        // 获取发布文章总数
        LambdaQueryWrapper<ArticleDO> articleQuery = Wrappers.lambdaQuery();
        articleQuery.eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, PushStatusEnum.ONLINE.getCode())
                .eq(ArticleDO::getDeleted, YesOrNoEnum.NO.getCode());
        Long articleCount = articleMapper.selectCount(articleQuery);

        UserPageDTO userPageDTO = new UserPageDTO();
        userPageDTO.setUserName(userInfoDO.getUserName());
        userPageDTO.setPhoto(userInfoDO.getPhoto());
        userPageDTO.setProfile(userInfoDO.getProfile());
        userPageDTO.setFollowCount(followCount);
        userPageDTO.setFansCount(fansCount);
        userPageDTO.setPraiseCount(articleFootCountDTO.getPraiseCount());
        userPageDTO.setReadCount(articleFootCountDTO.getReadCount());
        userPageDTO.setCollectionCount(articleFootCountDTO.getCollectionCount());
        userPageDTO.setArticleCount(articleCount.intValue());
        return userPageDTO;
    }

}
