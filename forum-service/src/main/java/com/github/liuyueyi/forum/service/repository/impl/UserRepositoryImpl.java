package com.github.liuyueyi.forum.service.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liuyueyi.forum.core.common.enums.YesOrNoEnum;
import com.github.liuyueyi.forum.service.repository.UserRepository;
import com.github.liuyueyi.forum.service.repository.entity.UserDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserFootDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserInfoDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserRelationDTO;
import com.github.liuyueyi.forum.service.repository.mapper.UserFootMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserMapper;
import com.github.liuyueyi.forum.service.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    public Integer addUser(UserDTO userDTO) {
        userMapper.insert(userDTO);
        return userDTO.getId();
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        userMapper.updateById(userDTO);
    }

    @Override
    public Integer addUserInfo(UserInfoDTO userInfoDTO) {
        userInfoMapper.insert(userInfoDTO);
        return userInfoDTO.getId();
    }

    @Override
    public void updateUserInfo(UserInfoDTO userInfoDTO) {
        userInfoMapper.updateById(userInfoDTO);
    }

    @Override
    public void deleteUserInfo(Long userId) {
        LambdaQueryWrapper<UserInfoDTO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDTO::getUserId, userId)
                .eq(UserInfoDTO::getDeleted, YesOrNoEnum.NO.getCode());
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserId(userId);
        userInfoDTO.setDeleted(YesOrNoEnum.YES.getCode());
        userInfoMapper.update(userInfoDTO, query);
    }

    @Override
    public UserInfoDTO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDTO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDTO::getUserId, userId)
                .eq(UserInfoDTO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    @Override
    public Integer addUserRelation(UserRelationDTO userRelationDTO) {
        userRelationMapper.insert(userRelationDTO);
        return userRelationDTO.getId();
    }

    @Override
    public List<UserRelationDTO> getUserRelationListByUserId(Integer userId) {
        LambdaQueryWrapper<UserRelationDTO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDTO::getUserId, userId);
        return userRelationMapper.selectList(query);
    }

    @Override
    public List<UserRelationDTO> getUserRelationListByFollowUserId(Integer followUserId) {
        LambdaQueryWrapper<UserRelationDTO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDTO::getFollowUserId, followUserId);
        return userRelationMapper.selectList(query);
    }

    @Override
    public void deleteUserRelationById(Long id) {
        UserRelationDTO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }

    public Integer addUserFoot(UserFootDTO userFootDTO) {
        userFootMapper.insert(userFootDTO);
        return userFootDTO.getId();
    }


}