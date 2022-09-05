package com.github.liuyueyi.forum.web.front.login.rest;

import com.github.liueyueyi.forum.api.model.vo.user.wx.WxTxtMsgReqVo;
import com.github.liueyueyi.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.liuyueyi.forum.service.user.service.LoginService;
import com.github.liuyueyi.forum.web.front.login.QrLoginHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 微信公众号登录相关
 *
 * @author YiHui
 * @date 2022/9/2
 */
@RequestMapping(path = "wx")
@RestController
public class WxRestController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private QrLoginHelper qrLoginHelper;

    /**
     * 微信的公众号接入 token 验证，即返回echostr的参数值
     *
     * @param request
     * @return
     */
    @GetMapping(path = "callback")
    public String check(HttpServletRequest request) {
        String echoStr = request.getParameter("echostr");
        if (StringUtils.isNoneEmpty(echoStr)) {
            return echoStr;
        }
        return "";
    }

    /**
     * fixme: 需要做防刷校验
     * 微信的响应返回
     * 本地测试访问: curl -X POST 'http://localhost:8080/wx/callback' -H 'content-type:application/xml' -d '<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[login]]></Content><MsgId>11111111</MsgId></xml>' -i
     *
     * @param msg
     * @return
     */
    @PostMapping(path = "callback",
            consumes = {"application/xml", "text/xml"},
            produces = "application/xml;charset=utf-8")
    public WxTxtMsgResVo callBack(@RequestBody WxTxtMsgReqVo msg) throws IOException {
        String content = msg.getContent();
        WxTxtMsgResVo res = new WxTxtMsgResVo();
        res.setFromUserName(msg.getToUserName());
        res.setToUserName(msg.getFromUserName());
        res.setCreateTime(System.currentTimeMillis() / 1000);
        res.setMsgType("text");
        if (loginSymbol(content)) {
            res.setContent("登录验证码: 【" + loginService.getVerifyCode(msg.getFromUserName()) + "】 五分钟内有效");
        } else if (NumberUtils.isDigits(content)){
            String verifyCode = loginService.getVerifyCode(msg.getFromUserName());
            qrLoginHelper.login(content, verifyCode);
        } else {
            res.setContent("加群：添加群主微信（lml200701158），备注（一灰灰blog）; 学习资料：全部收集在 https://hhui.top 个人站点");
        }
        return res;
    }

    /**
     * 判断是否为登录指令，后续扩展其他的响应
     *
     * @param msg
     * @return
     */
    private boolean loginSymbol(String msg) {
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
