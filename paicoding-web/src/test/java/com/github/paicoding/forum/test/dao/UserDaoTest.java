package com.github.paicoding.forum.test.dao;

import com.github.paicoding.forum.test.BasicTest;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class UserDaoTest extends BasicTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUserHome() throws Exception {
        UserStatisticInfoDTO userHomeDTO = userService.queryUserInfoWithStatistic(1L);
        log.info("query userPageDTO: {}", userHomeDTO);
    }
}
