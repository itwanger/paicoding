package com.github.liuyueyi.forum.service.user.service.relation;

import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liuyueyi.forum.core.util.NumUtil;
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
        List<UserFollowDTO> userRelationList = userRelationDao.queryUserFollowList(userId, pageParam);
        return buildRes(userRelationList, pageParam);
    }

    @Override
    public UserFollowListDTO getUserFansList(Long userId, PageParam pageParam) {
        List<UserFollowDTO> userRelationList = userRelationDao.queryUserFansList(userId, pageParam);
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
        if (NumUtil.nullOrZero(req.getUserRelationId())) {
            userRelationDao.save(UserConverter.toDO(req));
            return;
        }

        UserRelationDO userRelationDO = userRelationDao.getById(req.getUserRelationId());
        if (userRelationDO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "userRelationId=" + req.getUserRelationId());
        }
        userRelationDao.updateById(UserConverter.toDO(req));
    }
}
