package com.github.paicoding.forum.service.user.service.relation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.FollowStateEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.api.model.vo.user.UserRelationReq;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.user.converter.UserConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserRelationDao;
import com.github.paicoding.forum.service.user.repository.entity.UserRelationDO;
import com.github.paicoding.forum.service.user.service.UserRelationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户关系Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class UserRelationServiceImpl implements UserRelationService {
    @Resource
    private UserRelationDao userRelationDao;


    /**
     * 查询用户的关注列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    @Override
    public PageListVo<FollowUserInfoDTO> getUserFollowList(Long userId, PageParam pageParam) {
        List<FollowUserInfoDTO> userRelationList = userRelationDao.listUserFollows(userId, pageParam);
        return PageListVo.newVo(userRelationList, pageParam.getPageSize());
    }

    @Override
    public PageListVo<FollowUserInfoDTO> getUserFansList(Long userId, PageParam pageParam) {
        List<FollowUserInfoDTO> userRelationList = userRelationDao.listUserFans(userId, pageParam);
        return PageListVo.newVo(userRelationList, pageParam.getPageSize());
    }

    @Override
    public void updateUserFollowRelationId(PageListVo<FollowUserInfoDTO> followList, Long loginUserId) {
        if (loginUserId == null) {
            followList.getList().forEach(r -> {
                r.setRelationId(null);
                r.setFollowed(false);
            });
            return;
        }

        // 判断登录用户与给定的用户列表的关注关系
        Set<Long> userIds = followList.getList().stream().map(FollowUserInfoDTO::getUserId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<UserRelationDO> relationList = userRelationDao.listUserRelations(loginUserId, userIds);
        Map<Long, UserRelationDO> relationMap = MapUtils.toMap(relationList, UserRelationDO::getUserId, r -> r);
        followList.getList().forEach(follow -> {
            UserRelationDO relation = relationMap.get(follow.getUserId());
            if (relation == null) {
                follow.setRelationId(null);
                follow.setFollowed(false);
            } else if (Objects.equals(relation.getFollowState(), FollowStateEnum.FOLLOW.getCode())) {
                follow.setRelationId(relation.getId());
                follow.setFollowed(true);
            } else {
                follow.setRelationId(relation.getId());
                follow.setFollowed(false);
            }
        });
    }

    /**
     * 根据登录用户从给定用户列表中，找出已关注的用户id
     *
     * @param userIds    主用户列表
     * @param fansUserId 粉丝用户id
     * @return 返回fansUserId已经关注过的用户id列表
     */
    @Override
    public Set<Long> getFollowedUserId(List<Long> userIds, Long fansUserId) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptySet();
        }

        List<UserRelationDO> relationList = userRelationDao.listUserRelations(fansUserId, userIds);
        Map<Long, UserRelationDO> relationMap = MapUtils.toMap(relationList, UserRelationDO::getUserId, r -> r);
        return relationMap.values().stream().filter(s -> s.getFollowState().equals(FollowStateEnum.FOLLOW.getCode())).map(UserRelationDO::getUserId).collect(Collectors.toSet());
    }

    @Override
    public void saveUserRelation(UserRelationReq req) {
        // 查询是否存在
        UserRelationDO userRelationDO = userRelationDao.getUserRelationRecord(req.getUserId(), ReqInfoContext.getReqInfo().getUserId());
        if (userRelationDO == null) {
            userRelationDO = UserConverter.toDO(req);
            userRelationDao.save(userRelationDO);
            // 发布关注事件
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.FOLLOW, userRelationDO));
            return;
        }

        // 将是否关注状态重置
        userRelationDO.setFollowState(req.getFollowed() ? FollowStateEnum.FOLLOW.getCode() : FollowStateEnum.CANCEL_FOLLOW.getCode());
        userRelationDao.updateById(userRelationDO);
        // 发布关注、取消关注事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, req.getFollowed() ? NotifyTypeEnum.FOLLOW : NotifyTypeEnum.CANCEL_FOLLOW, userRelationDO));
    }

    public IPage<FollowUserInfoDTO> getUserFollowListPagination(Long userId, int currentPage, int pageSize) {

        return userRelationDao.listUserFollowsPagination(currentPage, pageSize, userId);
//        userRelationDOIPage.convert(UserConverter::toDTO);
    }

    public IPage<FollowUserInfoDTO> getUserFansListPagination(Long userId, int currentPage, int pageSize) {
        return userRelationDao.listUserFansPagination(currentPage, pageSize, userId);
    }
}
