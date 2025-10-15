package com.github.paicoding.forum.web.front.login.wx.callback;

import cn.hutool.core.util.NumberUtil;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.wx.BaseWxMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgReqVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.service.pay.PayServiceFactory;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import com.github.paicoding.forum.web.front.login.wx.helper.WxAckHelper;
import com.github.paicoding.forum.web.front.login.wx.helper.WxLoginHelper;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;

/**
 * 微信公众号登录相关
 *
 * @author YiHui
 * @date 2022/9/2
 */
@Slf4j
@RequestMapping(path = "wx")
@RestController
public class WxCallbackRestController {
    @Autowired
    private LoginService sessionService;
    @Autowired
    private WxLoginHelper qrLoginHelper;
    @Autowired
    private WxAckHelper wxHelper;
    @Autowired
    private ArticlePayService articlePayService;
    @Autowired
    private PayServiceFactory payServiceFactory;

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
    public BaseWxMsgResVo callBack(@RequestBody WxTxtMsgReqVo msg) {
        // 对于需要开启安全校验的场景，需要配置
        this.wxCallbackSecurityCheck();
        String code = msg.getContent();
        if ("subscribe".equals(msg.getEvent()) || "scan".equalsIgnoreCase(msg.getEvent())) {
            // 对于符号的逻辑，code需要从eventKey中获取
            String key = msg.getEventKey();
            if (StringUtils.isNotBlank(key)) {
                // 对于关注事件，key的格式为 qrscene_验证码； 对于扫码事件，key的格式就是 验证码
                if (key.startsWith("qrscene_")) {
                    code = key.substring(8);
                } else {
                    code = key;
                }
            }
        }

        if (directToLoginOcPai(code)) {
            // 命中校招派登录的场景
            return loginOcPai(msg);
        }

        // 执行技术派登录、用户响应问答的场景
        BaseWxMsgResVo res = wxHelper.buildResponseBody(msg.getEvent(), code, msg.getFromUserName());
        fillResVo(res, msg);
        return res;
    }


    /**
     * 对微信的回调进行安全校验
     */
    private void wxCallbackSecurityCheck() {
        String securityToken = SpringUtil.getBean(WxLoginProperties.class).getSecurityCheckToken();
        if (StringUtils.isBlank(securityToken)) {
            // 没有配置接口签名校验时，直接返回
            return;
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String sig = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        // 验证签名
        String toSign = timestamp + nonce + securityToken;
        if (!DigestUtils.sha1Hex(toSign).equals(sig)) {
            log.error("微信回调签名校验失败，请检查接口签名配置");
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS);
        }
    }


    /**
     * 当关键词命中下面的规则，表示登录校招派
     *
     * @param content
     * @return
     */
    private boolean directToLoginOcPai(String content) {
        return NumberUtil.isNumber(content) && content.length() == 4;
    }


    /**
     * oc使用的和技术派是同一个微信公众号进行授权登录，按照验证码的位数进行区分；我们在这里做一个路由转发
     *
     * @return
     */
    private BaseWxMsgResVo loginOcPai(WxTxtMsgReqVo msg) {
        return HttpRequestHelper.postJsonData(SpringUtil.getConfig("paicoding.openapi.oc-login-redirect-url"), msg, WxTxtMsgResVo.class);
    }

    private void fillResVo(BaseWxMsgResVo res, WxTxtMsgReqVo msg) {
        res.setFromUserName(msg.getToUserName());
        res.setToUserName(msg.getFromUserName());
        res.setCreateTime(System.currentTimeMillis() / 1000);
    }


    /**
     * 微信支付回调
     *
     * @param request
     * @return
     * @throws IOException
     */
    @PostMapping(path = "payNotify")
    public ResponseEntity<?> wxPayCallback(HttpServletRequest request) throws IOException {
        return payServiceFactory.getPayService(ThirdPayWayEnum.WX_NATIVE).payCallback(request, new Function<PayCallbackBo, Boolean>() {
            @Override
            public Boolean apply(PayCallbackBo transaction) {
                log.info("微信支付回调执行业务逻辑 {}", transaction);
                if (transaction.getOutTradeNo().startsWith("TEST-")) {
                    // TestController 中关于测试支付的回调逻辑时，我们只通过消息进行通知用户即可
                    long payUser = transaction.getPayId();
                    SpringUtil.getBean(NotifyService.class).notifyToUser(payUser, "您的一笔微信测试支付状态已更新为：" + transaction.getPayStatus().getMsg());
                    return true;
                }

                return articlePayService.updatePayStatus(transaction.getPayId(),
                        transaction.getOutTradeNo(),
                        transaction.getPayStatus(),
                        transaction.getSuccessTime(),
                        transaction.getThirdTransactionId());
            }
        });
    }


    /**
     * todo: 退款回调
     *
     * @return
     */
    @PostMapping(path = "refundNotify")
    public ResponseEntity<?> wxRefundCallback(HttpServletRequest request) throws IOException {
        return payServiceFactory.getPayService(ThirdPayWayEnum.WX_NATIVE)
                .refundCallback(request, new Function<RefundNotification, Boolean>() {
                    @Override
                    public Boolean apply(RefundNotification refundNotification) {
                        log.info("微信退款回调执行业务逻辑{}", refundNotification);
                        return null;
                    }
                });
    }
}
