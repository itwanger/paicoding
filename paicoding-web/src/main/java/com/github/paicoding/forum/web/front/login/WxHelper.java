package com.github.paicoding.forum.web.front.login;

import com.github.paicoding.forum.api.model.vo.user.wx.BaseWxMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtItemVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.paicoding.forum.core.util.CodeGenerateUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.service.chatai.service.ChatgptService;
import com.github.paicoding.forum.service.user.service.LoginOutService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class WxHelper {
    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx4a128c315d9b1228&secret=077e2d92dee69f04ba6d53a0ef4459f9";

    public static final String QR_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=";
    /**
     * 访问token
     */
    public static volatile String token = "";

    /**
     * 失效时间
     */
    public static volatile long expireTime = 0L;

    @Autowired
    private LoginOutService sessionService;
    @Autowired
    private QrLoginHelper qrLoginHelper;

    @Autowired
    private ChatgptService chatgptService;

    private RestTemplate restTemplate;

    public WxHelper() {
        restTemplate = new RestTemplate();
    }

    private synchronized void doGetToken() {
        ResponseEntity<HashMap> entity = restTemplate.getForEntity(ACCESS_TOKEN_URL, HashMap.class);
        HashMap data = entity.getBody();
        log.info("getToken:{}", JsonUtil.toStr(entity));
        token = (String) data.get("access_token");
        int expire = (int) data.get("expires_in");
        // 提前至十分钟失效
        expireTime = System.currentTimeMillis() / 1000 + expire - 600;
    }

    public String autoUpdateAccessToken() {
        if (StringUtils.isBlank(token) || System.currentTimeMillis() / 1000 >= expireTime) {
            doGetToken();
        }
        return token;
    }

    /**
     * 获取带参数的登录二维码地址
     *
     * @param code
     * @return
     */
    public String getLoginQrCode(String code) {
        String url = QR_CREATE_URL + autoUpdateAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> params = MapUtils.create("action_name", "QR_LIMIT_SCENE",
                "expire_seconds", 300,
                "action_info", MapUtils.create("scene", MapUtils.create("scene_str", code)));
        HttpEntity<String> request = new HttpEntity<>(JsonUtil.toStr(params), headers);

        Map ans = restTemplate.postForObject(url, request, HashMap.class);
        String qrcode = (String) ans.get("url");
        return qrcode;
    }


    /**
     * 返回自动响应的文本
     *
     * @return
     */
    public BaseWxMsgResVo buildResponseBody(String eventType, String content, String fromUser) {
        // 返回的文本消息
        String textRes = null;
        // 返回的是图文消息
        List<WxImgTxtItemVo> imgTxtList = null;
        if ("subscribe".equalsIgnoreCase(eventType)) {
            // 订阅
            textRes = "优秀的你一关注，二哥英俊的脸上就泛起了笑容。我这个废柴，既可以把程序人生写得风趣幽默，也可以把技术文章写得通俗易懂。\n" +
                    "\n" +
                    "可能是 2023 年最硬核的面试学习资料，内容涵盖 Java、Spring、MySQL、Redis、计算机网络、操作系统、消息队列、分布式等，60 万+字，300 张+手绘图，GitHub 星标 9000+，相信一定能够帮助到你。\n" +
                    "\n" +
                    "<a href=\"https://mp.weixin.qq.com/s/kGmZpxeV3lj04yjA0KRUXA\">[勾引]PDF 戳这里获取，手慢无！</a>\n" +
                    "\n" +
                    "没有什么使我停留——除了目的，纵然岸旁有玫瑰、有绿荫、有宁静的港湾，我是不系之舟。\n";
        }
        // 下面是关键词回复
        else if (chatgptService.inChat(fromUser, content)) {
            try {
                textRes = chatgptService.chat(fromUser, content);
            } catch (Exception e) {
                log.error("派聪明 访问异常! content: {}", content, e);
                textRes = "派聪明 出了点小状况，请稍后再试!";
            }
        }

        // 下面是回复图文消息
        else if ("加群".equalsIgnoreCase(content)) {
            WxImgTxtItemVo imgTxt = new WxImgTxtItemVo();
            imgTxt.setTitle("扫码加群");
            imgTxt.setDescription("加入技术派的技术交流群，卷起来！");
            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLGOyAuBLN76icGMb2LD1a7hBCoialjicOMsicvdsCovZq2ib1utmffHLjVlcyAX2UTmHoslvicK4Mg71Kyw/0?wx_fmt=jpeg");
            imgTxt.setUrl("https://mp.weixin.qq.com/s/aY5lkyKjLHWSUuEf1UT2yQ");
            imgTxtList = Arrays.asList(imgTxt);
        } else if ("admin".equalsIgnoreCase(content) || "后台".equals(content) || "002".equals(content)) {
            // admin后台登录，返回对应的用户名 + 密码
            textRes = "技术派后台游客登录账号\n-----------\n登录用户名: guest\n登录密码: 123456";
        } else if ("商务合作".equalsIgnoreCase(content) ) {
            textRes = "商务合作（非诚勿扰）：请添加二哥微信 qing_geee 备注\"商务合作\"'";
        }
        // 微信公众号登录
        else if (CodeGenerateUtil.isVerifyCode(content)) {
            String verifyCode = sessionService.autoRegisterAndGetVerifyCode(fromUser);
            if (qrLoginHelper.login(content, verifyCode)) {
                textRes = "登录成功，开始愉快的玩耍技术派吧！";
            } else {
                textRes = "验证码过期了，刷新登录页面重试一下吧";
            }
        } else {
            textRes = "/:? 还在找其它资料么？\n" +
                    "\n" +
                    "[机智] 添加二哥的微信 itwanger 后，微信回复 “110”，即可获得 10 本校招/社招必刷八股文，以及技术派团队的原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误，工作面试两不误。\n" +
                    "\n" +
                    "商务合作/技术交流群：请添加二哥微信 itwanger";
        }

        if (textRes != null) {
            WxTxtMsgResVo vo = new WxTxtMsgResVo();
            vo.setContent(textRes);
            return vo;
        } else {
            WxImgTxtMsgResVo vo = new WxImgTxtMsgResVo();
            vo.setArticles(imgTxtList);
            vo.setArticleCount(imgTxtList.size());
            return vo;
        }
    }
}
