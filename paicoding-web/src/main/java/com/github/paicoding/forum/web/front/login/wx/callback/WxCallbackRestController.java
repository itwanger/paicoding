package com.github.paicoding.forum.web.front.login.wx.callback;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.vo.user.wx.BaseWxMsgResVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgReqVo;
import com.github.paicoding.forum.api.model.vo.user.wx.WxTxtMsgResVo;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.pay.ThirdPayService;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.front.login.wx.helper.WxAckHelper;
import com.github.paicoding.forum.web.front.login.wx.helper.WxLoginHelper;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        String content = msg.getContent();
        if ("subscribe".equals(msg.getEvent()) || "scan".equalsIgnoreCase(msg.getEvent())) {
            String key = msg.getEventKey();
            if (StringUtils.isNotBlank(key) || key.startsWith("qrscene_")) {
                // 带参数的二维码，扫描、关注事件拿到之后，直接登录，省却输入验证码这一步
                // fixme 带参数二维码需要 微信认证，个人公众号无权限
                String code = key.substring("qrscene_".length());
                sessionService.autoRegisterWxUserInfo(msg.getFromUserName());
                qrLoginHelper.login(code);
                WxTxtMsgResVo res = new WxTxtMsgResVo();
                res.setContent("登录成功");
                fillResVo(res, msg);
                return res;
            }
        }

        BaseWxMsgResVo res = wxHelper.buildResponseBody(msg.getEvent(), content, msg.getFromUserName());
        fillResVo(res, msg);
        return res;
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
        return SpringUtil.getBeanOrNull(ThirdPayService.class).payCallback(request, new Function<Transaction, Boolean>() {
            @Override
            public Boolean apply(Transaction transaction) {
                log.info("微信支付回调执行业务逻辑 {}", transaction);
                String outTradeNo = transaction.getOutTradeNo();
                Long payId = IdUtil.getPayIdFromPayCode(outTradeNo);
                PayStatusEnum payStatus;
                switch (transaction.getTradeState()) {
                    case SUCCESS:
                        payStatus = PayStatusEnum.SUCCEED;
                        break;
                    case NOTPAY:
                        payStatus = PayStatusEnum.NOT_PAY;
                        break;
                    case USERPAYING:
                        payStatus = PayStatusEnum.PAYING;
                        break;
                    default:
                        payStatus = PayStatusEnum.FAIL;
                }
                Long payTime = transaction.getSuccessTime() != null ? DateUtil.wxDayToTimestamp(transaction.getSuccessTime()) : null;
                return articlePayService.updatePayStatus(payId, outTradeNo, payStatus, payTime, transaction.getTransactionId());
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
        return SpringUtil.getBeanOrNull(ThirdPayService.class).refundCallback(request, new Function<RefundNotification, Boolean>() {
            @Override
            public Boolean apply(RefundNotification refundNotification) {
                log.info("微信退款回调执行业务逻辑{}", refundNotification);
                return null;
            }
        });
    }
}
