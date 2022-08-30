package com.github.liuyueyi.forum.service.user.repository;

/**
 * 用户相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserRepository {
    /**
     * 查询关注用户总数
     *
     * @param userId
     * @return
     */
    Long queryUserFollowCount(Long userId);

    /**
     * 查询粉丝总数
     *
     * @param userId
     * @return
     */
    Long queryUserFansCount(Long userId);
}
