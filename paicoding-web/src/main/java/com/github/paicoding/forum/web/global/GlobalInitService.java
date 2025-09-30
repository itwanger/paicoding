package com.github.paicoding.forum.web.global;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.seo.Seo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.sitemap.service.SitemapService;
import com.github.paicoding.forum.service.statistics.service.UserStatisticService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import com.github.paicoding.forum.web.global.vo.GlobalVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

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
    private UserService userService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    @Resource
    private NotifyService notifyService;

    @Resource
    private SeoInjectService seoInjectService;

    @Resource
    private UserStatisticService userStatisticService;

    @Resource
    private SitemapService sitemapService;

    @Resource
    private WxLoginProperties wxLoginProperties;

    /**
     * 全局属性配置
     */
    public GlobalVo globalAttr() {
        GlobalVo vo = new GlobalVo();
        vo.setEnv(env);
        vo.setSiteInfo(globalViewConfig);
        vo.setOnlineCnt(userStatisticService.getOnlineUserCnt());
        vo.setSiteStatisticInfo(sitemapService.querySiteVisitInfo(null, null));
        vo.setTodaySiteStatisticInfo(sitemapService.querySiteVisitInfo(LocalDate.now(), null));
        vo.setLoginQrType(wxLoginProperties.getLoginQrType());

        if (ReqInfoContext.getReqInfo() == null || ReqInfoContext.getReqInfo().getSeo() == null || CollectionUtils.isEmpty(ReqInfoContext.getReqInfo().getSeo().getOgp())) {
            Seo seo = seoInjectService.defaultSeo();
            vo.setOgp(seo.getOgp());
            vo.setJsonLd(JSONUtil.toJsonStr(seo.getJsonLd()));
        } else {
            Seo seo = ReqInfoContext.getReqInfo().getSeo();
            vo.setOgp(seo.getOgp());
            vo.setJsonLd(JSONUtil.toJsonStr(seo.getJsonLd()));
        }

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
            } else if (request.getRequestURI().startsWith("/chat")) {
                vo.setCurrentDomain("chat");
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

        List<Cookie> list = SessionUtil.findCookiesByName(request, LoginService.SESSION_KEY);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Cookie ck : list) {
            if (initLoginUser(ck.getValue(), reqInfo)) {
                // 成功登录
                return;
            } else {
                // 未登录，直接删除
                SessionUtil.delCookie(ck);
            }
        }
    }

    public boolean initLoginUser(String session, ReqInfoContext.ReqInfo reqInfo) {
        BaseUserInfoDTO user = userService.getAndUpdateUserIpInfoBySessionId(session, null);
        if (user != null) {
            reqInfo.setSession(session);
            reqInfo.setUserId(user.getUserId());
            reqInfo.setUser(user);
            reqInfo.setMsgNum(notifyService.queryUserNotifyMsgCount(user.getUserId()));
            return true;
        }
        return false;
    }
}
