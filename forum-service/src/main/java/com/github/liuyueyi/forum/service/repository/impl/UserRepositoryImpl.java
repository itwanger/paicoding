package com.github.liuyueyi.forum.service.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liuyueyi.forum.core.common.enums.*;
import com.github.liuyueyi.forum.service.repository.UserRepository;
import com.github.liuyueyi.forum.service.repository.entity.UserDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserFootDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserInfoDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserRelationDTO;
import com.github.liuyueyi.forum.service.repository.mapper.UserFootMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserRelationMapper;
import com.github.liuyueyi.forum.service.repository.param.PageParam;
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
    public Long addUser(UserDTO userDTO) {
        userMapper.insert(userDTO);
        return userDTO.getId();
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        UserDTO updateUser = userMapper.selectById(userDTO.getId());
        if (updateUser != null)  {
            userMapper.updateById(userDTO);
        }
    }

    @Override
    public void deleteUser(Long userInfoId) {
        UserDTO updateUser = userMapper.selectById(userInfoId);
        if (updateUser != null)  {
            updateUser.setDeleted(YesOrNoEnum.YES.getCode());
            userMapper.updateById(updateUser);
        }
    }

    @Override
    public Long addUserInfo(UserInfoDTO userInfoDTO) {
        userInfoMapper.insert(userInfoDTO);
        return userInfoDTO.getId();
    }

    @Override
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        UserInfoDTO updateUserInfo = userInfoMapper.selectById(userInfoDTO.getId());
        if (updateUserInfo != null)  {
            userInfoMapper.updateById(userInfoDTO);
        }
    }

    @Override
    public void deleteUserInfo(Long userId) {
        UserInfoDTO updateUserInfo = userInfoMapper.selectById(userId);
        if (updateUserInfo != null)  {
            updateUserInfo.setDeleted(YesOrNoEnum.YES.getCode());
            userInfoMapper.updateById(updateUserInfo);
        }
    }

    @Override
    public UserInfoDTO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDTO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDTO::getUserId, userId)
                .eq(UserInfoDTO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    @Override
    public Long addUserRelation(UserRelationDTO userRelationDTO) {
        userRelationMapper.insert(userRelationDTO);
        return userRelationDTO.getId();
    }

    @Override
    public IPage<UserRelationDTO> getUserRelationListByUserId(Integer userId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDTO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDTO::getUserId, userId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page,query);
    }

    @Override
    public IPage<UserRelationDTO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDTO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDTO::getFollowUserId, followUserId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page,query);
    }

    @Override
    public void deleteUserRelationById(Long id) {
        UserRelationDTO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }

    @Override
    public Long addUserFoot(UserFootDTO userFootDTO) {
        userFootMapper.insert(userFootDTO);
        return userFootDTO.getId();
    }

    @Override
    public Integer queryCollentionCount(Long documentId) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getCollectionStat, CollectionStatEnum.COLLECTION.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Integer queryReadCount(Long documentId) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getReadStat, ReadStatEnum.READ.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Integer queryCommentCount(Long documentId) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getCommentStat, CommentStatEnum.COMMENT.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Integer queryPraiseCount(Long documentId) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getPraiseStat, PraiseStatEnum.PRAISE.getCode());
        return userFootMapper.selectCount(query);
    }

    @Override
    public Integer operateCollectionFoot(Long documentId, Long userId, CollectionStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getUserId, userId);
        UserFootDTO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCollectionStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    @Override
    public Integer operateCommentFoot(Long documentId, Long userId, CommentStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getUserId, userId);
        UserFootDTO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setCommentStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

    @Override
    public Integer operatePraiseFoot(Long documentId, Long userId, PraiseStatEnum statEnum) {
        LambdaQueryWrapper<UserFootDTO> query = Wrappers.lambdaQuery();
        query.eq(UserFootDTO::getDoucumentId, documentId)
                .eq(UserFootDTO::getUserId, userId);
        UserFootDTO userFootDTO = userFootMapper.selectOne(query);
        userFootDTO.setPraiseStat(statEnum.getCode());
        return userFootMapper.update(userFootDTO, query);
    }

}