package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.net.HttpRequestHelper;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.RandUtil;
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
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.function.Function;

/**
 * @author YiHui
 * @date 2024/12/6
 */
@Slf4j
public abstract class AbsWxPayIntegration implements ThirdPayIntegrationApi {
    public WxPayConfig wxPayConfig;

    abstract public String createPayOrder(ThirdPayOrderReqBo payReq);

    /**
     * 唤起支付
     * <a href="https://pay.weixin.qq.com/docs/merchant/apis/jsapi-payment/jsapi-transfer-payment.html">JSAPI调起支付</a>
     *
     * @return
     */
    public PrePayInfoResBo createOrder(ThirdPayOrderReqBo payReq) {
        log.info("微信支付 >>>>>>>>>>>>>>>>> 请求：{}", JsonUtil.toStr(payReq));
        ThirdPayWayEnum payWay = payReq.getPayWay();
        String prePayId = createPayOrder(payReq);

        long now = System.currentTimeMillis();
        PrePayInfoResBo prePay = new PrePayInfoResBo();
        prePay.setOutTradeNo(payReq.getOutTradeNo());
        prePay.setAppId(wxPayConfig.getAppId());
        prePay.setPrePayId(prePayId);
        if (payWay == ThirdPayWayEnum.WX_H5) {
            // 官方说明有效期五分钟，我们这里设置一下有效期为四分之后，避免正好卡在失效的时间点
            prePay.setExpireTime(now + payWay.getExpireTimePeriod());
            return prePay;
        } else if (payWay == ThirdPayWayEnum.WX_NATIVE) {
            // 官方说明有效期为两小时，我们设置为1.8小时之后失效
            prePay.setExpireTime(now + payWay.getExpireTimePeriod());
            return prePay;
        } else if (payWay == ThirdPayWayEnum.WX_JSAPI) {
            // 官方说明有效期为两小时，我们设置为1.8小时之后失效
            prePay.setExpireTime(now + payWay.getExpireTimePeriod());
            String timeStamp = String.valueOf(now / 1000);
            //随机字符串,要求小于32位
            String nonceStr = RandUtil.random(30);
            String packageStr = "prepay_id=" + prePayId;

            // 不能去除'.append("\n")'，否则失败
            String signStr = wxPayConfig.getAppId() + "\n" +
                    timeStamp + "\n" +
                    nonceStr + "\n" +
                    packageStr + "\n";

            byte[] message = signStr.getBytes(StandardCharsets.UTF_8);
            try {
                Signature sign = Signature.getInstance("SHA256withRSA");
                sign.initSign(PemUtil.loadPrivateKeyFromString(wxPayConfig.getPrivateKeyContent()));
                sign.update(message);
                String signStrBase64 = Base64.getEncoder().encodeToString(sign.sign());

                // 拼装返回结果
                prePay.setNonceStr(nonceStr);
                prePay.setPrePackage(packageStr);
                prePay.setSignType("RSA");
                prePay.setTimeStamp(timeStamp);
                prePay.setPaySign(signStrBase64);
                return prePay;
            } catch (Exception e) {
                log.error("唤醒支付签名异常: {} - {}", prePayId, payReq.getOutTradeNo(), e);
                return null;
            }
        }
        return null;
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
        return new PayCallbackBo().setPayStatus(payStatus).setOutTradeNo(outTradeNo).setPayId(payId)
                .setSuccessTime(payTime);
    }

    /**
     * 微信回调
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Transactional
    @Override
    public ResponseEntity<?> payCallback(HttpServletRequest request, Function<PayCallbackBo, Boolean> payCallback) throws IOException {
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
        try {
            // 验签、解密并转换成 Transaction（返回参数对象）
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("微信支付回调 成功，解析: {}", JSON.toJSONString(transaction));
            // TODO 处理你的业务逻辑
            boolean ans = payCallback.apply(toBo(transaction));
            if (ans) {
                // 处理成功，返回 200 OK 状态码
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // 处理异常，返回 500 服务器内部异常 状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ValidationException e) {
            log.error("微信支付回调v3java失败=" + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * 微信退款回调
     * - 技术派目前没有实现退款流程，下面只是实现了回调，没有具体的业务场景
     *
     * @param request
     * @return
     */
    @Transactional
    public <T> ResponseEntity refundCallback(HttpServletRequest request, Function<T, Boolean> refundCallback) throws IOException {
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
