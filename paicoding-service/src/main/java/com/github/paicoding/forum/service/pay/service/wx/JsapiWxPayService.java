
package com.github.paicoding.forum.service.pay.service.wx;

import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;

/**
 * @author YiHui
 * @date 2024/12/4
 */
@Slf4j
public class JsapiWxPayService {
    private static volatile JsapiService jsapiService;


    /**
     * jsApi微信支付 -- 适用于小程序、公众号等方式的支付场景: 需要拿到用户的openId
     */
    public static String jsApiOrder(ThirdPayOrderReqBo payReq, WxPayConfig wxPayConfig) {
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
        com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse response = getJsapiService(wxPayConfig).prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getPrepayId());
        return response.getPrepayId();
    }

    public static void closeOrder(String outTradeNo, WxPayConfig wxPayConfig) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        getJsapiService(wxPayConfig).closeOrder(closeRequest);
    }


    public static Transaction queryOrder(String outTradeNo, WxPayConfig wxPayConfig) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(outTradeNo);
        return getJsapiService(wxPayConfig).queryOrderByOutTradeNo(request);
    }

    private static JsapiService getJsapiService(WxPayConfig wxPayConfig) {
        if (jsapiService == null) {
            synchronized (JsapiWxPayService.class) {
                if (jsapiService == null) {
                    Config config = new RSAAutoCertificateConfig.Builder()
                            .merchantId(wxPayConfig.getMerchantId())
                            .privateKey(wxPayConfig.getPrivateKeyContent())
                            .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                            .apiV3Key(wxPayConfig.getApiV3Key())
                            .build();
                    jsapiService = new JsapiService.Builder().config(config).build();
                }
            }
        }
        return jsapiService;
    }
}
