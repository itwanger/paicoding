package com.github.paicoding.forum.web.front.user.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.service.UserTransferService;
import com.github.paicoding.forum.web.front.login.zsxq.helper.ZsxqHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户账号迁移
 *
 * @author YiHui
 * @date 2025/9/29
 */
@Permission(role = UserRole.LOGIN)
@RestController
@RequestMapping("/user/api/transfer")
public class UserTransferController {

    @Autowired
    private UserTransferService userTransferService;

    @Autowired
    private ZsxqHelper zsxqHelper;

    /**
     * 用户名密码方式账号迁移
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @PostMapping("/userPwd")
    public ResVo<Long> transferByUserPwd(@RequestParam(name = "username") String username,
                                         @RequestParam(name = "password") String password,
                                         HttpServletResponse response) throws IOException {
        boolean ans = userTransferService.transferUser(username, password);
        return ans ? ResVo.ok(ReqInfoContext.getReqInfo().getUserId()) : ResVo.ok(0L);
    }

    @RequestMapping("/zsxq")
    public void transferByZsxq(HttpServletResponse response) throws IOException {
        String url = zsxqHelper.buildZsxqLoginUrl(ZsxqHelper.EXTRA_TAG_USER_TRANSFER);
        response.sendRedirect(url);
    }
}