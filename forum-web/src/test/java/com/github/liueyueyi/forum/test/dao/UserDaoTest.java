package com.github.liueyueyi.forum.test.dao;

import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.service.user.service.UserService;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
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
    private UserService userService;

    @Test
    public void testUserHome() throws Exception {
        UserStatisticInfoDTO userHomeDTO = userService.queryUserInfoWithStatistic(1L);
        log.info("query userPageDTO: {}", userHomeDTO);
    }
}
