package com.github.liuyueyi.forum.service.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liuyueyi.forum.core.common.enums.CollectionStatEnum;
import com.github.liuyueyi.forum.core.common.enums.CommentStatEnum;
import com.github.liuyueyi.forum.core.common.enums.PraiseStatEnum;
import com.github.liuyueyi.forum.service.repository.entity.UserDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserFootDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserInfoDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserRelationDTO;
import com.github.liuyueyi.forum.service.repository.param.PageParam;

import java.util.List;

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
    Long addUser(UserDTO userDTO);

    /**
     * 更新用户
     * @param userDTO
     */
    void updateUser(UserDTO userDTO);

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
    Long addUserInfo(UserInfoDTO userInfoDTO);

    /**
     * 更新用户信息
     * @param userInfoDTO
     */
    void updateUserInfo(UserInfoDTO userInfoDTO);

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
    UserInfoDTO getUserInfoByUserId(Long userId);

    /**
     * 新增用户关系
     * @param userRelationDTO
     * @return
     */
    Long addUserRelation(UserRelationDTO userRelationDTO);

    /**
     * 获取关注用户列表
     * @param userId
     * @return
     */
    IPage<UserRelationDTO> getUserRelationListByUserId(Integer userId, PageParam pageParam);

    /**
     * 获取被关注用户列表
     * @param followUserId
     * @return
     */
    IPage<UserRelationDTO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam);

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
    Long addUserFoot(UserFootDTO userFootDTO);

    /**
     * 文章收藏数
     * @param documentId
     * @return
     */
    Integer queryCollentionCount(Long documentId);

    /**
     * 文章阅读数
     * @param documentId
     * @return
     */
    Integer queryReadCount(Long documentId);

    /**
     * 文章评论数
     * @param documentId
     * @return
     */
    Integer queryCommentCount(Long documentId);

    /**
     * 文章点赞数
     * @param documentId
     * @return
     */
    Integer queryPraiseCount(Long documentId);

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
