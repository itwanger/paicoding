package com.github.paicoding.forum.web.front.login.zsxq.controller;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserZsxqLoginReq;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.core.util.StarNumberUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.UserTransferService;
import com.github.paicoding.forum.web.front.login.zsxq.helper.ZsxqHelper;
import com.github.paicoding.forum.web.front.login.zsxq.vo.ZsxqLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 知识星球登录
 *
 * @author YiHui
 * @date 2025/8/19
 */
@RestController
@Slf4j
public class ZsxqLoginController {
    @Autowired
    private ZsxqHelper zsxqHelper;

    @Autowired
    private LoginService loginService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserTransferService userTransferService;

    /**
     * 用户信息绑定
     *
     * @param useXqName true 表示使用星球的昵称/头像来更新技术派的用户信息
     * @return
     */
    @RequestMapping("zsxq/bind")
    public void autoBindZsxqUser(@RequestParam(name = "useXqName", required = false, defaultValue = "true") Boolean useXqName, HttpServletResponse response) throws IOException {
        String url = zsxqHelper.buildZsxqLoginUrl("" + useXqName);
        response.sendRedirect(url);
    }

    /**
     * 知识星球的回调
     *
     * @param login
     * @param response
     * @throws IOException
     */
    @RequestMapping("login/zsxq/callback")
    public void callbackZsxq(ZsxqLoginVo login, HttpServletResponse response) throws IOException {
        // 1. 首先进行签名校验
        if (!zsxqHelper.verifySignature(login)) {
            log.info("登录失败：{}", login);
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "请确认知识星球正常完成了系统授权登录哦~");
        }

        String starNumber = StarNumberUtil.formatStarNumber(login.getUser_number());
        // 2. 对于未登录的场景，执行星球登录
        if (ReqInfoContext.getReqInfo().getUser() == null) {
            String session = loginService.loginByZsxq(new UserZsxqLoginReq()
                    .setStarUserId(login.getUser_id())
                    .setUsername("zsxq_" + starNumber)
                    .setDisplayName(login.getUser_name())
                    .setStarNumber(starNumber)
                    .setAvatar(login.getUser_icon())
                    .setExpireTime(login.getExpire_time() * 1000L)
            );

            if (StringUtils.isNotBlank(session)) {
                // cookie中写入用户登录信息，用于身份识别
                response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
                response.sendRedirect("/");
            } else {
                response.sendError(403, "登录失败，请重试再试");
            }
            return;
        }

        // 3. 如果是通过知识星球进行账号迁移
        if (Objects.equals(login.getExtra(), ZsxqHelper.EXTRA_TAG_USER_TRANSFER)) {
            // 迁移完成之后，跳转到新的用户主页
            userTransferService.transferUser(starNumber);
        }

        // 4. 对于已登录场景，执行星球信息绑定
        userService.bindUserInfo(new UserZsxqLoginReq()
                .setUpdateUserInfo(BooleanUtils.toBoolean(login.getExtra()))
                .setUsername("zsxq_" + starNumber)
                .setDisplayName(login.getUser_name())
                .setStarNumber(starNumber)
                .setAvatar(login.getUser_icon())
                .setExpireTime(login.getExpire_time() * 1000L)
                .setStarUserId(login.getUser_id())
        );
        // 绑定成功
        response.sendRedirect("/user/home?userId=" + ReqInfoContext.getReqInfo().getUserId());
    }
}
