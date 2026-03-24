package com.github.paicoding.forum.web.front.login.wx.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewAiReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewAiResDTO;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.service.config.service.WxMenuService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 微信被动回复快速ACK后的异步补发
 *
 * @author Codex
 * @date 2026/3/24
 */
@Slf4j
@Component
public class WxAsyncReplyHelper {
    private static final String WX_CUSTOM_SEND_API = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";

    private final WxMenuService wxMenuService;
    private final WxLoginQrGenIntegration wxLoginQrGenIntegration;

    public WxAsyncReplyHelper(WxMenuService wxMenuService, WxLoginQrGenIntegration wxLoginQrGenIntegration) {
        this.wxMenuService = wxMenuService;
        this.wxLoginQrGenIntegration = wxLoginQrGenIntegration;
    }

    @Async
    public void sendAiReply(String openId, WxMenuPreviewAiReq req) {
        if (StringUtils.isBlank(openId) || req == null || StringUtils.isBlank(req.getContent())) {
            log.warn("异步发送微信AI回复失败, openId或请求参数为空, openId={}", openId);
            return;
        }

        try {
            WxMenuPreviewAiResDTO res = wxMenuService.previewAi(req);
            if (res == null || !Boolean.TRUE.equals(res.getSuccess()) || StringUtils.isBlank(res.getReplyText())) {
                log.warn("异步生成微信AI回复失败, openId={}, provider={}, errorMsg={}",
                        openId, req.getAiProvider(), res == null ? "previewAi返回null" : res.getErrorMsg());
                return;
            }
            sendTextMessage(openId, res.getReplyText(), true);
        } catch (Exception e) {
            log.error("异步发送微信AI回复异常, openId={}, content={}", openId, req.getContent(), e);
        }
    }

    public boolean sendTextMessage(String openId, String content, boolean aiMessage) {
        if (StringUtils.isBlank(openId) || StringUtils.isBlank(content)) {
            return false;
        }

        WxCustomMsgReq req = WxCustomMsgReq.buildText(openId, content, aiMessage);
        WxCustomMsgRes res = doSend(req);
        if (needRefreshToken(res)) {
            wxLoginQrGenIntegration.refreshAccessToken();
            res = doSend(req);
        }

        if (res == null) {
            log.error("发送微信客服消息失败, openId={}, 微信接口返回null", openId);
            return false;
        }
        if (!res.isSuccess()) {
            log.error("发送微信客服消息失败, openId={}, errCode={}, errMsg={}", openId, res.getErrCode(), res.getErrMsg());
            return false;
        }

        log.info("发送微信客服消息成功, openId={}", openId);
        return true;
    }

    private WxCustomMsgRes doSend(WxCustomMsgReq req) {
        String accessToken = wxLoginQrGenIntegration.getAccessToken();
        String url = String.format(WX_CUSTOM_SEND_API, accessToken);
        return HttpRequestHelper.postJsonData(url, req, WxCustomMsgRes.class);
    }

    private boolean needRefreshToken(WxCustomMsgRes res) {
        return res != null && "40001".equals(res.getErrCode());
    }

    @Data
    private static class WxCustomMsgReq {
        private String touser;
        private String msgtype;
        private Text text;
        private AiMsgContext aimsgcontext;

        static WxCustomMsgReq buildText(String openId, String content, boolean aiMessage) {
            WxCustomMsgReq req = new WxCustomMsgReq();
            req.setTouser(openId);
            req.setMsgtype("text");
            req.setText(new Text(content));
            if (aiMessage) {
                req.setAimsgcontext(new AiMsgContext(1));
            }
            return req;
        }
    }

    @Data
    private static class Text {
        private final String content;
    }

    @Data
    private static class AiMsgContext {
        @JsonProperty("is_ai_msg")
        private final Integer isAiMsg;
    }

    @Data
    private static class WxCustomMsgRes {
        @JsonProperty("errcode")
        private String errCode;
        @JsonProperty("errmsg")
        private String errMsg;

        boolean isSuccess() {
            return StringUtils.isBlank(errCode) || "0".equals(errCode);
        }
    }
}
