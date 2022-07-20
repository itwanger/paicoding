package com.github.liuyueyi.forum.service.user.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liuyueyi.forum.service.common.enums.CollectionStatEnum;
import com.github.liuyueyi.forum.service.common.enums.CommentStatEnum;
import com.github.liuyueyi.forum.service.common.enums.PraiseStatEnum;
import com.github.liuyueyi.forum.service.user.repository.entity.UserDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.core.model.req.PageParam;

/**
 * 用户相关DB操作
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserRepository {

    /**
     * 保存用户
     * @param userDTO
     * @return
     */
    Long addUser(UserDO userDTO);

    /**
     * 更新用户
     * @param userDTO
     */
    void updateUser(UserDO userDTO);

    /**
     * 删除用户
     * @param userInfoId
     */
    void deleteUser(Long userInfoId);

    /**
     * 保存用户信息
     * @param userInfoDTO
     * @return
     */
    Long addUserInfo(UserInfoDO userInfoDTO);

    /**
     * 更新用户信息
     * @param userInfoDTO
     */
    void updateUserInfo(UserInfoDO userInfoDTO);

    /**
     * 删除用户信息
     * @param userId
     */
    void deleteUserInfo(Long userId);

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    UserInfoDO getUserInfoByUserId(Long userId);

    /**
     * 新增用户关系
     * @param userRelationDTO
     * @return
     */
    Long addUserRelation(UserRelationDO userRelationDTO);

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

    /**
     * 添加用户足迹
     * @param userFootDTO
     * @return
     */
    Long addUserFoot(UserFootDO userFootDTO);

    /**
     * 文章收藏数
     * @param documentId
     * @return
     */
    Long queryCollentionCount(Long documentId);

    /**
     * 文章阅读数
     * @param documentId
     * @return
     */
    Long queryReadCount(Long documentId);

    /**
     * 文章评论数
     * @param documentId
     * @return
     */
    Long queryCommentCount(Long documentId);

    /**
     * 文章点赞数
     * @param documentId
     * @return
     */
    Long queryPraiseCount(Long documentId);

    /**
     * 收藏/取消收藏足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer operateCollectionFoot(Long documentId, Long userId, CollectionStatEnum statEnum);

    /**
     * 评论/删除评论足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer operateCommentFoot(Long documentId, Long userId, CommentStatEnum statEnum);

    /**
     * 点赞/取消点赞足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer operatePraiseFoot(Long documentId, Long userId, PraiseStatEnum statEnum);
}
