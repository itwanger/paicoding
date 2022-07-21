package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.common.enums.*;
import com.github.liuyueyi.forum.service.user.UserService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
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
    private UserFootMapper userFootMapper;

    /**
     * 更新用户
     * @param userDTO
     */
    private void updateUser(UserDO userDTO) {
        UserDO updateUser = userMapper.selectById(userDTO.getId());
        if (updateUser != null) {
            userMapper.updateById(userDTO);
        }
    }

    /**
     * 删除用户
     * @param userInfoId
     */
    private void deleteUser(Long userInfoId) {
        UserDO updateUser = userMapper.selectById(userInfoId);
        if (updateUser != null) {
            updateUser.setDeleted(YesOrNoEnum.YES.getCode());
            userMapper.updateById(updateUser);
        }
    }

    /**
     * 更新用户信息
     * @param userInfoDTO
     */
    private void updateUserInfo(UserInfoDO userInfoDTO) {
        UserInfoDO updateUserInfo = userInfoMapper.selectById(userInfoDTO.getId());
        if (updateUserInfo != null) {
            userInfoMapper.updateById(userInfoDTO);
        }
    }

    /**
     * 删除用户信息
     * @param userId
     */
    private void deleteUserInfo(Long userId) {
        UserInfoDO updateUserInfo = userInfoMapper.selectById(userId);
        if (updateUserInfo != null) {
            updateUserInfo.setDeleted(YesOrNoEnum.YES.getCode());
            userInfoMapper.updateById(updateUserInfo);
        }
    }

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    private UserInfoDO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    /**
     * 获取关注用户列表
     * @param userId
     * @return
     */
    private IPage<UserRelationDO> getUserRelationListByUserId(Integer userId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getUserId, userId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page, query);
    }

    /**
     * 获取被关注用户列表
     * @param followUserId
     * @return
     */
    private IPage<UserRelationDO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getFollowUserId, followUserId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page, query);
    }

    /**
     * 删除用户关系
     * @param id
     */
    private void deleteUserRelationById(Long id) {
        UserRelationDO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }

    /**
     * 文章收藏数
     * @param documentId
     * @return
     */
    private Long queryCollentionCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getCollectionStat, CollectionStatEnum.COLLECTION.getCode());
        return userFootMapper.selectCount(query);
    }

    /**
     * 文章阅读数
     * @param documentId
     * @return
     */
    private Long queryReadCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getReadStat, ReadStatEnum.READ.getCode());
        return userFootMapper.selectCount(query);
    }

    /**
     * 文章评论数
     * @param documentId
     * @return
     */
    private Long queryCommentCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getCommentStat, CommentStatEnum.COMMENT.getCode());
        return userFootMapper.selectCount(query);
    }

    /**
     * 文章点赞数
     * @param documentId
     * @return
     */
    private Long queryPraiseCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getPraiseStat, PraiseStatEnum.PRAISE.getCode());
        return userFootMapper.selectCount(query);
    }

    /**
     * 收藏/取消收藏足迹
     * @param documentId
     * @param userId
     * @return
     */
    private Integer operateCollectionFoot(Long documentId, Long userId, CollectionStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCollectionStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    /**
     * 评论/删除评论足迹
     * @param documentId
     * @param userId
     * @return
     */
    private Integer operateCommentFoot(Long documentId, Long userId, CommentStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCommentStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    /**
     * 点赞/取消点赞足迹
     * @param documentId
     * @param userId
     * @return
     */
    private Integer operatePraiseFoot(Long documentId, Long userId, PraiseStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setPraiseStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }
}
