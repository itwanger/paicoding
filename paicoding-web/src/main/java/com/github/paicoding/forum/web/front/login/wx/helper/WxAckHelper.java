package com.github.paicoding.forum.web.front.login.wx.helper;

import com.github.paicoding.forum.api.model.vo.user.wx.BaseWxMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtItemVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxImgTxtMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewAiReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuReplyArticleDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuReplyDTO;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.core.util.CodeGenerateUtil;
import com.github.paicoding.forum.service.chatai.service.ChatgptService;
import com.github.paicoding.forum.service.config.service.WxMenuService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.audit.UserShareRiskControlService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class WxAckHelper {
    @Autowired
    private LoginService sessionService;
    @Autowired
    private WxLoginHelper qrLoginHelper;
    @Autowired
    private UserShareRiskControlService userShareRiskControlService;

    @Autowired
    private ChatgptService chatgptService;

    @Autowired
    private WxMenuService wxMenuService;
    @Autowired
    private WxAsyncReplyHelper wxAsyncReplyHelper;

    /**
     * 返回自动响应的文本
     *
     * @return
     */
    public WxAckResult buildResponseBody(String eventType, String eventKey, String content, String fromUser) {
        // 返回的文本消息
        String textRes = null;
        // 返回的是图文消息
        List<WxImgTxtItemVo> imgTxtList = null;
        if (("subscribe".equalsIgnoreCase(eventType) || "scan".equalsIgnoreCase(eventType))
                && !CodeGenerateUtil.isVerifyCode(content)) {
            BaseWxMsgResVo configuredReply = buildConfiguredReply(wxMenuService.getSubscribeReply());
            if (configuredReply != null) {
                return WxAckResult.reply(configuredReply);
            }
            // 单纯的服务号订阅、扫码，而不是登录的场景，返回下面的提示信息
            textRes = "优秀的你一关注，二哥英俊的脸上就泛起了笑容。我这个废柴，既可以把程序人生写得风趣幽默，也可以把技术文章写得通俗易懂。\n" +
                    "\n" +
                    "可能是 2023 年最硬核的面试学习资料，内容涵盖 Java、Spring、MySQL、Redis、计算机网络、操作系统、消息队列、分布式等，60 万+字，300 张+手绘图，GitHub 星标 9000+，相信一定能够帮助到你。\n" +
                    "\n" +
                    "<a href=\"https://mp.weixin.qq.com/s/kGmZpxeV3lj04yjA0KRUXA\">[勾引]PDF 戳这里获取，手慢无！</a>\n" +
                    "\n" +
                    "没有什么使我停留——除了目的，纵然岸旁有玫瑰、有绿荫、有宁静的港湾，我是不系之舟。\n";
        }

        BaseWxMsgResVo configuredRuleReply = buildConfiguredReply(wxMenuService.matchKeywordReply(eventType, eventKey, content));
        if (configuredRuleReply != null) {
            return WxAckResult.reply(configuredRuleReply);
        }

        // 下面是关键词回复
        if (chatgptService.inChat(fromUser, content)) {
            try {
                textRes = chatgptService.chat(fromUser, content);
            } catch (Exception e) {
                log.error("派聪明 访问异常! content: {}", content, e);
                textRes = "派聪明 出了点小状况，请稍后再试!";
            }
        } else if ("加群".equalsIgnoreCase(content)) {
            WxImgTxtItemVo imgTxt = new WxImgTxtItemVo();
            imgTxt.setTitle("扫码加群");
            imgTxt.setDescription("加入技术派的技术交流群，卷起来！");
            imgTxt.setPicUrl("https://mmbiz.qpic.cn/mmbiz_jpg/sXFqMxQoVLGOyAuBLN76icGMb2LD1a7hBCoialjicOMsicvdsCovZq2ib1utmffHLjVlcyAX2UTmHoslvicK4Mg71Kyw/0?wx_fmt=jpeg");
            imgTxt.setUrl("https://mp.weixin.qq.com/s/aY5lkyKjLHWSUuEf1UT2yQ");
            imgTxtList = Arrays.asList(imgTxt);
        } else if ("admin".equalsIgnoreCase(content) || "后台".equals(content) || "002".equals(content)) {
            // admin后台登录，返回对应的用户名 + 密码
            textRes = "技术派后台游客登录账号\n-----------\n登录用户名: guest\n登录密码: 123456";
        } else if ("商务合作".equalsIgnoreCase(content)) {
            textRes = "商务合作（非诚勿扰）：请添加二哥微信 qing_geee 备注\"商务合作\"'";
        }
        // 微信公众号登录
        else if (CodeGenerateUtil.isVerifyCode(content)) {
            sessionService.autoRegisterWxUserInfo(fromUser);
            if (qrLoginHelper.login(content)) {
                textRes = "登录成功，开始愉快的玩耍技术派吧！\n\n" +
                        "🎉 欢迎来到技术派！\n" +
                        "👉 <a href=\"https://paicoding.com\">访问技术派官网</a> 👈\n\n" +
                        "在这里你可以：\n" +
                        "• 发布技术文章，分享你的经验\n" +
                        "• 学习优质内容，提升技术能力\n" +
                        "• 与技术爱好者交流互动";
                String riskTip = userShareRiskControlService.getHighRiskLoginTip(ReqInfoContext.getReqInfo().getUserId());
                if (StringUtils.isNotBlank(riskTip)) {
                    textRes += "\n\n风险提醒：" + riskTip;
                }
            } else {
                textRes = "验证码过期了，刷新登录页面重试一下吧";
            }
        } else {
            WxAckResult aiReply = buildAiFallbackReply(content, fromUser);
            if (aiReply != null) {
                return aiReply;
            }
            BaseWxMsgResVo configuredReply = buildFallbackReply();
            if (configuredReply != null) {
                return WxAckResult.reply(configuredReply);
            }
            textRes = "/:? 还在找其它资料么？\n" +
                    "\n" +
                    "[机智] 添加二哥的微信 itwanger 后，微信回复 “110”，即可获得 10 本校招/社招必刷八股文，以及技术派团队的原创手册《高并发手册》、《Spring 源码解析手册》、《设计模式手册》、《JVM 核心手册》、《Java 并发编程手册》、《架构选型手册》，工作面试两不误，工作面试两不误。\n" +
                    "\n" +
                    "商务合作/技术交流群：请添加二哥微信 itwanger";
        }

        if (textRes != null) {
            WxTxtMsgResVo vo = new WxTxtMsgResVo();
            vo.setContent(textRes);
            return WxAckResult.reply(vo);
        } else {
            WxImgTxtMsgResVo vo = new WxImgTxtMsgResVo();
            vo.setArticles(imgTxtList);
            vo.setArticleCount(imgTxtList.size());
            return WxAckResult.reply(vo);
        }
    }

    private BaseWxMsgResVo buildFallbackReply() {
        String strategy = StringUtils.defaultIfBlank(wxMenuService.getMessageFallbackStrategy(), "fixed_reply");
        if ("fixed_reply".equalsIgnoreCase(strategy)) {
            return buildConfiguredReply(wxMenuService.getDefaultReply());
        }
        return null;
    }

    private WxAckResult buildAiFallbackReply(String content, String fromUser) {
        String strategy = StringUtils.defaultIfBlank(wxMenuService.getMessageFallbackStrategy(), "fixed_reply");
        if (!"ai_reply".equalsIgnoreCase(strategy) || !Boolean.TRUE.equals(wxMenuService.getAiEnable())) {
            return null;
        }

        WxMenuPreviewAiReq req = new WxMenuPreviewAiReq();
        req.setContent(content);
        req.setAiEnable(Boolean.TRUE);
        req.setAiPrompt(wxMenuService.getAiPrompt());
        req.setAiProvider(wxMenuService.getAiProvider());

        try {
            // 被动回复只做快速确认，真正的AI结果通过客服消息异步补发给用户。
            AsyncUtil.callWithTimeLimit(300, TimeUnit.MILLISECONDS, () -> {
                wxAsyncReplyHelper.sendAiReply(fromUser, req);
                return Boolean.TRUE;
            });
            return WxAckResult.ackOnly();
        } catch (Exception e) {
            log.warn("触发微信 AI 异步回复失败, content={}", content, e);
            return null;
        }
    }

    private BaseWxMsgResVo buildConfiguredReply(WxMenuReplyDTO reply) {
        if (reply == null || StringUtils.isBlank(reply.getReplyType())) {
            return null;
        }

        if ("text".equalsIgnoreCase(reply.getReplyType())) {
            if (StringUtils.isBlank(reply.getContent())) {
                return null;
            }
            WxTxtMsgResVo vo = new WxTxtMsgResVo();
            vo.setContent(reply.getContent());
            return vo;
        }

        if ("news".equalsIgnoreCase(reply.getReplyType()) && reply.getArticles() != null && !reply.getArticles().isEmpty()) {
            WxImgTxtMsgResVo vo = new WxImgTxtMsgResVo();
            List<WxImgTxtItemVo> articles = reply.getArticles().stream()
                    .filter(item -> item != null && StringUtils.isNotBlank(item.getTitle()))
                    .map(this::toImgTxtItem)
                    .collect(Collectors.toList());
            if (articles.isEmpty()) {
                return null;
            }
            vo.setArticles(articles);
            vo.setArticleCount(articles.size());
            return vo;
        }

        return null;
    }

    private WxImgTxtItemVo toImgTxtItem(WxMenuReplyArticleDTO article) {
        WxImgTxtItemVo item = new WxImgTxtItemVo();
        item.setTitle(article.getTitle());
        item.setDescription(article.getDescription());
        item.setPicUrl(article.getPicUrl());
        item.setUrl(article.getUrl());
        return item;
    }

    @Data
    @AllArgsConstructor
    public static class WxAckResult {
        private BaseWxMsgResVo response;
        private boolean ackOnly;

        public static WxAckResult reply(BaseWxMsgResVo response) {
            return new WxAckResult(response, false);
        }

        public static WxAckResult ackOnly() {
            return new WxAckResult(null, true);
        }
    }
}
