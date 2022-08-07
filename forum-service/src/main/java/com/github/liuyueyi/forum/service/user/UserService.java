package com.github.liuyueyi.forum.service.user;

import com.github.liueyueyi.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liuyueyi.forum.service.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;

/**
 * 用户Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserService {

    /**
     * 保存用户
     * @param req
     * @throws Exception
     */
    void saveUser(UserSaveReq req);

    /**
     * 保存用户详情
     * @param req
     */
    void saveUserInfo(UserInfoSaveReq req);

    /**
     * 查询用户详情信息
     * @param userId
     * @return
     */
    UserInfoDO getUserInfoByUserId(Long userId);


    /**
     * 查询用户主页信息
     * @param userId
     * @return
     * @throws Exception
     */
    UserHomeDTO getUserHomeDTO(Long userId);
}
