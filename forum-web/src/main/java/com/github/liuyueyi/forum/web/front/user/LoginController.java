package com.github.liuyueyi.forum.web.front.user;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liueyueyi.forum.api.model.vo.user.wx.WxTxtMsgReqVo;
import com.github.liueyueyi.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.user.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 登录/登出的入口
 *
 * @author YiHui
 * @date 2022/8/15
 */
@Controller
public class LoginController {
    @Autowired
    private LoginService loginService;

    @ResponseBody
    @RequestMapping("login")
    public ResVo<Boolean> login(@RequestParam(name = "code") String code, HttpServletResponse response) {
        String session = loginService.login(code);
        if (StringUtils.isNotBlank(session)) {
            response.addCookie(new Cookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(true);
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登录码异常，请重新输入");
        }
    }

    @ResponseBody
    @Permission(role = UserRole.LOGIN)
    @RequestMapping("logout")
    public ResVo<Boolean> logOut() {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginService.logout(s.getSession()));
        return ResVo.ok(true);
    }

    /**
     * 微信的公众号接入 token 验证，即返回echostr的参数值
     *
     * @param request
     * @return
     */
    @GetMapping(path = "msg/callback")
    public String check(HttpServletRequest request) {
        String echoStr = request.getParameter("echostr");
        if (StringUtils.isNoneEmpty(echoStr)) {
            return echoStr;
        }
        return "";
    }

    /**
     * 微信的响应返回
     * 本地测试访问: curl -X POST 'http://localhost:8080/wx/callback' -H 'content-type:application/xml' -d '<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[login]]></Content><MsgId>11111111</MsgId></xml>' -i
     *
     * @param msg
     * @param request
     * @return
     */
    @PostMapping(path = "wx/callback",
            consumes = {"application/xml", "text/xml"},
            produces = "application/xml;charset=utf-8")
    @ResponseBody
    public Object callBack(@RequestBody WxTxtMsgReqVo msg, HttpServletRequest request) {
        String content = msg.getContent();
        WxTxtMsgResVo res = new WxTxtMsgResVo();
        res.setFromUserName(msg.getToUserName());
        res.setToUserName(msg.getFromUserName());
        res.setCreateTime(System.currentTimeMillis() / 1000);
        res.setMsgType("text");
        if (containLoginKey(content)) {
            res.setContent("登录验证码: 【" + loginService.getVerifyCode(msg.getFromUserName()) + "】 五分钟内有效");
        } else {
            res.setContent("输入关键词不对!");
        }
        return res;
    }

    private boolean containLoginKey(String msg) {
        if (StringUtils.isBlank(msg)) {
            return false;
        }

        msg = msg.trim();
        for (String key : LoginService.LOGIN_CODE_KEY) {
            if (msg.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }
}
