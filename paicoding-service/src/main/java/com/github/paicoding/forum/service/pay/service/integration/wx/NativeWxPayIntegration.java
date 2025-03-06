package com.github.paicoding.forum.service.pay.service.integration.wx;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.pay.config.WxPayConfig;
import com.github.paicoding.forum.service.pay.model.PayCallbackBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.nativepay.model.SceneInfo;
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
public class NativeWxPayIntegration extends AbsWxPayIntegration {
    private NativePayService nativePayService;

    public NativeWxPayIntegration(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayConfig.getMerchantId())
                .privateKey(wxPayConfig.getPrivateKeyContent())
                .merchantSerialNumber(wxPayConfig.getMerchantSerialNumber())
                .apiV3Key(wxPayConfig.getApiV3Key())
                .build();
        nativePayService = new NativePayService.Builder().config(config).build();
    }

    @Override
    public boolean support(ThirdPayWayEnum payWay) {
        return ThirdPayWayEnum.WX_NATIVE == payWay;
    }

    /**
     * native 支付，生成扫描支付二维码唤起微信支付页面
     *
     * @return 形如 wx://xxx 的支付二维码
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
        amount.setCurrency("CNY");
        request.setAmount(amount);

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(ReqInfoContext.getReqInfo().getClientIp());
        request.setSceneInfo(sceneInfo);

        log.info("微信native下单, 微信请求参数: {}", JsonUtil.toStr(request));
        PrepayResponse response = nativePayService.prepay(request);
        log.info("微信支付 >>>>>>>>>>>> 返回: {}", response.getCodeUrl());
        return response.getCodeUrl();
    }

    public void closeOrder(String outTradeNo) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(wxPayConfig.getMerchantId());
        closeRequest.setOutTradeNo(outTradeNo);
        nativePayService.closeOrder(closeRequest);
    }

    public PayCallbackBo queryOrder(String outTradeNo) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(wxPayConfig.getMerchantId());
        request.setOutTradeNo(outTradeNo);
        Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
        return toBo(transaction);
    }
}
