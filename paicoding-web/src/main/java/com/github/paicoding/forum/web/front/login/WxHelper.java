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
            textRes = "优秀的你一关注，楼仔英俊的脸上就泛起了笑容[奸笑]。我这个废柴，既可以把程序人生写得风趣幽默，也可以把技术文章写得通俗易懂。\n" +
                    "\n" +
                    "这里整理了一份「 2023年超硬核面试备战手册」，内容涵盖计算机网络、操作系统、数据结构与算法、MySQL、Redis、Java、Spring、高并发等等\n" +
                    "\n" +
                    "<a href=\"https://mp.weixin.qq.com/s/NtXsheOHepLuGIII_sY7fw\">[勾引]速来，手慢无！</a>\n" +
                    "\n" +
                    "我从清晨走过，也拥抱夜晚的星辰，人生没有捷径，你我皆平凡，你好，陌生人，一起共勉。\n";
        }
        // 下面是关键词回复
        else if (chatgptService.inChat(fromUser, content)) {
            try {
                textRes = chatgptService.chat(fromUser, content);
            } catch (Exception e) {
                log.error("chatgpt 访问异常! content: {}", content, e);
                textRes = "chatgpt 出了点小状况，请稍后再试!";
            }
        } else if ("110".equalsIgnoreCase(content)) {
            textRes = "[机智] [10 本校招/社招必刷八股文] 链接: https://pan.baidu.com/s/1-ElSmMtaHXSl9bj8lChXQA?pwd=iw20 提取码: iw20";
        } else if ("119".equalsIgnoreCase(content) || "高并发".equalsIgnoreCase(content)) {
            textRes = "[机智] [高并发手册] 链接: https://pan.baidu.com/s/15UuFz__trjW2iLGugUiCIw?pwd=wwlm 提取码: wwlm";
        } else if ("120".equalsIgnoreCase(content) || "JVM".equalsIgnoreCase(content)) {
            textRes = "[机智] [JVM手册] 链接: https://pan.baidu.com/s/1b-YD5hbPNdJsWeEQTw7TSA?pwd=h66t 提取码: h66t";
        } else if ("122".equalsIgnoreCase(content) || "Spring".equalsIgnoreCase(content)) {
            textRes = "[机智] [6 本楼仔原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误。\n] 链接: https://pan.baidu.com/s/1mGkHxsWQPOlySIZm7i9FbA?pwd=0mje 提取码: 0mje\n";
        } else if ("资料".equalsIgnoreCase(content) || "pdf".equalsIgnoreCase(content) || "楼仔".equalsIgnoreCase(content) || "111".equalsIgnoreCase(content)) {
            textRes = "[机智] [6 本楼仔原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误。\n] 链接: https://pan.baidu.com/s/1mGkHxsWQPOlySIZm7i9FbA?pwd=0mje 提取码: 0mje\n" +
                    "\n" +
                    "[机智] [Java 实战演练 35 讲] 链接: https://pan.baidu.com/s/1JYOWbrgRVs-BOB-vx2gECw?pwd=jr15 提取码: jr15\n" +
                    "\n" +
                    "[机智] [10 本校招/社招必刷八股文] 链接: https://pan.baidu.com/s/1-ElSmMtaHXSl9bj8lChXQA?pwd=iw20 提取码: iw20";
        } else if ("123".equalsIgnoreCase(content)) {
            textRes = "[机智] 添加楼仔的微信「lml200701158」后，微信回复 “123”，即可获得 10 本校招/社招必刷八股文，以及 6 本楼仔原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误，工作面试两不误。";
        }

        // 下面是回复图文消息
        else if ("加群".equalsIgnoreCase(content)) {
            WxImgTxtItemVo imgTxt = new WxImgTxtItemVo();
            imgTxt.setTitle("扫码加群");
            imgTxt.setDescription("加入技术交流群，定期分享技术好文，卷起来！");
            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLGOyAuBLN76icGMb2LD1a7hBCoialjicOMsicvdsCovZq2ib1utmffHLjVlcyAX2UTmHoslvicK4Mg71Kyw/0?wx_fmt=jpeg");
            imgTxt.setUrl("https://mp.weixin.qq.com/s?__biz=Mzg3OTU5NzQ1Mw==&mid=2247489777&idx=1&sn=fe41b1d5b461213c1586befc602618ac&chksm=cf035a13f874d305c21c7dbdcc6ce0f8ffbc59c1fa2f8d436620017a676881d175b2f0af3306&token=466180380&lang=zh_CN#rd");
            imgTxtList = Arrays.asList(imgTxt);
        } /*else if ("职业规划".equalsIgnoreCase(content)) {
            WxImgTxtItemVo imgTxt = new WxImgTxtItemVo();
            imgTxt.setTitle("晋升 P7 了");
            imgTxt.setDescription("如何才能达到阿里 P7 水平，技术、业务和软技能，三者缺一不可，本文告诉你如何卷！");
            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLF2gXC2gl…RHHDRfQ5tiblDL8XbLF8I3iaNppofbwRGFA/0?wx_fmt=jpeg");
            imgTxt.setUrl("https://mp.weixin.qq.com/s?__biz=Mzg3OTU5NzQ1Mw==&mid=2247489759&idx=1&sn=c775e16329c13e1c563a6cff8c65e939&chksm=cf035a3df874d32b1a41294daa2e32cf73c2cb4e22b8e5a504ef6831d449cf7830284b12e309&token=466180380&lang=zh_CN#rd");


            WxImgTxtItemVo imgTxt2 = new WxImgTxtItemVo();
            imgTxt.setTitle("如何看待程序员35岁职业危机？");
            imgTxt.setDescription("作为程序员，你有过35岁焦虑么？如何看待程序员大龄危机，如何提前做好职业规划，我们一起聊聊~~");
            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLEOMH20jF…U8UmFWP4emV8b2pcL4icDlGT3wWZZFvaoIw/0?wx_fmt=jpeg");
            imgTxt.setUrl("https://mp.weixin.qq.com/s?__biz=Mzg3OTU5NzQ1Mw==&mid=2247491344&idx=1&sn=38e026846aac4604124354275676de90&chksm=cf035df2f874d4e405a7cd0599c5e2835a1aabde4c61a36b1cfbe8959683bdeedb4635e2b216&token=466180380&lang=zh_CN#rd");

            imgTxtList = new ArrayList<>();
            imgTxtList.add(imgTxt);
            imgTxtList.add(imgTxt2);
        }*/
        else if ("admin".equalsIgnoreCase(content) || "后台".equals(content) || "001".equals(content)) {
            // admin后台登录，返回对应的用户名 + 密码
            textRes = "技术派后台游客登录账号\n-----------\n登录用户名: guest\n登录密码: 123456";
        } else if ("商务合作".equalsIgnoreCase(content) ) {
            textRes = "商务合作：请添加楼仔微信「lml200701158」，备注\"商务合作\"'";
        }
        // 微信公众号登录
        else if (CodeGenerateUtil.isVerifyCode(content)) {
            String verifyCode = sessionService.autoRegisterAndGetVerifyCode(fromUser);
            if (qrLoginHelper.login(content, verifyCode)) {
                textRes = "登录成功";
            } else {
                textRes = "验证码过期了，刷新登录页面重试一下吧";
            }
        } else {
            textRes = "/:? 还在找其它资料么？\n" +
                    "\n" +
                    "[机智] 添加楼仔的微信「lml200701158」后，微信回复 “123”，即可获得 10 本校招/社招必刷八股文，以及 6 本楼仔原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误，工作面试两不误。\n" +
                    "\n" +
                    "商务合作/技术交流群：请添加楼仔微信「lml200701158」";
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
