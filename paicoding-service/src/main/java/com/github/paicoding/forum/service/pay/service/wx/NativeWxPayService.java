package com.github.paicoding.forum.service.pay.service.wx;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.SceneInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @author YiHui
 * @date 2024/12/4
 */
@Slf4j
public class NativeWxPayService {
    private static volatile NativePayService nativePayService;


    /**
     * native 支付，生成扫描支付二维码唤起微信支付页面
     *
     * @return 形如 wx://xxx 的支付二维码
     */
    public static String nativeApiOrder(ThirdPayOrderReqBo payReq, WxPayConfig wxPayConfig) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayConfig.getAppId());
        request.setMchid(wxPayConfig.getMerchantId());
        request.setDescription(payReq.getDescription());
        request.setNotifyUrl(wxPayConfig.getPayNotifyUrl());
        request.setOutTradeNo(payReq.getOutTradeNo());

        Amount amount = new Amount();
        amount.setTotal(payReq.getTotal());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(ReqInfoContext.getReqInfo().getClientIp());
        request.setSceneInfo(sceneInfo);

        log.info("微信native下单, 微信请求参数: {}", JsonUtil.toStr(request));
        PrepayResponse response = getNativeService(wxPayConfig).prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getCodeUrl());
        return response.getCodeUrl();
    }

    public static void closeOrder(String outTradeNo, WxPayConfig wxPayConfig) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        getNativeService(wxPayConfig).closeOrder(closeRequest);
    }

    public static Transaction queryOrder(String outTradeNo, WxPayConfig wxPayConfig) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(outTradeNo);
        return getNativeService(wxPayConfig).queryOrderByOutTradeNo(request);
    }


    private static NativePayService getNativeService(WxPayConfig wxPayConfig) {
        if (nativePayService == null) {
            synchronized (NativeWxPayService.class) {
                if (nativePayService == null) {
                    Config config = new RSAAutoCertificateConfig.Builder()
                            .merchantId(wxPayConfig.getMerchantId())
                            .privateKey(wxPayConfig.getPrivateKeyContent())
                            .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                            .apiV3Key(wxPayConfig.getApiV3Key())
                            .build();
                    nativePayService = new NativePayService.Builder().config(config).build();
                }
            }
        }
        return nativePayService;
    }
}
