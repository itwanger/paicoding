package com.github.liuyueyi.forum.web.global;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.liuyueyi.forum.core.util.NumUtil;
import com.github.liuyueyi.forum.service.notify.service.NotifyService;
import com.github.liuyueyi.forum.service.user.service.LoginService;
import com.github.liuyueyi.forum.web.config.GlobalViewConfig;
import com.github.liuyueyi.forum.web.global.vo.GlobalVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Slf4j
@Service
public class GlobalInitService {
    @Value("${env.name}")
    private String env;
    @Autowired
    private LoginService loginService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    @Resource
    private NotifyService notifyService;

    /**
     * 全局属性配置
     */
    public GlobalVo globalAttr() {
        GlobalVo vo = new GlobalVo();
        vo.setEnv(env);
        vo.setSiteInfo(globalViewConfig);
        try {
            if (ReqInfoContext.getReqInfo() != null && NumUtil.upZero(ReqInfoContext.getReqInfo().getUserId())) {
                vo.setIsLogin(true);
                vo.setUser(ReqInfoContext.getReqInfo().getUser());
                vo.setMsgNum(ReqInfoContext.getReqInfo().getMsgNum());
            } else {
                vo.setIsLogin(false);
            }

            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request.getRequestURI().startsWith("/column")) {
                vo.setCurrentDomain("column");
            } else {
                vo.setCurrentDomain("article");
            }
        } catch (Exception e) {
            log.error("loginCheckError:", e);
        }
        return vo;
    }

    /**
     * 初始化用户信息
     *
     * @param reqInfo
     */
    public void initLoginUser(ReqInfoContext.ReqInfo reqInfo) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request.getCookies() == null) {
            return;
        }
        for (Cookie cookie : request.getCookies()) {
            if (LoginService.SESSION_KEY.equalsIgnoreCase(cookie.getName())) {
                String session = cookie.getValue();
                BaseUserInfoDTO user = loginService.getUserBySessionId(session);
                reqInfo.setSession(session);
                if (user != null) {
                    reqInfo.setUserId(user.getUserId());
                    reqInfo.setUser(user);
                    reqInfo.setMsgNum(notifyService.queryUserNotifyMsgCount(user.getUserId()));
                }
                return;
            }
        }
    }
}
