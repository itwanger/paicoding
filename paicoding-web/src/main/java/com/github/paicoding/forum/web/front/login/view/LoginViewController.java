package com.github.paicoding.forum.web.front.login.view;

import com.github.paicoding.forum.web.front.login.QrLoginHelper;
import com.github.paicoding.forum.web.global.BaseViewController;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


/**
 * 用户注册、取消，登录、登出
 *
 * @author louzai
 * @date : 2022/8/3 10:56
 **/
@Controller
@Slf4j
public class LoginViewController extends BaseViewController {
    @Autowired
    private QrLoginHelper qrLoginHelper;

    /**
     * 客户端与后端建立扫描二维码的长连接
     *
     * @param code
     * @return
     */
    @GetMapping(path = "subscribe", produces = {org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter subscribe(@RequestParam(name = "id") String code) throws IOException {
        return qrLoginHelper.subscribe(code);
    }
}
