package com.github.paicoding.forum.web.app.user.extend;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.service.user.service.UserRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户扩展服务
 *
 * @author YiHui
 * @date 2024/3/16
 */
@Service
public class AppUserServiceExtend {
    @Resource
    private UserRelationService userRelationService;

    /**
     * 查询粉丝列表
     *
     * @param userId
     * @param pageParam
     */
    public PageListVo<FollowUserInfoDTO> queryFansList(long userId, PageParam pageParam) {
        // 查询粉丝列表时，只能确定粉丝关注了userId，但是不能反向判断，因此需要再更新下映射关系，判断userId是否有关注这个用户
        PageListVo<FollowUserInfoDTO> fansList = userRelationService.getUserFansList(userId, pageParam);

        // 更新当前登录用户与这些用户列表的关系
        Long loginUserId = ReqInfoContext.getReqInfo().getUserId();
        if (!Objects.isNull(loginUserId) && !CollectionUtils.isEmpty(fansList.getList())) {
            userRelationService.updateUserFollowRelationId(fansList, loginUserId);
        }
        return fansList;
    }
}
