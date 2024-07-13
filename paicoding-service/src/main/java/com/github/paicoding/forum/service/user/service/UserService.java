package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.UserPwdLoginReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;

import java.util.Collection;
import java.util.List;

/**
 * 用户Service接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface UserService {
    /**
     * 判断微信用户是否注册过
     *
     * @param wxuuid
     * @return
     */
    UserDO getWxUser(String wxuuid);

    /**
     * 根据用户名模糊搜索用户
     *
     * @param userName 用户名
     * @return
     */
    List<SimpleUserInfoDTO> searchUser(String userName);

    /**
     * 保存用户详情
     *
     * @param req
     */
    void saveUserInfo(UserInfoSaveReq req);

    /**
     * 获取登录的用户信息,并更行丢对应的ip信息
     *
     * @param session  用户会话
     * @param clientIp 用户最新的登录ip
     * @return 返回用户基本信息
     */
    BaseUserInfoDTO getAndUpdateUserIpInfoBySessionId(String session, String clientIp);

    /**
     * 查询极简的用户信息
     *
     * @param userId
     * @return
     */
    SimpleUserInfoDTO querySimpleUserInfo(Long userId);

    /**
     * 查询用户基本信息
     * todo: 可以做缓存优化
     *
     * @param userId
     * @return
     */
    BaseUserInfoDTO queryBasicUserInfo(Long userId);


    /**
     * 批量查询用户基本信息
     *
     * @param userIds
     * @return
     */
    List<SimpleUserInfoDTO> batchQuerySimpleUserInfo(Collection<Long> userIds);

    /**
     * 批量查询用户基本信息
     *
     * @param userIds
     * @return
     */
    List<BaseUserInfoDTO> batchQueryBasicUserInfo(Collection<Long> userIds);

    /**
     * 查询用户主页信息
     *
     * @param userId
     * @return
     * @throws Exception
     */
    UserStatisticInfoDTO queryUserInfoWithStatistic(Long userId);

    /**
     * 用户计数
     *
     * @return
     */
    Long getUserCount();

    /**
     * 绑定用户信息
     */
    void bindUserInfo(UserPwdLoginReq loginReq);
}
