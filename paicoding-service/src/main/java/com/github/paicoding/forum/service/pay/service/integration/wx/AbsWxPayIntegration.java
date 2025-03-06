package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.integration.ThirdPayIntegrationApi;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * @author YiHui
 * @date 2024/12/6
 */
@Slf4j
public abstract class AbsWxPayIntegration implements ThirdPayIntegrationApi {
    public WxPayConfig wxPayConfig;

    public abstract String createPayOrder(ThirdPayOrderReqBo payReq);

    /**
     * 补齐支付信息
     *
     * @param payReq   支付请求参数
     * @param prePayId 微信返回的支付唤起code
     */
    protected PrePayInfoResBo buildPayInfo(ThirdPayOrderReqBo payReq, String prePayId) {
        // 结果封装返回
        ThirdPayWayEnum payWay = payReq.getPayWay();
        PrePayInfoResBo prePay = new PrePayInfoResBo();
        prePay.setPayWay(payWay);
        prePay.setOutTradeNo(payReq.getOutTradeNo());
        prePay.setAppId(wxPayConfig.getAppId());
        prePay.setPrePayId(prePayId);
        prePay.setExpireTime(System.currentTimeMillis() + payWay.getExpireTimePeriod());
        return prePay;
    }

    /**
     * 唤起支付
     * <a href="https://pay.weixin.qq.com/docs/merchant/apis/jsapi-payment/jsapi-transfer-payment.html">JSAPI调起支付</a>
     *
     * @return
     */
    public PrePayInfoResBo createOrder(ThirdPayOrderReqBo payReq) {
        log.info("微信支付 >>>>>>>>>>>>>>>>> 请求：{}", JsonUtil.toStr(payReq));
        // 微信下单
        String prePayId = createPayOrder(payReq);
        return buildPayInfo(payReq, prePayId);
    }

    protected PayCallbackBo toBo(Transaction transaction) {
        String outTradeNo = transaction.getOutTradeNo();
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
        Long payId = IdUtil.getPayIdFromPayCode(outTradeNo);
        Long payTime = transaction.getSuccessTime() != null ? DateUtil.wxDayToTimestamp(transaction.getSuccessTime()) : null;
        return new PayCallbackBo()
                .setPayStatus(payStatus)
                .setOutTradeNo(outTradeNo)
                .setPayId(payId)
                .setThirdTransactionId(transaction.getTransactionId())
                .setSuccessTime(payTime);
    }

    @Override
    public PayCallbackBo payCallback(HttpServletRequest request) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .body(HttpRequestHelper.readReqData(request))
                .build();
        log.info("微信回调v3 >>>>>>>>>>>>>>>>> {}", JSONObject.toJSONString(requestParam));

        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();

        NotificationParser parser = new NotificationParser(config);
        // 验签、解密并转换成 Transaction（返回参数对象）
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        log.info("微信支付回调 成功，解析: {}", JSON.toJSONString(transaction));
        return toBo(transaction);
    }

    /**
     * 微信退款回调
     * - 技术派目前没有实现退款流程，下面只是实现了回调，没有具体的业务场景
     *
     * @param request
     * @return
     */
    @Transactional
    public <T> ResponseEntity refundCallback(HttpServletRequest request, Function<T, Boolean> refundCallback) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .body(HttpRequestHelper.readReqData(request))
                .build();
        log.info("微信退款回调v3 >>>>>>>>>>>>>>>>> {}", JSONObject.toJSONString(requestParam));

        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();

        NotificationParser parser = new NotificationParser(config);

        try {
            // 验签、解密并转换成 Transaction（返回参数对象）
            RefundNotification refundNotify = parser.parse(requestParam, RefundNotification.class);
            log.info("微信退款回调 成功，解析: {}", JSON.toJSONString(refundNotify));
            boolean ans = refundCallback.apply((T) refundNotify);
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ValidationException e) {
            log.error("微信退款回调v3java失败=" + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
