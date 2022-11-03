package com.github.liuyueyi.forum.service.user.service.relation;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.FollowStateEnum;
import com.github.liueyueyi.forum.api.model.enums.NotifyTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.liuyueyi.forum.core.util.MapUtils;
import com.github.liuyueyi.forum.core.util.SpringUtil;
import com.github.liuyueyi.forum.service.user.converter.UserConverter;
import com.github.liuyueyi.forum.service.user.repository.dao.UserRelationDao;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.service.UserRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户关系Service
 *
 * @author louzai
 * @date 2022-07-20
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
        // 发布关注事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.FOLLOW, userRelationDO));
    }
}
