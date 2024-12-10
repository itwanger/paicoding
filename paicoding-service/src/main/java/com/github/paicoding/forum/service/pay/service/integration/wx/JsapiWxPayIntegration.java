
package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.RandUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;

/**
 * @author YiHui
 * @date 2024/12/4
 */
@Slf4j
@Service
@ConditionalOnBean(WxPayConfig.class)
public class JsapiWxPayIntegration extends AbsWxPayIntegration {
    private JsapiService jsapiService;

    public JsapiWxPayIntegration(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();
        jsapiService = new JsapiService.Builder().config(config).build();
    }

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return ThirdPayWayEnum.WX_JSAPI == payWay;
    }


    /**
     * jsApi微信支付 -- 适用于小程序、公众号等方式的支付场景: 需要拿到用户的openId
     */
    public String createPayOrder(ThirdPayOrderReqBo payReq) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(payReq.getDescription());
        request.setNotifyUrl(wxPayConfig.getPayNotifyUrl());
        request.setOutTradeNo(payReq.getOutTradeNo());

        Amount amount = new Amount();
        amount.setTotal(payReq.getTotal());
        request.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(payReq.getOpenId());
        request.setPayer(payer);

        log.info("微信JsApi下单, 请求参数: {}", JsonUtil.toStr(request));
        com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse response = jsapiService.prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getPrepayId());
        return response.getPrepayId();
    }

    @Override
    protected PrePayInfoResBo buildPayInfo(ThirdPayOrderReqBo payReq, String prePayId) {
        PrePayInfoResBo payRes = super.buildPayInfo(payReq, prePayId);
        long now = System.currentTimeMillis();
        // 官方说明有效期为两小时，我们设置为1.8小时之后失效
        payRes.setExpireTime(now + ThirdPayWayEnum.WX_JSAPI.getExpireTimePeriod());
        String timeStamp = String.valueOf(now / 1000);
        //随机字符串,要求小于32位
        String nonceStr = RandUtil.random(30);
        String packageStr = "prepay_id=" + payRes.getPrePayId();

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
            payRes.setNonceStr(nonceStr);
            payRes.setPrePackage(packageStr);
            payRes.setSignType("RSA");
            payRes.setTimeStamp(timeStamp);
            payRes.setPaySign(signStrBase64);
            return payRes;
        } catch (Exception e) {
            log.error("唤醒支付签名异常: {} - {}", payRes.getPrePayId(), payRes.getOutTradeNo(), e);
            return null;
        }
    }

    public void closeOrder(String outTradeNo) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        jsapiService.closeOrder(closeRequest);
    }


    public PayCallbackBo queryOrder(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(outTradeNo);
        Transaction transaction = jsapiService.queryOrderByOutTradeNo(request);
        return toBo(transaction);
    }
}
