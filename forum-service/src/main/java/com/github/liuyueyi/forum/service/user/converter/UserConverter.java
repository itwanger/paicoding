package com.github.liuyueyi.forum.service.user.converter;

import com.github.liueyueyi.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户转换
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserConverter {

    public UserDO toDO(UserSaveReq req) {
        if (req == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        userDO.setId(req.getUserId());
        userDO.setThirdAccountId(req.getThirdAccountId());
        userDO.setLoginType(req.getLoginType());
        return userDO;
    }

    public UserInfoDO toDO(UserInfoSaveReq req) {
        if (req == null) {
            return null;
        }
        UserInfoDO userInfoDO = new UserInfoDO();
        userInfoDO.setUserId(req.getUserId());
        userInfoDO.setUserName(req.getUserName());
        userInfoDO.setPhoto(req.getPhoto());
        userInfoDO.setPosition(req.getPosition());
        userInfoDO.setCompany(req.getCompany());
        userInfoDO.setProfile(req.getProfile());
        return userInfoDO;
    }

    public BaseUserInfoDTO toDO(UserInfoDO info) {
        if (info == null) {
            return null;
        }
        BaseUserInfoDTO user = new BaseUserInfoDTO();
        // todo 知识点，bean属性拷贝的几种方式， 直接get/set方式，使用BeanUtil工具类(spring, cglib, apache, objectMapper)，序列化方式等
        BeanUtils.copyProperties(info, user);
        return user;
    }

    public UserRelationDO toDO(UserRelationReq req) {
        if (req == null) {
            return null;
        }
        UserRelationDO userRelationDO = new UserRelationDO();
        userRelationDO.setId(req.getUserRelationId());
        userRelationDO.setUserId(req.getUserId());
        userRelationDO.setFollowUserId(req.getFollowUserId());
        userRelationDO.setFollowState(req.getFollowState());
        return userRelationDO;
    }
}
