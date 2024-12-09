
package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * @author YiHui
 * @date 2024/12/4
 */
@Slf4j
@Service
@ConditionalOnBean(WxPayConfig.class)
public class JsapiWxPayIntegration extends AbsWxPayIntegration {
    private JsapiService jsapiService;

    private final WxPayConfig wxPayConfig;

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
