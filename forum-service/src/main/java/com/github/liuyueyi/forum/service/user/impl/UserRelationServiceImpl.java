package com.github.liuyueyi.forum.service.user.impl;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowDTO;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowListDTO;
import com.github.liuyueyi.forum.service.user.UserRelationService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

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
    public void deleteUserRelationById(Long id) {
        UserRelationDO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }
}
