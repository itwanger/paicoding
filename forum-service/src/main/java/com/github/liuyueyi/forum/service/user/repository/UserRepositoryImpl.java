package com.github.liuyueyi.forum.service.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.common.enums.*;
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
 * 用户相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
@Service
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserRelationMapper userRelationMapper;

    @Resource
    private UserFootMapper userFootMapper;

    @Override
    public Long addUser(UserDO userDTO) {
        userMapper.insert(userDTO);
        return userDTO.getId();
    }

    @Override
    public void updateUser(UserDO userDTO) {
        UserDO updateUser = userMapper.selectById(userDTO.getId());
        if (updateUser != null) {
            userMapper.updateById(userDTO);
        }
    }

    @Override
    public void deleteUser(Long userInfoId) {
        UserDO updateUser = userMapper.selectById(userInfoId);
        if (updateUser != null) {
            updateUser.setDeleted(YesOrNoEnum.YES.getCode());
            userMapper.updateById(updateUser);
        }
    }

    @Override
    public Long addUserInfo(UserInfoDO userInfoDTO) {
        userInfoMapper.insert(userInfoDTO);
        return userInfoDTO.getId();
    }

    @Override
    public void updateUserInfo(UserInfoDO userInfoDTO) {
        UserInfoDO updateUserInfo = userInfoMapper.selectById(userInfoDTO.getId());
        if (updateUserInfo != null) {
            userInfoMapper.updateById(userInfoDTO);
        }
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
    public Long addUserRelation(UserRelationDO userRelationDTO) {
        userRelationMapper.insert(userRelationDTO);
        return userRelationDTO.getId();
    }

    @Override
    public IPage<UserRelationDO> getUserRelationListByUserId(Integer userId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getUserId, userId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page, query);
    }

    @Override
    public IPage<UserRelationDO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getFollowUserId, followUserId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page, query);
    }

    @Override
    public void deleteUserRelationById(Long id) {
        UserRelationDO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }

    @Override
    public Long addUserFoot(UserFootDO userFootDTO) {
        userFootMapper.insert(userFootDTO);
        return userFootDTO.getId();
    }

    @Override
    public Long queryCollentionCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getCollectionStat, CollectionStatEnum.COLLECTION.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Long queryReadCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getReadStat, ReadStatEnum.READ.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Long queryCommentCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getCommentStat, CommentStatEnum.COMMENT.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Long queryPraiseCount(Long documentId) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getPraiseStat, PraiseStatEnum.PRAISE.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Integer operateCollectionFoot(Long documentId, Long userId, CollectionStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCollectionStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    @Override
    public Integer operateCommentFoot(Long documentId, Long userId, CommentStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCommentStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    @Override
    public Integer operatePraiseFoot(Long documentId, Long userId, PraiseStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDO::getDoucumentId, documentId)
                .eq(UserFootDO::getUserId, userId);
        UserFootDO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setPraiseStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

}