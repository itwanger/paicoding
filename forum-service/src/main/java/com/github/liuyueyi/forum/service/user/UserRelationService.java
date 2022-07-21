package com.github.liuyueyi.forum.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liuyueyi.forum.core.model.req.PageParam;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;

/**
 * 用户关系Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserRelationService {

    /**
     * 获取关注用户列表
     * @param userId
     * @return
     */
    IPage<UserRelationDO> getUserRelationListByUserId(Integer userId, PageParam pageParam);

    /**
     * 获取被关注用户列表
     * @param followUserId
     * @return
     */
    IPage<UserRelationDO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam);

    /**
     * 删除用户关系
     * @param id
     */
    void deleteUserRelationById(Long id);
}
