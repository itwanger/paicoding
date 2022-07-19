package com.github.liuyueyi.forum.service.repository;

import com.github.liuyueyi.forum.service.repository.entity.UserDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserFootDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserInfoDTO;
import com.github.liuyueyi.forum.service.repository.entity.UserRelationDTO;

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
    Integer addUser(UserDTO userDTO);

    /**
     * 更新用户
     * @param userDTO
     */
    void updateUser(UserDTO userDTO);

    /**
     * 保存用户信息
     * @param userInfoDTO
     * @return
     */
    Integer addUserInfo(UserInfoDTO userInfoDTO);

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
    Integer addUserRelation(UserRelationDTO userRelationDTO);

    /**
     * 获取关注用户列表
     * @param userId
     * @return
     */
    List<UserRelationDTO> getUserRelationListByUserId(Integer userId);

    /**
     * 获取被关注用户列表
     * @param followUserId
     * @return
     */
    List<UserRelationDTO> getUserRelationListByFollowUserId(Integer followUserId);

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
    Integer addUserFoot(UserFootDTO userFootDTO);

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
     * 取消收藏足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer cancelCollectionFoot(Long documentId, Long userId);

    /**
     * 删除评论足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer deleteCommentFoot(Long documentId, Long userId);

    /**
     * 取消点赞足迹
     * @param documentId
     * @param userId
     * @return
     */
    Integer cancelPraiseFoot(Long documentId, Long userId);
}
