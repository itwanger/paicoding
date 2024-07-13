package com.github.paicoding.forum.test.user;

import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.test.BasicTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class UserServiceTest extends BasicTest {

    @Autowired
    private UserService userService;

    /**
     * 注册一个用户
     */
    @Test
    public void testRegister() {
        UserInfoSaveReq save = new UserInfoSaveReq();
        save.setUserId(1L);
        save.setUserName("一灰灰");
        save.setPhoto("https://spring.hhui.top/spring-blog/css/images/avatar.jpg");
        save.setCompany("xm");
        save.setPosition("java");
        save.setProfile("码农");
        userService.saveUserInfo(save);
    }

}
