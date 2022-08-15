package com.github.liuyueyi.forum.service.user;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;

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
    UserFollowListDTO getUserFollowList(Long userId, PageParam pageParam);


    /**
     * 关注我的粉丝
     *
     * @param userId
     * @param pageParam
     * @return
     */
    UserFollowListDTO getUserFansList(Long userId, PageParam pageParam);


    /**
     * 保存用户关系
     * @param req
     * @throws Exception
     */
    void saveUserRelation(UserRelationReq req) throws Exception;
}
