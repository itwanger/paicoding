package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowListDTO;
import com.github.liuyueyi.forum.service.user.UserRelationService;
import com.github.liuyueyi.forum.service.user.UserService;
import com.github.liuyueyi.forum.service.user.dto.UserHomeDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author YiHui
 * @date 2022/7/20
 */
@Slf4j
public class UserDaoTest extends BasicTest {

    @Autowired
    private UserRelationService userRelationService;

    @Autowired
    private UserService userService;

    @Test
    public void testUserRelation() {
        UserFollowListDTO userFollowListDTO = userRelationService.getUserFollowList(1L, PageParam.newPageInstance(1L, 10L));
        log.info("query userFollowDTOS: {}", userFollowListDTO);

        UserFollowListDTO userFansListDTO = userRelationService.getUserFansList(1L, PageParam.newPageInstance(1L, 10L));
        log.info("query userFansList: {}", userFansListDTO);
    }

    @Test
    public void testUser() throws Exception {
        UserHomeDTO userHomeDTO = userService.getUserHomeDTO(1L);
        log.info("query userPageDTO: {}", userHomeDTO);
    }
}
