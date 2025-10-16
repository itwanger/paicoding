package com.github.paicoding.forum.web.front.login.wx.controller;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.core.mdc.MdcDot;
import com.github.paicoding.forum.web.front.login.wx.helper.WxLoginHelper;
import com.github.paicoding.forum.web.front.login.wx.vo.WxLoginVo;
import com.github.paicoding.forum.web.global.BaseViewController;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.util.NumUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 公众号登陆的长连接控制器
 *
 * @author louzai
 * @date : 2022/8/3 10:56
 **/
@Controller
@Slf4j
public class WxLoginController extends BaseViewController {
    @Autowired
    private WxLoginHelper qrLoginHelper;

    /**
     * 客户端与后端建立扫描二维码的长连接
     *
     * @return
     */
    @MdcDot
    @ResponseBody
    @GetMapping(path = "subscribe", produces = {org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter subscribe(String deviceId) throws IOException {
        return qrLoginHelper.subscribe();
    }

    @GetMapping(path = "/login/fetch")
    @ResponseBody
    public String resendCode(String deviceId) throws IOException {
        return qrLoginHelper.resend();
    }

    /**
     * 刷新验证码
     *
     * @return
     * @throws IOException
     */
    @MdcDot
    @GetMapping(path = "/login/refresh")
    @ResponseBody
    public ResVo<WxLoginVo> refresh(String deviceId) throws IOException {
        WxLoginVo vo = new WxLoginVo();
        String code = qrLoginHelper.refreshCode();
        if (StringUtils.isBlank(code)) {
            // 刷新失败，之前的连接已失效，重新建立连接
            vo.setCode(code);
            vo.setReconnect(true);
        } else {
            vo.setCode(code);
            vo.setReconnect(false);
        }
        return ResVo.ok(vo);
    }

    /**
     * 检查登录状态
     * 用于移动端扫码登录后，页面重新可见时检查是否已登录
     *
     * @return 登录状态信息
     */
    @MdcDot
    @ResponseBody
    @GetMapping(path = "/login/status")
    public ResVo<Map<String, Object>> loginStatus() {
        Map<String, Object> result = new HashMap<>();

        // 检查用户是否已登录
        boolean isLogin = ReqInfoContext.getReqInfo() != null
                && NumUtil.upZero(ReqInfoContext.getReqInfo().getUserId());

        result.put("loggedIn", isLogin);

        if (isLogin) {
            // 如果已登录，返回用户基本信息
            result.put("userId", ReqInfoContext.getReqInfo().getUserId());
            result.put("username", ReqInfoContext.getReqInfo().getUser().getUserName());
            result.put("photo", ReqInfoContext.getReqInfo().getUser().getPhoto());
        }

        return ResVo.ok(result);
    }
}
