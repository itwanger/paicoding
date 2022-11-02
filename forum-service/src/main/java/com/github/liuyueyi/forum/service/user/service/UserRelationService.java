package com.github.liuyueyi.forum.service.user.service;

import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.FollowUserInfoDTO;

/**
 * 用户关系Service接口
 *
 * @author louzai
 * @date 2022-07-20
 */
public interface UserRelationService {

    /**
     * 我关注的用户
     *
     * @param userId
     * @param pageParam
     * @return
     */
    PageListVo<FollowUserInfoDTO> getUserFollowList(Long userId, PageParam pageParam);


    /**
     * 关注我的粉丝
     *
     * @param userId
     * @param pageParam
     * @return
     */
    PageListVo<FollowUserInfoDTO> getUserFansList(Long userId, PageParam pageParam);

    /**
     * 更新当前登录用于与列表中的用户的关注关系
     *
     * @param followList
     * @param loginUserId
     */
    void updateUserFollowRelationId(PageListVo<FollowUserInfoDTO> followList, Long loginUserId);


    /**
     * 保存用户关系
     *
     * @param req
     * @throws Exception
     */
    void saveUserRelation(UserRelationReq req);
}
