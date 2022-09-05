package com.github.liuyueyi.forum.service.user.service.relation;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.FollowStateEnum;
import com.github.liueyueyi.forum.api.model.enums.NotifyTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liueyueyi.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liuyueyi.forum.core.util.SpringUtil;
import com.github.liuyueyi.forum.service.user.converter.UserConverter;
import com.github.liuyueyi.forum.service.user.repository.dao.UserRelationDao;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.service.UserRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

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


    @Override
    public UserFollowListDTO getUserFollowList(Long userId, PageParam pageParam) {
        List<UserFollowDTO> userRelationList = userRelationDao.listUserFollows(userId, pageParam);
        return buildRes(userRelationList, pageParam);
    }

    @Override
    public UserFollowListDTO getUserFansList(Long userId, PageParam pageParam) {
        List<UserFollowDTO> userRelationList = userRelationDao.listUserFans(userId, pageParam);
        return buildRes(userRelationList, pageParam);
    }

    private UserFollowListDTO buildRes(List<UserFollowDTO> records, PageParam param) {
        if (CollectionUtils.isEmpty(records)) {
            return UserFollowListDTO.emptyInstance();
        }

        UserFollowListDTO userFollowListDTO = new UserFollowListDTO();
        userFollowListDTO.setUserFollowList(records);
        userFollowListDTO.setIsMore(records.size() == param.getPageSize());
        return userFollowListDTO;
    }

    @Override
    public void saveUserRelation(UserRelationReq req) {
        // 查询是否存在
        UserRelationDO userRelationDO = userRelationDao.getUserRelationRecord(req.getUserId(), ReqInfoContext.getReqInfo().getUserId());
        if (userRelationDO == null) {
            userRelationDao.save(UserConverter.toDO(req));
            // 发布关注事件
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.FOLLOW, userRelationDO));
            return;
        }
        userRelationDO.setFollowState(req.getFollowed() ? FollowStateEnum.FOLLOW.getCode() : FollowStateEnum.CANCEL_FOLLOW.getCode());
        userRelationDao.updateById(userRelationDO);
        // 发布关注事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.FOLLOW, userRelationDO));
    }
}
