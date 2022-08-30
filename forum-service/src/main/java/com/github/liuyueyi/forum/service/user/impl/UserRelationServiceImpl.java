package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.liueyueyi.forum.api.model.enums.FollowStateEnum;
import com.github.liueyueyi.forum.api.model.enums.FollowTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liuyueyi.forum.service.user.UserRelationService;
import com.github.liuyueyi.forum.service.user.converter.UserConverter;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UserRelationMapper userRelationMapper;

    @Resource
    private UserConverter userConverter;

    @Override
    public UserFollowListDTO getUserFollowList(Long userId, PageParam pageParam) {

        UserFollowListDTO userFollowListDTO = new UserFollowListDTO();
        List<UserFollowDTO> userRelationList = userRelationMapper.queryUserFollowList(userId, pageParam);
        if (userRelationList.isEmpty())  {
            return userFollowListDTO;
        }

        Boolean isMore = (userRelationList.size() == pageParam.getPageSize()) ? Boolean.TRUE : Boolean.FALSE;

        userFollowListDTO.setUserFollowList(userRelationList);
        userFollowListDTO.setIsMore(isMore);
        return userFollowListDTO;
    }

    @Override
    public UserFollowListDTO getUserFansList(Long userId, PageParam pageParam) {

        UserFollowListDTO userFollowListDTO = new UserFollowListDTO();
        List<UserFollowDTO> userRelationList = userRelationMapper.queryUserFansList(userId, pageParam);
        if (userRelationList.isEmpty())  {
            return userFollowListDTO;
        }

        Boolean isMore = (userRelationList.size() == pageParam.getPageSize()) ? Boolean.TRUE : Boolean.FALSE;

        userFollowListDTO.setUserFollowList(userRelationList);
        userFollowListDTO.setIsMore(isMore);
        return userFollowListDTO;
    }

    @Override
    public void saveUserRelation(UserRelationReq req) throws Exception {
        if (req.getUserRelationId() == null || req.getUserRelationId() == 0) {
            userRelationMapper.insert(userConverter.toDO(req));
            return;
        }

        UserRelationDO userRelationDO = userRelationMapper.selectById(req.getUserRelationId());
        if (userRelationDO == null) {
            throw new Exception("未查询到该用户关系");
        }
        userRelationMapper.updateById(userConverter.toDO(req));
    }
}
